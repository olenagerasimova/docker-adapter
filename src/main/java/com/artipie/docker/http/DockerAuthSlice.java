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

import com.artipie.docker.error.DeniedError;
import com.artipie.docker.error.UnauthorizedError;
import com.artipie.http.Response;
import com.artipie.http.Slice;
import com.artipie.http.rs.RsStatus;
import com.artipie.http.rs.RsWithHeaders;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import org.reactivestreams.Publisher;

/**
 * Slice that wraps origin Slice replacing body with errors JSON in Docker API format
 * for 403 Unauthorized response status.
 *
 * @since 0.5
 */
final class DockerAuthSlice implements Slice {

    /**
     * Origin slice.
     */
    private final Slice origin;

    /**
     * Ctor.
     *
     * @param origin Origin slice.
     */
    DockerAuthSlice(final Slice origin) {
        this.origin = origin;
    }

    @Override
    public Response response(
        final String rqline,
        final Iterable<Map.Entry<String, String>> rqheaders,
        final Publisher<ByteBuffer> rqbody) {
        final Response response = this.origin.response(rqline, rqheaders, rqbody);
        return connection -> response.send(
            (rsstatus, rsheaders, rsbody) -> {
                final CompletionStage<Void> sent;
                if (rsstatus == RsStatus.UNAUTHORIZED) {
                    sent = new RsWithHeaders(
                        new ErrorsResponse(rsstatus, new UnauthorizedError()),
                        rsheaders
                    ).send(connection);
                } else if (rsstatus == RsStatus.FORBIDDEN) {
                    sent = new RsWithHeaders(
                        new ErrorsResponse(rsstatus, new DeniedError()),
                        rsheaders
                    ).send(connection);
                } else {
                    sent = connection.accept(rsstatus, rsheaders, rsbody);
                }
                return sent;
            }
        );
    }
}
