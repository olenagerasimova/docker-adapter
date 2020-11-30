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

import com.artipie.asto.Content;
import com.artipie.asto.FailedCompletionStage;
import com.artipie.asto.ext.PublisherAs;
import com.artipie.docker.Digest;
import com.artipie.docker.Manifests;
import com.artipie.docker.Repo;
import com.artipie.docker.RepoName;
import com.artipie.docker.Tags;
import com.artipie.docker.http.DigestHeader;
import com.artipie.docker.manifest.JsonManifest;
import com.artipie.docker.manifest.Manifest;
import com.artipie.docker.ref.ManifestRef;
import com.artipie.http.Headers;
import com.artipie.http.Slice;
import com.artipie.http.rq.RequestLine;
import com.artipie.http.rq.RqMethod;
import com.artipie.http.rs.RsStatus;
import io.reactivex.Flowable;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Proxy implementation of {@link Repo}.
 *
 * @since 0.3
 * @todo #354:30min Implement tags method in ProxyManifests
 *  `tags` method was added without proper implementation as placeholder.
 *  Method should be implemented and covered with unit tests.
 */
public final class ProxyManifests implements Manifests {

    /**
     * Remote repository.
     */
    private final Slice remote;

    /**
     * Repository name.
     */
    private final RepoName name;

    /**
     * Ctor.
     *
     * @param remote Remote repository.
     * @param name Repository name.
     */
    public ProxyManifests(final Slice remote, final RepoName name) {
        this.remote = remote;
        this.name = name;
    }

    @Override
    public CompletionStage<Manifest> put(final ManifestRef ref, final Content content) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CompletionStage<Optional<Manifest>> get(final ManifestRef ref) {
        final CompletableFuture<Optional<Manifest>> promise = new CompletableFuture<>();
        return this.remote.response(
            new RequestLine(RqMethod.GET, new ManifestPath(this.name, ref).string()).toString(),
            Headers.EMPTY,
            Flowable.empty()
        ).send(
            (status, headers, body) -> {
                final CompletionStage<Optional<Manifest>> result;
                if (status == RsStatus.OK) {
                    final Digest digest = new DigestHeader(headers).value();
                    result = new PublisherAs(body).bytes().thenApply(
                        bytes -> Optional.of(new JsonManifest(digest, bytes))
                    );
                } else if (status == RsStatus.NOT_FOUND) {
                    result = CompletableFuture.completedFuture(Optional.empty());
                } else {
                    result = new FailedCompletionStage<>(
                        new IllegalArgumentException(String.format("Unexpected status: %s", status))
                    );
                }
                return result.thenAccept(promise::complete).toCompletableFuture();
            }
        ).thenCompose(nothing -> promise);
    }

    @Override
    public CompletionStage<Tags> tags() {
        throw new UnsupportedOperationException();
    }
}
