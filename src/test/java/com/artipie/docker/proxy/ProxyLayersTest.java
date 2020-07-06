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

import com.artipie.docker.Blob;
import com.artipie.docker.Digest;
import com.artipie.docker.RepoName;
import com.artipie.http.Headers;
import com.artipie.http.headers.ContentLength;
import com.artipie.http.rs.RsFull;
import com.artipie.http.rs.RsStatus;
import io.reactivex.Flowable;
import java.util.Optional;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ProxyLayers}.
 *
 * @since 0.3
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
class ProxyLayersTest {

    @Test
    void shouldGetBlob() {
        final long size = 10L;
        final String digest = "sha256:123";
        final Optional<Blob> blob = new ProxyLayers(
            (line, headers, body) -> {
                if (!line.startsWith(String.format("HEAD /v2/test/blobs/%s ", digest))) {
                    throw new IllegalArgumentException();
                }
                return new RsFull(
                    RsStatus.OK,
                    new Headers.From(new ContentLength(String.valueOf(size))),
                    Flowable.empty()
                );
            },
            new RepoName.Valid("test")
        ).get(new Digest.FromString(digest)).toCompletableFuture().join();
        MatcherAssert.assertThat(blob.isPresent(), new IsEqual<>(true));
        MatcherAssert.assertThat(
            blob.get().digest().string(),
            new IsEqual<>(digest)
        );
        MatcherAssert.assertThat(
            blob.get().size().toCompletableFuture().join(),
            new IsEqual<>(size)
        );
    }
}
