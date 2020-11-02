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

import com.artipie.asto.Content;
import com.artipie.asto.FailedCompletionStage;
import com.artipie.asto.ext.PublisherAs;
import com.artipie.docker.error.InvalidManifestException;
import com.artipie.docker.error.InvalidRepoNameException;
import com.artipie.docker.error.InvalidTagNameException;
import com.artipie.http.Headers;
import com.artipie.http.Response;
import com.artipie.http.client.auth.AuthClientSlice;
import com.artipie.http.client.auth.Authenticator;
import com.artipie.http.headers.Header;
import com.artipie.http.hm.ResponseMatcher;
import com.artipie.http.rq.RequestLine;
import com.artipie.http.rq.RqMethod;
import com.artipie.http.rs.RsFull;
import com.artipie.http.rs.RsStatus;
import com.artipie.http.rs.StandardRs;
import io.reactivex.Flowable;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests for {@link ErrorHandlingSlice}.
 *
 * @since 0.5
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class ErrorHandlingSliceTest {

    @Test
    void shouldPassRequestUnmodified() {
        final String line = new RequestLine(RqMethod.GET, "/file.txt").toString();
        final Header header = new Header("x-name", "some value");
        final byte[] body = "text".getBytes();
        new ErrorHandlingSlice(
            (rqline, rqheaders, rqbody) -> {
                MatcherAssert.assertThat(
                    "Request line unmodified",
                    rqline,
                    new IsEqual<>(line)
                );
                MatcherAssert.assertThat(
                    "Headers unmodified",
                    rqheaders,
                    Matchers.containsInAnyOrder(header)
                );
                MatcherAssert.assertThat(
                    "Body unmodified",
                    new PublisherAs(rqbody).bytes().toCompletableFuture().join(),
                    new IsEqual<>(body)
                );
                return StandardRs.OK;
            }
        ).response(
            line, new Headers.From(header), Flowable.just(ByteBuffer.wrap(body))
        ).send(
            (status, rsheaders, rsbody) -> CompletableFuture.allOf()
        ).toCompletableFuture().join();
    }

    @Test
    void shouldPassResponseUnmodified() {
        final Header header = new Header("x-name", "some value");
        final byte[] body = "text".getBytes();
        final RsStatus status = RsStatus.OK;
        final Response response = new AuthClientSlice(
            (rsline, rsheaders, rsbody) -> new RsFull(
                status,
                new Headers.From(header),
                Flowable.just(ByteBuffer.wrap(body))
            ),
            Authenticator.ANONYMOUS
        ).response(new RequestLine(RqMethod.GET, "/").toString(), Headers.EMPTY, Flowable.empty());
        MatcherAssert.assertThat(
            response,
            new ResponseMatcher(status, body, header)
        );
    }

    @ParameterizedTest
    @MethodSource("exceptions")
    void shouldHandleErrorInvalid(final RuntimeException exception, final String code) {
        MatcherAssert.assertThat(
            new ErrorHandlingSlice(
                (line, headers, body) -> connection -> new FailedCompletionStage<>(exception)
            ).response(
                new RequestLine(RqMethod.GET, "/").toString(),
                Headers.EMPTY,
                Flowable.empty()
            ),
            new IsErrorsResponse(RsStatus.BAD_REQUEST, code)
        );
    }

    @ParameterizedTest
    @MethodSource("exceptions")
    void shouldHandleSliceError(final RuntimeException exception, final String code) {
        MatcherAssert.assertThat(
            new ErrorHandlingSlice(
                (line, headers, body) -> {
                    throw exception;
                }
            ).response(
                new RequestLine(RqMethod.GET, "/").toString(),
                Headers.EMPTY,
                Content.EMPTY
            ),
            new IsErrorsResponse(RsStatus.BAD_REQUEST, code)
        );
    }

    @ParameterizedTest
    @MethodSource("exceptions")
    void shouldHandleConnectionError(final RuntimeException exception, final String code) {
        MatcherAssert.assertThat(
            new ErrorHandlingSlice(
                (line, headers, body) -> connection -> {
                    throw exception;
                }
            ).response(
                new RequestLine(RqMethod.GET, "/").toString(),
                Headers.EMPTY,
                Content.EMPTY
            ),
            new IsErrorsResponse(RsStatus.BAD_REQUEST, code)
        );
    }

    @Test
    void shouldPassSliceError() {
        final RuntimeException exception = new IllegalStateException();
        final ErrorHandlingSlice slice = new ErrorHandlingSlice(
            (line, headers, body) -> {
                throw exception;
            }
        );
        final Exception actual = Assertions.assertThrows(
            exception.getClass(),
            () -> slice.response(
                new RequestLine(RqMethod.GET, "/").toString(),
                Headers.EMPTY,
                Content.EMPTY
            ).send(
                (status, headers, body) -> CompletableFuture.allOf()
            ).toCompletableFuture().join(),
            "Exception not handled"
        );
        MatcherAssert.assertThat(
            "Original exception preserved",
            actual,
            new IsEqual<>(exception)
        );
    }

    @Test
    void shouldPassConnectionError() {
        final RuntimeException exception = new IllegalStateException();
        final ErrorHandlingSlice slice = new ErrorHandlingSlice(
            (line, headers, body) -> connection -> {
                throw exception;
            }
        );
        final Exception actual = Assertions.assertThrows(
            exception.getClass(),
            () -> slice.response(
                new RequestLine(RqMethod.GET, "/").toString(),
                Headers.EMPTY,
                Content.EMPTY
            ).send(
                (status, headers, body) -> CompletableFuture.allOf()
            ).toCompletableFuture().join(),
            "Exception not handled"
        );
        MatcherAssert.assertThat(
            "Original exception preserved",
            actual,
            new IsEqual<>(exception)
        );
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private static Stream<Arguments> exceptions() {
        final InvalidRepoNameException repo = new InvalidRepoNameException("repo name exception");
        final InvalidTagNameException tag = new InvalidTagNameException("tag name exception");
        final InvalidManifestException mnf = new InvalidManifestException("manifest exception");
        return Stream.of(
            Arguments.of(repo, repo.code()),
            Arguments.of(tag, tag.code()),
            Arguments.of(mnf, mnf.code())
        );
    }
}
