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
import com.artipie.docker.junit.DockerImage;
import com.artipie.docker.junit.DockerRepository;
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
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
@DockerClientSupport
public final class DockerSliceITCase {
    /**
     * Example docker image to use in tests.
     */
    private DockerImage image;

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
        this.image = new DockerImage(this.repository.url());
        this.image.initialize();
        this.prepare();
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

    private void prepare() throws Exception {
        final String original = String.format("%s@%s", this.image.local(), this.image.digest());
        this.client.run("pull", original);
        final String local = "my-test";
        this.client.run("tag", original, String.format("%s:latest", local));
        this.image.updateName(local);
        this.client.run("tag", original, this.image.remote());
    }
}
