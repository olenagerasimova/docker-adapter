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

import com.artipie.docker.Catalog;
import com.artipie.docker.TestPublisherAs;
import com.artipie.http.client.Settings;
import com.artipie.http.client.jetty.JettyClientSlices;
import java.util.Optional;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsAnything;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import wtf.g4s8.hamcrest.json.JsonHas;
import wtf.g4s8.hamcrest.json.StringIsJson;

/**
 * Integration tests for {@link ProxyDocker}.
 *
 * @since 0.10
 */
final class ProxyDockerIT {

    /**
     * HTTP client used for proxy.
     */
    private JettyClientSlices client;

    /**
     * Proxy docker.
     */
    private ProxyDocker docker;

    @BeforeEach
    void setUp() throws Exception {
        this.client = new JettyClientSlices(new Settings.WithFollowRedirects(true));
        this.client.start();
        this.docker = new ProxyDocker(this.client.https("mcr.microsoft.com"));
    }

    @AfterEach
    void tearDown() throws Exception {
        this.client.stop();
    }

    @Test
    void readsCatalog() {
        MatcherAssert.assertThat(
            this.docker.catalog(Optional.empty(), Integer.MAX_VALUE)
                .thenApply(Catalog::json)
                .thenApply(TestPublisherAs::new)
                .thenCompose(TestPublisherAs::asciiString)
                .toCompletableFuture().join(),
            new StringIsJson.Object(new JsonHas("repositories", new IsAnything<>()))
        );
    }
}
