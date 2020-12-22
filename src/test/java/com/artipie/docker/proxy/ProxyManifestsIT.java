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

import com.artipie.asto.ext.PublisherAs;
import com.artipie.docker.RepoName;
import com.artipie.docker.Tags;
import com.artipie.http.client.Settings;
import com.artipie.http.client.jetty.JettyClientSlices;
import java.util.Optional;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.core.IsAnything;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import wtf.g4s8.hamcrest.json.JsonHas;
import wtf.g4s8.hamcrest.json.JsonValueIs;
import wtf.g4s8.hamcrest.json.StringIsJson;

/**
 * Integration tests for {@link ProxyManifests}.
 *
 * @since 0.10
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
final class ProxyManifestsIT {

    /**
     * HTTP client used for proxy.
     */
    private JettyClientSlices client;

    @BeforeEach
    void setUp() throws Exception {
        this.client = new JettyClientSlices(new Settings.WithFollowRedirects(true));
        this.client.start();
    }

    @AfterEach
    void tearDown() throws Exception {
        this.client.stop();
    }

    @Test
    void readsTags() {
        final String repo = "dotnet/runtime";
        MatcherAssert.assertThat(
            new ProxyManifests(
                this.client.https("mcr.microsoft.com"),
                new RepoName.Simple(repo)
            ).tags(Optional.empty(), Integer.MAX_VALUE)
                .thenApply(Tags::json)
                .thenApply(PublisherAs::new)
                .thenCompose(PublisherAs::asciiString)
                .toCompletableFuture().join(),
            new StringIsJson.Object(
                Matchers.allOf(
                    new JsonHas("name", new JsonValueIs(repo)),
                    new JsonHas("tags", new IsAnything<>())
                )
            )
        );
    }
}
