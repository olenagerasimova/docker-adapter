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
package com.artipie.docker.misc;

import com.artipie.asto.Content;
import com.artipie.asto.ext.PublisherAs;
import com.artipie.docker.RepoName;
import com.artipie.docker.fake.FakeCatalogDocker;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import wtf.g4s8.hamcrest.json.JsonContains;
import wtf.g4s8.hamcrest.json.JsonHas;
import wtf.g4s8.hamcrest.json.JsonValueIs;
import wtf.g4s8.hamcrest.json.StringIsJson;

/**
 * Tests for {@link JoinedCatalogSource}.
 *
 * @since 0.10
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
final class JoinedCatalogSourceTest {

    @Test
    void joinsCatalogs() {
        final int limit = 3;
        MatcherAssert.assertThat(
            new JoinedCatalogSource(
                Stream.of(
                    "{\"repositories\":[\"one\",\"two\"]}",
                    "{\"repositories\":[\"one\",\"three\",\"four\"]}"
                ).map(
                    json -> new FakeCatalogDocker(() -> new Content.From(json.getBytes()))
                ).collect(Collectors.toList()),
                Optional.of(new RepoName.Simple("four")),
                limit
            ).catalog().thenCompose(
                catalog -> new PublisherAs(catalog.json()).asciiString()
            ).toCompletableFuture().join(),
            new StringIsJson.Object(
                new JsonHas(
                    "repositories",
                    new JsonContains(
                        new JsonValueIs("one"), new JsonValueIs("three"), new JsonValueIs("two")
                    )
                )
            )
        );
    }
}
