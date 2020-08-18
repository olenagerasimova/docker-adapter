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
 */
@SuppressWarnings({
    "PMD.AvoidDuplicateLiterals",
    "PMD.TooManyMethods"
})
@DockerClientSupport
final class DockerSliceITCase {
    /**
     * Example docker image to use in tests.
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

    /**
     * Full image name in remote registry.
     */
    private String remote;

    @BeforeEach
    void setUp() throws Exception {
        this.repository = new DockerRepository(
            new AstoDocker(new InMemoryStorage())
        );
        this.repository.start();
        this.image = this.prepareImage();
        this.remote = this.image.remote().split("@")[0];
    }

    @AfterEach
    void tearDown() {
        this.repository.stop();
    }

    @Test
    void shouldPush() throws Exception {
        final String output = this.client.run("push", this.remote);
        MatcherAssert.assertThat(
            output,
            Matchers.allOf(this.layersPushed(), this.manifestPushed())
        );
    }

    @Test
    void shouldPushExisting() throws Exception {
        this.client.run("push", this.remote);
        final String output = this.client.run("push", this.remote);
        MatcherAssert.assertThat(
            output,
            Matchers.allOf(this.layersAlreadyExist(), this.manifestPushed())
        );
    }

    @Test
    void shouldPullPushedByTag() throws Exception {
        this.client.run("push", this.remote);
        this.client.run("image", "rm", this.local());
        this.client.run("image", "rm", this.remote);
        final String output = this.client.run("pull", this.remote);
        MatcherAssert.assertThat(
            output,
            new StringContains(
                false,
                String.format("Status: Downloaded newer image for %s", this.remote)
            )
        );
    }

    @Test
    void shouldPullPushedByDigest() throws Exception {
        this.client.run("push", this.remote);
        this.client.run("image", "rm", this.local());
        this.client.run("image", "rm", this.remote);
        final String output = this.client.run("pull", this.remoteByDigest());
        MatcherAssert.assertThat(
            output,
            new StringContains(
                false,
                String.format("Status: Downloaded newer image for %s", this.remoteByDigest())
            )
        );
    }

    private Image prepareImage() throws Exception {
        final Image tmpimg = new Image.ForOs();
        final String original = tmpimg.remote();
        this.client.run("pull", original);
        final String local = "my-test";
        this.client.run("tag", original, String.format("%s:latest", local));
        final Image img = new Image.From(
            this.repository.url(),
            local,
            tmpimg.digest(),
            tmpimg.layer()
        );
        this.client.run("tag", original, img.remote().split("@")[0]);
        return img;
    }

    private String local() {
        return this.image.name();
    }

    private String remoteByDigest() {
        return this.image.remote();
    }

    private Matcher<String> manifestPushed() {
        return new StringContains(false, String.format("latest: digest: %s", this.image.digest()));
    }

    private Matcher<String> layersPushed() {
        return new StringContains(false, String.format("%s: Pushed", this.image.layer()));
    }

    private Matcher<String> layersAlreadyExist() {
        return new StringContains(
            false,
            String.format("%s: Layer already exists", this.image.layer())
        );
    }
}
