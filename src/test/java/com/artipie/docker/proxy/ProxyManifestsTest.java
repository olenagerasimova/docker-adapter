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
import com.artipie.docker.http.DigestHeader;
import com.artipie.docker.manifest.Manifest;
import com.artipie.docker.misc.ByteBufPublisher;
import com.artipie.docker.ref.ManifestRef;
import com.artipie.http.Headers;
import com.artipie.http.rs.RsFull;
import com.artipie.http.rs.RsStatus;
import com.artipie.http.rs.RsWithStatus;
import java.util.Optional;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ProxyManifests}.
 *
 * @since 0.3
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
class ProxyManifestsTest {

    @Test
    void shouldGetManifest() {
        final byte[] data = "data".getBytes();
        final String digest = "sha256:123";
        final Optional<Manifest> found = new ProxyManifests(
            (line, headers, body) -> {
                if (!line.startsWith("GET /v2/test/manifests/abc ")) {
                    throw new IllegalArgumentException();
                }
                return new RsFull(
                    RsStatus.OK,
                    new Headers.From(new DigestHeader(new Digest.FromString(digest))),
                    new Content.From(data)
                );
            },
            new RepoName.Valid("test")
        ).get(new ManifestRef.FromString("abc")).toCompletableFuture().join();
        MatcherAssert.assertThat(found.isPresent(), new IsEqual<>(true));
        final Manifest manifest = found.get();
        MatcherAssert.assertThat(manifest.digest().string(), new IsEqual<>(digest));
        final Content content = manifest.content();
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
    void shouldGetEmptyWhenNotFound() {
        final Optional<Manifest> found = new ProxyManifests(
            (line, headers, body) -> {
                if (!line.startsWith("GET /v2/my-test/manifests/latest ")) {
                    throw new IllegalArgumentException();
                }
                return new RsWithStatus(RsStatus.NOT_FOUND);
            },
            new RepoName.Valid("my-test")
        ).get(new ManifestRef.FromString("latest")).toCompletableFuture().join();
        MatcherAssert.assertThat(found.isEmpty(), new IsEqual<>(true));
    }
}
