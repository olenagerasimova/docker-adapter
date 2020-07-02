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
package com.artipie.docker.http;

import com.artipie.asto.memory.InMemoryStorage;
import com.artipie.docker.Digest;
import com.artipie.docker.Docker;
import com.artipie.docker.Manifests;
import com.artipie.docker.RepoName;
import com.artipie.docker.asto.AstoDocker;
import com.artipie.docker.cache.CacheDocker;
import com.artipie.docker.junit.DockerClient;
import com.artipie.docker.junit.DockerClientSupport;
import com.artipie.docker.junit.DockerRepository;
import com.artipie.docker.proxy.ClientSlice;
import com.artipie.docker.proxy.ProxyDocker;
import com.artipie.docker.ref.ManifestRef;
import com.artipie.http.slice.LoggingSlice;
import com.google.common.base.Stopwatch;
import java.util.concurrent.TimeUnit;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.StringContains;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

/**
 * Integration test for {@link ProxyDocker}.
 *
 * @since 0.3
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
@DisabledOnOs(OS.LINUX)
@DockerClientSupport
final class CachingProxyITCase {

    /**
     * Docker client.
     */
    private DockerClient cli;

    /**
     * Docker cache.
     */
    private Docker cache;

    /**
     * HTTP client used for proxy.
     */
    private HttpClient client;

    /**
     * Docker repository.
     */
    private DockerRepository repo;

    @BeforeEach
    void setUp() throws Exception {
        this.cache = new AstoDocker(new InMemoryStorage());
        this.client = new HttpClient(new SslContextFactory.Client());
        this.client.start();
        this.repo = new DockerRepository(
            new CacheDocker(
                new ProxyDocker(
                    new LoggingSlice(new ClientSlice(this.client, "mcr.microsoft.com"))
                ),
                this.cache
            )
        );
        this.repo.start();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (this.repo != null) {
            this.repo.stop();
        }
        if (this.client != null) {
            this.client.stop();
        }
    }

    @Test
    void shouldPull() throws Exception {
        final String digest = String.format(
            "%s:%s",
            "sha256",
            "c91e7b0fcc21d5ee1c7d3fad7e31c71ed65aa59f448f7dcc1756153c724c8b07"
        );
        final String image = String.format("%s/dotnet/core/runtime@%s", this.repo.url(), digest);
        final String output = this.cli.run("pull", image);
        MatcherAssert.assertThat(output, CachingProxyITCase.imagePulled(image));
    }

    @Test
    void shouldPullWhenRemoteIsDown() throws Exception {
        final String name = "dotnet/core/runtime";
        final String digest = String.format(
            "%s:%s",
            "sha256",
            "c91e7b0fcc21d5ee1c7d3fad7e31c71ed65aa59f448f7dcc1756153c724c8b07"
        );
        final String image = String.format("%s/%s@%s", this.repo.url(), name, digest);
        this.cli.run("pull", image);
        this.awaitManifestCached(name, digest);
        this.cli.run("image", "rm", image);
        this.client.stop();
        final String output = this.cli.run("pull", image);
        MatcherAssert.assertThat(output, CachingProxyITCase.imagePulled(image));
    }

    private void awaitManifestCached(final String name, final String digest) throws Exception {
        final Manifests manifests = this.cache.repo(new RepoName.Simple(name)).manifests();
        final ManifestRef ref = new ManifestRef.FromDigest(new Digest.FromString(digest));
        final Stopwatch stopwatch = Stopwatch.createStarted();
        while (manifests.get(ref).toCompletableFuture().join().isEmpty()) {
            if (stopwatch.elapsed(TimeUnit.SECONDS) > TimeUnit.MINUTES.toSeconds(1)) {
                throw new IllegalStateException(
                    String.format(
                        "Manifest is expected to be present, but it was not found after %s seconds",
                        stopwatch.elapsed(TimeUnit.SECONDS)
                    )
                );
            }
            Thread.sleep(TimeUnit.SECONDS.toMillis(1));
        }
    }

    private static Matcher<String> imagePulled(final String image) {
        return new StringContains(
            false,
            String.format("Status: Downloaded newer image for %s", image)
        );
    }
}
