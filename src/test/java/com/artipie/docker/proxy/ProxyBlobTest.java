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
import com.artipie.docker.Digest;
import com.artipie.docker.RepoName;
import com.artipie.docker.misc.ByteBufPublisher;
import com.artipie.http.Headers;
import com.artipie.http.rs.ContentLength;
import com.artipie.http.rs.RsFull;
import com.artipie.http.rs.RsStatus;
import java.util.Optional;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ProxyBlob}.
 *
 * @since 0.3
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
class ProxyBlobTest {

    @Test
    void shouldReadContent() {
        final byte[] data = "data".getBytes();
        final Content content = new ProxyBlob(
            (line, headers, body) -> {
                if (!line.startsWith("GET /v2/test/blobs/sha256:123 ")) {
                    throw new IllegalArgumentException();
                }
                return new RsFull(
                    RsStatus.OK,
                    new Headers.From(new ContentLength("4")),
                    new Content.From(data)
                );
            },
            new RepoName.Valid("test"),
            new Digest.FromString("sha256:123"),
            data.length
        ).content().toCompletableFuture().join();
        MatcherAssert.assertThat(
            new ByteBufPublisher(content).bytes().toCompletableFuture().join(),
            new IsEqual<>(data)
        );
        MatcherAssert.assertThat(
            content.size(),
            new IsEqual<>(Optional.of((long) data.length))
        );
    }

    @Test
    void shouldReadSize() {
        final long size = 1235L;
        final ProxyBlob blob = new ProxyBlob(
            (line, headers, body) -> {
                throw new UnsupportedOperationException();
            },
            new RepoName.Valid("my/test"),
            new Digest.FromString("sha256:abc"),
            size
        );
        MatcherAssert.assertThat(
            blob.size().toCompletableFuture().join(),
            new IsEqual<>(size)
        );
    }
}
