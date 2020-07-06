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

import com.artipie.docker.Digest;
import com.artipie.docker.RepoName;
import com.artipie.docker.Tag;
import com.artipie.docker.manifest.Manifest;
import com.artipie.docker.misc.ByteBufPublisher;
import com.artipie.docker.misc.DigestFromContent;
import com.artipie.docker.ref.ManifestRef;
import com.artipie.http.Headers;
import com.artipie.http.headers.Authorization;
import com.artipie.http.rq.RequestLine;
import com.artipie.http.rq.RqMethod;
import com.artipie.http.rs.RsStatus;
import io.reactivex.Flowable;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNot;
import org.hamcrest.text.IsEmptyString;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration test for {@link ClientSlice}.
 *
 * @since 0.3
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class ClientSliceIT {

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
        final ClientSlice slice = new ClientSlice(this.client, "mcr.microsoft.com");
        final RepoName name = new RepoName.Valid("dotnet/core/runtime");
        final Digest digest = new Digest.Sha256(
            "b71717fef0141577dd1588f2838d9a797e026ca20d95d0a89559a6b6af734c7b"
        );
        final ProxyBlob blob = new ProxyBlob(slice, name, digest, 828L);
        MatcherAssert.assertThat(
            blob.content()
                .thenApply(DigestFromContent::new)
                .thenCompose(DigestFromContent::digest)
                .thenApply(Digest::string)
                .toCompletableFuture().join(),
            new IsEqual<>("sha256:b71717fef0141577dd1588f2838d9a797e026ca20d95d0a89559a6b6af734c7b")
        );
    }

    @Test
    void getManifestByDigest() {
        final ProxyManifests manifests = this.proxyManifests();
        final ManifestRef ref = new ManifestRef.FromDigest(
            new Digest.Sha256("c91e7b0fcc21d5ee1c7d3fad7e31c71ed65aa59f448f7dcc1756153c724c8b07")
        );
        final Optional<Manifest> manifest = manifests.get(ref).toCompletableFuture().join();
        MatcherAssert.assertThat(
            manifest.isPresent(),
            new IsEqual<>(true)
        );
    }

    @Test
    void getManifestByTag() {
        final ProxyManifests manifests = this.proxyManifests();
        final ManifestRef ref = new ManifestRef.FromTag(new Tag.Valid("latest"));
        final Optional<Manifest> manifest = manifests.get(ref).toCompletableFuture().join();
        MatcherAssert.assertThat(
            manifest.isPresent(),
            new IsEqual<>(true)
        );
    }

    @Test
    void getManifestNotFound() {
        final ProxyManifests manifests = this.proxyManifests();
        final ManifestRef ref = new ManifestRef.FromDigest(
            new Digest.FromString(
                "sha256:0123456789012345678901234567890123456789012345678901234567890123"
            )
        );
        final Optional<Manifest> manifest = manifests.get(ref).toCompletableFuture().join();
        MatcherAssert.assertThat(
            manifest.isPresent(),
            new IsEqual<>(false)
        );
    }

    @Test
    void shouldSendQueryAndHeaders() throws Exception {
        final ClientSlice slice = new ClientSlice(this.client, "auth.docker.io");
        final CompletableFuture<byte[]> promise = new CompletableFuture<>();
        final byte[] response = slice.response(
            new RequestLine(
                RqMethod.GET,
                "/token?service=registry.docker.io&scope=repository:library/ubuntu:pull"
            ).toString(),
            new Headers.From(
                new Authorization.Basic("testartipie", "db72e2e2-a690-43bc-a18b-335bd229aa3a")
            ),
            Flowable.empty()
        ).send(
            (status, headers, body) -> CompletableFuture.supplyAsync(
                () -> {
                    if (status != RsStatus.OK) {
                        throw new IllegalStateException(
                            String.format("Unexpected status: %s", status)
                        );
                    }
                    return new ByteBufPublisher(body).bytes().thenAccept(promise::complete);
                }
            ).thenCompose(Function.identity())
        ).thenCompose(nothing -> promise).toCompletableFuture().get(1, TimeUnit.MINUTES);
        MatcherAssert.assertThat(
            new TokenResponse(response).token(),
            new IsNot<>(IsEmptyString.emptyString())
        );
    }

    private ProxyManifests proxyManifests() {
        final ClientSlice slice = new ClientSlice(this.client, "mcr.microsoft.com");
        final RepoName name = new RepoName.Valid("dotnet/core/runtime");
        return new ProxyManifests(slice, name);
    }
}
