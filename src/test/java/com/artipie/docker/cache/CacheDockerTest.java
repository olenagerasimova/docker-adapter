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
package com.artipie.docker.cache;

import com.artipie.asto.Content;
import com.artipie.asto.memory.InMemoryStorage;
import com.artipie.docker.RepoName;
import com.artipie.docker.TestPublisherAs;
import com.artipie.docker.asto.AstoDocker;
import com.artipie.docker.fake.FakeCatalogDocker;
import com.artipie.docker.proxy.ProxyDocker;
import com.artipie.http.rs.StandardRs;
import java.util.Optional;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsInstanceOf;
import org.junit.jupiter.api.Test;
import wtf.g4s8.hamcrest.json.JsonContains;
import wtf.g4s8.hamcrest.json.JsonHas;
import wtf.g4s8.hamcrest.json.JsonValueIs;
import wtf.g4s8.hamcrest.json.StringIsJson;

/**
 * Tests for {@link CacheDocker}.
 *
 * @since 0.3
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
final class CacheDockerTest {

    @Test
    void createsCacheRepo() {
        final CacheDocker docker = new CacheDocker(
            new ProxyDocker((line, headers, body) -> StandardRs.EMPTY),
            new AstoDocker(new InMemoryStorage())
        );
        MatcherAssert.assertThat(
            docker.repo(new RepoName.Simple("test")),
            new IsInstanceOf(CacheRepo.class)
        );
    }

    @Test
    void loadsCatalogsFromOriginAndCache() {
        final int limit = 3;
        MatcherAssert.assertThat(
            new CacheDocker(
                fake("{\"repositories\":[\"one\",\"three\",\"four\"]}"),
                fake("{\"repositories\":[\"one\",\"two\"]}")
            ).catalog(Optional.of(new RepoName.Simple("four")), limit).thenCompose(
                catalog -> new TestPublisherAs(catalog.json()).asciiString()
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

    private static FakeCatalogDocker fake(final String catalog) {
        return new FakeCatalogDocker(() -> new Content.From(catalog.getBytes()));
    }
}
