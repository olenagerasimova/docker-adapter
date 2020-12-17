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
import com.artipie.docker.RepoName;
import java.util.stream.Collectors;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.collection.IsEmptyCollection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Test for {@link ParsedCatalog}.
 *
 * @since 0.10
 */
class ParsedCatalogTest {

    @Test
    void parsesNames() {
        MatcherAssert.assertThat(
            new ParsedCatalog(
                () -> new Content.From("{\"repositories\":[\"one\",\"two\"]}".getBytes())
            ).repos().toCompletableFuture().join()
                .stream()
                .map(RepoName::value)
                .collect(Collectors.toList()),
            Matchers.contains("one", "two")
        );
    }

    @Test
    void parsesEmptyRepositories() {
        MatcherAssert.assertThat(
            new ParsedCatalog(
                () -> new Content.From("{\"repositories\":[]}".getBytes())
            ).repos().toCompletableFuture().join()
                .stream()
                .map(RepoName::value)
                .collect(Collectors.toList()),
            new IsEmptyCollection<>()
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "{}", "[]", "123"})
    void failsParsingInvalid(final String json) {
        final ParsedCatalog catalog = new ParsedCatalog(() -> new Content.From(json.getBytes()));
        Assertions.assertThrows(
            Exception.class,
            () -> catalog.repos().toCompletableFuture().join()
        );
    }
}
