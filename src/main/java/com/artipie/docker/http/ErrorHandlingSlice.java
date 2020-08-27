/*
 * MIT License
 *
 * Copyright (c) 2020 Artipie
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.artipie.docker.http;

import com.artipie.docker.error.InvalidRepoNameException;
import com.artipie.docker.error.InvalidTagNameException;
import com.artipie.http.Response;
import com.artipie.http.Slice;
import com.artipie.http.rs.RsStatus;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import org.reactivestreams.Publisher;

/**
 * Slice that handles exceptions in origin slice by sending well-formed error responses.
 *
 * @since 0.5
 */
final class ErrorHandlingSlice implements Slice {

    /**
     * Origin.
     */
    private final Slice origin;

    /**
     * Ctor.
     *
     * @param origin Origin.
     */
    ErrorHandlingSlice(final Slice origin) {
        this.origin = origin;
    }

    @Override
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public Response response(
        final String line,
        final Iterable<Map.Entry<String, String>> headers,
        final Publisher<ByteBuffer> body
    ) {
        Response response;
        try {
            final Response original = this.origin.response(line, headers, body);
            response = connection -> {
                CompletionStage<Void> sent;
                try {
                    sent = original.send(connection);
                    // @checkstyle IllegalCatchCheck (1 line)
                } catch (final RuntimeException ex) {
                    sent = handle(ex).map(rsp -> rsp.send(connection)).orElseThrow(() -> ex);
                }
                return sent.handle(
                    (nothing, throwable) -> {
                        final CompletionStage<Void> result;
                        if (throwable == null) {
                            result = CompletableFuture.completedFuture(nothing);
                        } else {
                            result = handle(throwable)
                                .map(rsp -> rsp.send(connection))
                                .orElseGet(() -> CompletableFuture.failedFuture(throwable));
                        }
                        return result;
                    }
                ).thenCompose(Function.identity());
            };
            // @checkstyle IllegalCatchCheck (1 line)
        } catch (final RuntimeException ex) {
            response = handle(ex).orElseThrow(() -> ex);
        }
        return response;
    }

    /**
     * Translates throwable to error response.
     *
     * @param throwable Throwable to translate.
     * @return Result response, empty that throwable cannot be handled.
     * @checkstyle ReturnCountCheck (3 lines)
     */
    @SuppressWarnings("PMD.OnlyOneReturn")
    private static Optional<Response> handle(final Throwable throwable) {
        if (throwable instanceof InvalidRepoNameException) {
            return Optional.of(
                new ErrorsResponse(RsStatus.BAD_REQUEST, (InvalidRepoNameException) throwable)
            );
        }
        if (throwable instanceof InvalidTagNameException) {
            return Optional.of(
                new ErrorsResponse(RsStatus.BAD_REQUEST, (InvalidTagNameException) throwable)
            );
        }
        return Optional.empty();
    }
}
