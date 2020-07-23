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
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.StringContains;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration test for authentication in {@link DockerSlice}.
 *
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @since 0.4
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
@DockerClientSupport
final class DockerAuthITCase {

    /**
     * Docker client.
     */
    private DockerClient cli;

    /**
     * Docker repository.
     */
    private DockerRepository repo;

    @BeforeEach
    void setUp() throws Exception {
        this.repo = new DockerRepository(
            new DockerSlice(
                new AstoDocker(new InMemoryStorage()),
                (name, action) -> TestAuthentication.USERNAME.equals(name),
                new TestAuthentication()
            )
        );
        this.repo.start();
        this.cli.run(
            "login",
            "--username", TestAuthentication.USERNAME,
            "--password", TestAuthentication.PASSWORD,
            this.repo.url()
        );
    }

    @AfterEach
    void tearDown() {
        this.repo.stop();
    }

    @Test
    void shouldPush() throws Exception {
        final Image original = new Image.ForOs();
        final String image = this.copy(original);
        final String output = this.cli.run("push", image);
        MatcherAssert.assertThat(
            output,
            new StringContains(
                false,
                String.format("latest: digest: %s", original.digest())
            )
        );
    }

    @Test
    void shouldPull() throws Exception {
        final String image = this.copy(new Image.ForOs());
        this.cli.run("push", image);
        this.cli.run("image", "rm", image);
        final String output = this.cli.run("pull", image);
        MatcherAssert.assertThat(
            output,
            new StringContains(
                false,
                String.format("Status: Downloaded newer image for %s", image)
            )
        );
    }

    private String copy(final Image original) throws Exception {
        this.cli.run("pull", original.remote());
        final String copy = String.format("%s/my-test/latest", this.repo.url());
        this.cli.run("tag", original.remote(), copy);
        return copy;
    }
}
