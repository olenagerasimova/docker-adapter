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
package com.artipie.docker.proxy;

import com.artipie.http.Headers;
import com.artipie.http.Response;
import com.artipie.http.Slice;
import com.artipie.http.async.AsyncResponse;
import com.artipie.http.rq.RequestLineFrom;
import com.artipie.http.rs.Header;
import com.artipie.http.rs.RsFull;
import com.artipie.http.rs.RsStatus;
import io.reactivex.Flowable;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.http.client.utils.URIBuilder;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.reactive.client.ReactiveRequest;
import org.eclipse.jetty.reactive.client.ReactiveResponse;
import org.reactivestreams.Publisher;

/**
 * HTTP client {@link Slice} implementation.
 *
 * @since 0.3
 */
public final class ClientSlice implements Slice {

    /**
     * HTTPS default port.
     */
    private static final int HTTPS_PORT = 443;

    /**
     * HTTP client.
     */
    private final HttpClient client;

    /**
     * Target host name.
     */
    private final String host;

    /**
     * Ctor.
     *
     * @param client HTTP client.
     * @param host Target host name.
     */
    public ClientSlice(final HttpClient client, final String host) {
        this.client = client;
        this.host = host;
    }

    @Override
    public Response response(
        final String line,
        final Iterable<Map.Entry<String, String>> unsupported,
        final Publisher<ByteBuffer> ignored
    ) {
        final RequestLineFrom req = new RequestLineFrom(line);
        final Request request = this.client.newRequest(
            new URIBuilder()
                .setScheme("https")
                .setHost(this.host)
                .setPort(ClientSlice.HTTPS_PORT)
                .setPath(req.uri().getPath())
                .toString()
        ).method(req.method().value()).followRedirects(true);
        return new AsyncResponse(
            Flowable.fromPublisher(
                ReactiveRequest.newBuilder(request).build().response(
                    (response, body) -> Flowable.just(
                        new RsFull(
                            new RsStatus.ByCode(response.getStatus()).find(),
                            ClientSlice.headers(response),
                            Flowable.fromPublisher(body).map(chunk -> chunk.buffer)
                        )
                    )
                )
            ).singleOrError()
        );
    }

    /**
     * Extract headers from response.
     *
     * @param response Response to extract headers from.
     * @return Headers.
     */
    private static Headers headers(final ReactiveResponse response) {
        return new Headers.From(
            response.getHeaders().stream()
                .map(header -> new Header(header.getName(), header.getValue()))
                .collect(Collectors.toList())
        );
    }
}
