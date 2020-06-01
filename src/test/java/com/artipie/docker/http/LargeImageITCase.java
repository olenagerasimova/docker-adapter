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

import com.artipie.asto.fs.FileStorage;
import com.artipie.docker.asto.AstoDocker;
import com.artipie.vertx.VertxSliceServer;
import com.google.common.collect.ImmutableList;
import com.jcabi.log.Logger;
import io.vertx.reactivex.core.Vertx;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

/**
 * Integration test for large file pushing scenario of {@link DockerSlice}.
 *
 * @since 0.3
 * @todo #152:45 min Extract common methods with {@link DockerSliceITCase} to parent abstract
 *  class or create JUnit extension.
*/
@DisabledOnOs(OS.WINDOWS)
public final class LargeImageITCase {
    /**
     * Docker image name.
     */
    private static final String IMAGE = "large-image";

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

    @Test
    public void largeImageUploadWorks() throws Exception {
        final Path dockerfile = Path.of(
            Objects.requireNonNull(
                Thread.currentThread().getContextClassLoader()
                    .getResource("large-image/Dockerfile")
            ).toURI()
        );
        Files.copy(dockerfile, this.temp.resolve("Dockerfile"));
        final String image = String.format("%s/%s", this.repo, LargeImageITCase.IMAGE);
        try {
            this.run("build", ".", "-t", image);
            this.run("push", image);
        } finally {
            this.run("rmi", image);
        }
    }

    @BeforeEach
    void setUp() {
        this.vertx = Vertx.vertx();
        this.server = new VertxSliceServer(
            this.vertx,
            new DockerSlice(
                new AstoDocker(
                    new FileStorage(this.temp, this.vertx.fileSystem())
                )
            )
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
}
