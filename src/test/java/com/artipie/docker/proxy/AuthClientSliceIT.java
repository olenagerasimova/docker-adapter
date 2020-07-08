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

import com.artipie.docker.RepoName;
import com.artipie.docker.Tag;
import com.artipie.docker.manifest.Manifest;
import com.artipie.docker.ref.ManifestRef;
import java.util.Optional;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration test for {@link AuthClientSlice}.
 *
 * @since 0.3
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class AuthClientSliceIT {

    /**
     * HTTP client used for proxy.
     */
    private HttpClient client;

    /**
     * Repository URL.
     */
    private AuthClientSlice slice;

    @BeforeEach
    void setUp() throws Exception {
        this.client = new HttpClient(new SslContextFactory.Client());
        this.client.start();
        final ClientSlices slices = new ClientSlices(this.client);
        this.slice = new AuthClientSlice(
            slices,
            slices.slice("registry-1.docker.io")
        );
    }

    @AfterEach
    void tearDown() throws Exception {
        if (this.client != null) {
            this.client.stop();
        }
    }

    @Test
    void getManifestByTag() {
        final RepoName name = new RepoName.Valid("library/busybox");
        final ProxyManifests manifests = new ProxyManifests(this.slice, name);
        final ManifestRef ref = new ManifestRef.FromTag(new Tag.Valid("latest"));
        final Optional<Manifest> manifest = manifests.get(ref).toCompletableFuture().join();
        MatcherAssert.assertThat(
            manifest.isPresent(),
            new IsEqual<>(true)
        );
    }
}
