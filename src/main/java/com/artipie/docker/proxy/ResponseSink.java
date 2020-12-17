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
import com.artipie.http.rs.RsStatus;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.reactivestreams.Publisher;

/**
 * Sink that accepts response data (status, headers and body) and transforms it into result object.
 *
 * @param <T> Result object type.
 * @since 0.10
 */
final class ResponseSink<T> {

    /**
     * Response.
     */
    private final Response response;

    /**
     * Response transformation.
     */
    private final Transformation<T> transform;

    /**
     * Ctor.
     *
     * @param response Response.
     * @param transform Response transformation.
     */
    ResponseSink(final Response response, final Transformation<T> transform) {
        this.response = response;
        this.transform = transform;
    }

    /**
     * Transform result into object.
     *
     * @return Result object.
     */
    public CompletionStage<T> result() {
        final CompletableFuture<T> promise = new CompletableFuture<>();
        return this.response.send(
            (status, headers, body) -> this.transform.transform(status, headers, body)
                .thenAccept(promise::complete)
        ).thenCompose(nothing -> promise);
    }

    /**
     * Transformation that transforms response into result object.
     *
     * @param <T> Result object type.
     * @since 0.10
     */
    interface Transformation<T> {

        /**
         * Transform response into an object.
         *
         * @param status Response status.
         * @param headers Response headers.
         * @param body Response body.
         * @return Completion stage for transformation.
         */
        CompletionStage<T> transform(RsStatus status, Headers headers, Publisher<ByteBuffer> body);
    }
}
