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
package com.artipie.docker.composite;

import com.artipie.docker.Blob;
import com.artipie.docker.Digest;
import com.artipie.docker.RepoName;
import com.artipie.docker.misc.DigestFromContent;
import com.artipie.docker.proxy.AuthClientSlice;
import com.artipie.docker.proxy.ClientSlices;
import com.artipie.docker.proxy.ProxyLayers;
import com.artipie.http.slice.LoggingSlice;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration test for {@link MultiReadLayers}.
 *
 * @since 0.3
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
class MultiReadLayersIT {

    /**
     * HTTP client used for proxy.
     */
    private HttpClient client;

    @BeforeEach
    void setUp() throws Exception {
        this.client = new HttpClient(new SslContextFactory.Client());
        this.client.start();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (this.client != null) {
            this.client.stop();
        }
    }

    @Test
    void shouldGetBlob() {
        final ClientSlices slices = new ClientSlices(this.client);
        final RepoName name = new RepoName.Valid("library/busybox");
        final MultiReadLayers layers = new MultiReadLayers(
            Stream.of(
                slices.slice("mcr.microsoft.com"),
                new AuthClientSlice(slices, slices.slice("registry-1.docker.io"))
            ).map(LoggingSlice::new).map(
                slice -> new ProxyLayers(slice, name)
            ).collect(Collectors.toList())
        );
        final String digest = String.format(
            "%s:%s",
            "sha256",
            "78096d0a54788961ca68393e5f8038704b97d8af374249dc5c8faec1b8045e42"
        );
        MatcherAssert.assertThat(
            layers.get(new Digest.FromString(digest))
                .thenApply(Optional::get)
                .thenCompose(Blob::content)
                .thenApply(DigestFromContent::new)
                .thenCompose(DigestFromContent::digest)
                .thenApply(Digest::string)
                .toCompletableFuture().join(),
            new IsEqual<>(digest)
        );
    }
}
