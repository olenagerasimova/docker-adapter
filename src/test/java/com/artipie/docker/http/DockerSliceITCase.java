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

import com.artipie.docker.ExampleStorage;
import com.artipie.docker.asto.AstoDocker;
import com.artipie.http.slice.LoggingSlice;
import com.artipie.vertx.VertxSliceServer;
import com.google.common.collect.ImmutableList;
import com.jcabi.log.Logger;
import io.vertx.reactivex.core.Vertx;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.core.StringContains;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Integration test for {@link DockerSlice}.
 *
 * @todo #54:30min Support tests running on Windows platform
 *  Docker client on Windows cannot pull Linux images.
 *  This requires to select image for tests depending on `os.name` property
 *  and redesign pull tests to push images first.
 * @since 0.2
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@DisabledIfSystemProperty(named = "os.name", matches = "Windows.*")
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
final class DockerSliceITCase {

    // @checkstyle VisibilityModifierCheck (5 lines)
    /**
     * Temporary directory.
     */
    @TempDir
    Path temp;

    /**
     * Vert.x instance to use in tests.
     */
    private Vertx vertx;

    /**
     * HTTP server hosting repository.
     */
    private VertxSliceServer server;

    /**
     * Repository URL.
     */
    private String repo;

    @BeforeEach
    void setUp() throws Exception {
        this.vertx = Vertx.vertx();
        this.ensureDockerInstalled();
        this.server = new VertxSliceServer(
            this.vertx,
            new LoggingSlice(new DockerSlice(new AstoDocker(new ExampleStorage())))
        );
        final int port = this.server.start();
        this.repo = String.format("localhost:%s", port);
    }

    @AfterEach
    void tearDown() {
        if (this.server != null) {
            this.server.stop();
        }
        if (this.vertx != null) {
            this.vertx.close();
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "my-alpine:latest",
        "my-alpine@sha256:cb8a924afdf0229ef7515d9e5b3024e23b3eb03ddbba287f4a19c6ac90b8d221"
    })
    void shouldPull(final String name) throws Exception {
        final String image = String.format("%s/%s", this.repo, name);
        final String output = this.run("pull", image);
        MatcherAssert.assertThat(
            output,
            new StringContains(
                false,
                String.format("Status: Downloaded newer image for %s", image)
            )
        );
    }

    @Test
    void shouldPush() throws Exception {
        final String original = "busybox";
        this.run("pull", original);
        final String remote = String.format("%s/my-%s", this.repo, original);
        this.run("tag", original, remote);
        final String output = this.run("push", remote);
        MatcherAssert.assertThat(
            output,
            Matchers.allOf(
                new StringContains(false, "1079c30efc82: Pushed"),
                new StringContains(
                    false,
                    String.format(
                        "latest: digest: %s:%s",
                        "sha256",
                        "a7766145a775d39e53a713c75b6fd6d318740e70327aaa3ed5d09e0ef33fc3df"
                    )
                )
            )
        );
    }

    @Test
    void shouldPushExisting() throws Exception {
        final String digest = String.format(
            "%s:%s",
            "sha256",
            "cb8a924afdf0229ef7515d9e5b3024e23b3eb03ddbba287f4a19c6ac90b8d221"
        );
        final String original = String.format("alpine@%s", digest);
        this.run("pull", original);
        final String remote = String.format("%s/my-alpine", this.repo);
        this.run("tag", original, remote);
        final String output = this.run("push", remote);
        MatcherAssert.assertThat(
            output,
            Matchers.allOf(
                new StringContains(false, "beee9f30bc1f: Layer already exists"),
                new StringContains(
                    false,
                    String.format("latest: digest: %s", digest)
                )
            )
        );
    }

    private String run(final String... args) throws Exception {
        final Path stdout = this.temp.resolve(
            String.format("%s-stdout.txt", UUID.randomUUID().toString())
        );
        final List<String> command = ImmutableList.<String>builder()
            .add("docker")
            .add(args)
            .build();
        Logger.debug(this, "Command:\n%s", String.join(" ", command));
        final int code = new ProcessBuilder()
            .directory(this.temp.toFile())
            .command(command)
            .redirectOutput(stdout.toFile())
            .redirectErrorStream(true)
            .start()
            .waitFor();
        final String log = new String(Files.readAllBytes(stdout));
        Logger.debug(this, "Full stdout/stderr:\n%s", log);
        if (code != 0) {
            throw new IllegalStateException(String.format("Not OK exit code: %d", code));
        }
        return log;
    }

    private void ensureDockerInstalled() throws Exception {
        final String output = this.run("--version");
        if (!output.startsWith("Docker version")) {
            throw new IllegalStateException("Docker not installed");
        }
    }
}
