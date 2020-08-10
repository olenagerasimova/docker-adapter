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

import com.artipie.docker.misc.ByteBufPublisher;
import com.artipie.http.Headers;
import com.artipie.http.Response;
import com.artipie.http.Slice;
import com.artipie.http.async.AsyncResponse;
import com.artipie.http.client.ClientSlices;
import com.artipie.http.headers.Authorization;
import com.artipie.http.headers.WwwAuthenticate;
import com.artipie.http.rq.RequestLine;
import com.artipie.http.rq.RqMethod;
import com.artipie.http.rs.RsFull;
import com.artipie.http.rs.RsStatus;
import io.reactivex.Flowable;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import org.reactivestreams.Publisher;

/**
 * Slice augmenting requests with Authorization header when needed.
 *
 * @since 0.3
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class AuthClientSlice implements Slice {

    /**
     * Client slices.
     */
    private final ClientSlices client;

    /**
     * Origin slice.
     */
    private final Slice origin;

    /**
     * Credentials.
     */
    private final Credentials credentials;

    /**
     * Ctor.
     *
     * @param client Client slices.
     * @param origin Origin slice.
     */
    public AuthClientSlice(final ClientSlices client, final Slice origin) {
        this(client, origin, Credentials.ANONYMOUS);
    }

    /**
     * Ctor.
     *
     * @param client Client slices.
     * @param origin Origin slice.
     * @param credentials Credentials.
     */
    public AuthClientSlice(
        final ClientSlices client,
        final Slice origin,
        final Credentials credentials
    ) {
        this.client = client;
        this.origin = origin;
        this.credentials = credentials;
    }

    @Override
    public Response response(
        final String line,
        final Iterable<Map.Entry<String, String>> headers,
        final Publisher<ByteBuffer> body) {
        final CompletableFuture<Response> promise = new CompletableFuture<>();
        return new AsyncResponse(
            this.origin.response(line, headers, body).send(
                (rsstatus, rsheaders, rsbody) -> {
                    final RsFull original = new RsFull(rsstatus, rsheaders, rsbody);
                    final Response response;
                    if (rsstatus == RsStatus.UNAUTHORIZED) {
                        response = new AsyncResponse(
                            this.authenticate(new WwwAuthenticate(rsheaders)).thenApply(
                                authorization -> this.origin.response(
                                    line,
                                    new Headers.From(headers, authorization),
                                    body
                                )
                            )
                        );
                    } else {
                        response = original;
                    }
                    promise.complete(response);
                    return CompletableFuture.allOf();
                }
            ).thenCompose(nothing -> promise)
        );
    }

    /**
     * Create Authorization header for given WWW-Authenticate header.
     *
     * @param header WWW-Authenticate header.
     * @return Authorization header.
     */
    private CompletionStage<Map.Entry<String, String>> authenticate(final WwwAuthenticate header) {
        final String scheme = header.scheme();
        if (!scheme.equals("Bearer")) {
            throw new IllegalArgumentException(
                String.format("Unsupported authentication scheme: %s", scheme)
            );
        }
        final URI realm;
        try {
            realm = new URI(header.realm());
        } catch (final URISyntaxException ex) {
            throw new IllegalArgumentException(ex);
        }
        final String path = realm.getPath();
        final String query = header.params().stream()
            .filter(param -> !param.name().equals("realm"))
            .map(param -> String.format("%s=%s", param.name(), param.value()))
            .collect(Collectors.joining("&"));
        final CompletableFuture<String> promise = new CompletableFuture<>();
        return this.client.https(realm.getHost()).response(
            new RequestLine(
                RqMethod.GET,
                String.format("%s?%s", path, query)
            ).toString(),
            this.credentials.headers(),
            Flowable.empty()
        ).send(
            (status, headers, body) -> new ByteBufPublisher(body).bytes()
                .thenApply(TokenResponse::new)
                .thenApply(TokenResponse::token)
                .thenCompose(
                    token -> {
                        promise.complete(token);
                        return CompletableFuture.allOf();
                    }
                )
        ).thenCompose(ignored -> promise).thenApply(Authorization.Bearer::new);
    }
}
