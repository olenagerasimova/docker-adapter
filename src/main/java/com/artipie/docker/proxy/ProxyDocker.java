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
import com.artipie.docker.Catalog;
import com.artipie.docker.Docker;
import com.artipie.docker.Repo;
import com.artipie.docker.RepoName;
import com.artipie.docker.TestPublisherAs;
import com.artipie.http.Headers;
import com.artipie.http.Slice;
import com.artipie.http.rq.RequestLine;
import com.artipie.http.rq.RqMethod;
import com.artipie.http.rs.RsStatus;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

/**
 * Proxy {@link Docker} implementation.
 *
 * @since 0.3
 */
public final class ProxyDocker implements Docker {

    /**
     * Remote repository.
     */
    private final Slice remote;

    /**
     * Ctor.
     *
     * @param remote Remote repository.
     */
    public ProxyDocker(final Slice remote) {
        this.remote = remote;
    }

    @Override
    public Repo repo(final RepoName name) {
        return new ProxyRepo(this.remote, name);
    }

    @Override
    public CompletionStage<Catalog> catalog(final Optional<RepoName> from, final int limit) {
        return new ResponseSink<>(
            this.remote.response(
                new RequestLine(RqMethod.GET, new CatalogUri(from, limit).string()).toString(),
                Headers.EMPTY,
                Content.EMPTY
            ),
            (status, headers, body) -> {
                final CompletionStage<Catalog> result;
                if (status == RsStatus.OK) {
                    result = new TestPublisherAs(body).bytes().thenApply(
                        bytes -> () -> new Content.From(bytes)
                    );
                } else {
                    result = new FailedCompletionStage<>(
                        new IllegalArgumentException(String.format("Unexpected status: %s", status))
                    );
                }
                return result;
            }
        ).result();
    }
}
