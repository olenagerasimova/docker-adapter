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
import com.artipie.docker.asto.AstoDocker;
import com.artipie.docker.junit.DockerClient;
import com.artipie.docker.junit.DockerClientSupport;
import com.artipie.docker.junit.DockerRepository;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.core.StringContains;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration test for {@link DockerSlice}.
 *
 * @since 0.2
 * @todo #131:30min Refactor DockerSliceITCase to have less methods.
 *  DockerSliceITCase became too big, containing both tests cases and image preparation logic.
 *  It would be nice to extract logic regarding image preparation and docker client running
 *  to separate classes.
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings({
    "PMD.TooManyMethods",
    "PMD.AvoidDuplicateLiterals",
    "PMD.UseObjectForClearerAPI"
})
@DockerClientSupport
final class DockerSliceITCase {
    /**
     * Example image to use in tests.
     */
    private Image image;

    /**
     * Docker client.
     */
    private DockerClient client;

    /**
     * Docker repository.
     */
    private DockerRepository repository;

    @BeforeEach
    void setUp() throws Exception {
        this.repository = new DockerRepository(
            new AstoDocker(new InMemoryStorage())
        );
        this.repository.start();
        this.image = this.prepareImage();
    }

    @AfterEach
    void tearDown() {
        this.repository.stop();
    }

    @Test
    void shouldPush() throws Exception {
        final String output = this.client.run("push", this.image.remote());
        MatcherAssert.assertThat(
            output,
            Matchers.allOf(this.image.layersPushed(), this.image.manifestPushed())
        );
    }

    @Test
    void shouldPushExisting() throws Exception {
        this.client.run("push", this.image.remote());
        final String output = this.client.run("push", this.image.remote());
        MatcherAssert.assertThat(
            output,
            Matchers.allOf(this.image.layersAlreadyExist(), this.image.manifestPushed())
        );
    }

    @Test
    void shouldPullPushedByTag() throws Exception {
        this.client.run("push", this.image.remote());
        this.client.run("image", "rm", this.image.local());
        this.client.run("image", "rm", this.image.remote());
        final String output = this.client.run("pull", this.image.remote());
        MatcherAssert.assertThat(
            output,
            new StringContains(
                false,
                String.format("Status: Downloaded newer image for %s", this.image.remote())
            )
        );
    }

    @Test
    void shouldPullPushedByDigest() throws Exception {
        this.client.run("push", this.image.remote());
        this.client.run("image", "rm", this.image.local());
        this.client.run("image", "rm", this.image.remote());
        final String output = this.client.run("pull", this.image.remoteByDigest());
        MatcherAssert.assertThat(
            output,
            new StringContains(
                false,
                String.format("Status: Downloaded newer image for %s", this.image.remoteByDigest())
            )
        );
    }

    private Image prepareImage() throws Exception {
        final Image img;
        if (System.getProperty("os.name").startsWith("Windows")) {
            img = this.windowsImage();
        } else {
            img = this.linuxImage();
        }
        return img;
    }

    /**
     * Prepare `mcr.microsoft.com/dotnet/core/runtime:3.1.4-nanoserver-1809` image
     * for Windows Server 2019 amd64 architecture.
     *
     * @return Prepared image.
     * @throws Exception In case preparation fails.
     */
    private Image windowsImage() throws Exception {
        return this.prepare(
            "mcr.microsoft.com/dotnet/core/runtime",
            String.format(
                "%s:%s",
                "sha256",
                "c91e7b0fcc21d5ee1c7d3fad7e31c71ed65aa59f448f7dcc1756153c724c8b07"
            ),
            "d9e06d032060"
        );
    }

    /**
     * Prepare `amd64/busybox:1.31.1` image for linux/amd64 architecture.
     *
     * @return Prepared image.
     * @throws Exception In case preparation fails.
     */
    private Image linuxImage() throws Exception {
        return this.prepare(
            "busybox",
            String.format(
                "%s:%s",
                "sha256",
                "a7766145a775d39e53a713c75b6fd6d318740e70327aaa3ed5d09e0ef33fc3df"
            ),
            "1079c30efc82"
        );
    }

    // @checkstyle ParameterNumberCheck (5 lines)
    private Image prepare(
        final String name,
        final String digest,
        final String layer) throws Exception {
        final String original = String.format("%s@%s", name, digest);
        this.client.run("pull", original);
        final String local = "my-test";
        this.client.run("tag", original, String.format("%s:latest", local));
        final Image img = new Image(this.repository.url(), local, digest, layer);
        this.client.run("tag", original, img.remote());
        return img;
    }

    /**
     * Docker image info.
     *
     * @since 0.2
     */
    private static class Image {

        /**
         * Repo URL.
         */
        private final String repo;

        /**
         * Image name.
         */
        private final String name;

        /**
         * Manifest digest.
         */
        private final String digest;

        /**
         * Short layer hash.
         */
        private final String layer;

        // @checkstyle ParameterNumberCheck (5 lines)
        Image(final String repo, final String name, final String digest, final String layer) {
            this.repo = repo;
            this.name = name;
            this.digest = digest;
            this.layer = layer;
        }

        public String local() {
            return this.name;
        }

        public String remote() {
            return String.format("%s/%s", this.repo, this.local());
        }

        public String remoteByDigest() {
            return String.format("%s@%s", this.remote(), this.digest);
        }

        public Matcher<String> manifestPushed() {
            return new StringContains(false, String.format("latest: digest: %s", this.digest));
        }

        public Matcher<String> layersPushed() {
            return new StringContains(false, String.format("%s: Pushed", this.layer));
        }

        public Matcher<String> layersAlreadyExist() {
            return new StringContains(false, String.format("%s: Layer already exists", this.layer));
        }
    }
}
