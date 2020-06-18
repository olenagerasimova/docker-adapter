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
package com.artipie.docker.junit;

import com.artipie.docker.Docker;
import com.artipie.docker.http.DockerSlice;
import com.artipie.http.slice.LoggingSlice;
import com.artipie.vertx.VertxSliceServer;
import com.jcabi.log.Logger;
import io.vertx.reactivex.core.Vertx;

/**
 * Docker HTTP server, using provided {@link Docker} instance as back-end.
 *
 * @since 0.3
 */
public final class DockerRepository {

    /**
     * Docker back-end.
     */
    private final Docker docker;

    /**
     * Vert.x instance used for running HTTP server.
     */
    private Vertx vertx;

    /**
     * HTTP server instance.
     */
    private VertxSliceServer server;

    /**
     * HTTP server port.
     */
    private int port;

    /**
     * Ctor.
     *
     * @param docker Docker back-end.
     */
    public DockerRepository(final Docker docker) {
        this.docker = docker;
    }

    /**
     * Start the server.
     */
    public void start() {
        this.vertx = Vertx.vertx();
        this.server = new VertxSliceServer(
            this.vertx,
            new LoggingSlice(new DockerSlice(this.docker))
        );
        Logger.debug(this, "Vertx server is created");
        this.port = this.server.start();
        Logger.debug(this, "Vertx server is listening on port %d now", this.port);
    }

    /**
     * Stop the server releasing all resources.
     */
    public void stop() {
        this.port = 0;
        if (this.server != null) {
            this.server.stop();
        }
        Logger.debug(this, "Vertx server is stopped");
        if (this.vertx != null) {
            this.vertx.close();
        }
        Logger.debug(this, "Vertx instance is destroyed");
    }

    /**
     * Server URL.
     *
     * @return Server URL string.
     */
    public String url() {
        if (this.port == 0) {
            throw new IllegalStateException("Server not started");
        }
        return String.format("localhost:%s", this.port);
    }
}
