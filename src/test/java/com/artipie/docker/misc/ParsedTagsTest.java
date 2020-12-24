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
import com.artipie.docker.Tag;
import java.util.stream.Collectors;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.collection.IsEmptyCollection;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Test for {@link ParsedTags}.
 *
 * @since 0.10
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class ParsedTagsTest {

    @Test
    void parsesTags() {
        MatcherAssert.assertThat(
            new ParsedTags(
                () -> new Content.From("{\"tags\":[\"one\",\"two\"]}".getBytes())
            ).tags().toCompletableFuture().join()
                .stream()
                .map(Tag::value)
                .collect(Collectors.toList()),
            Matchers.contains("one", "two")
        );
    }

    @Test
    void parsesName() {
        MatcherAssert.assertThat(
            new ParsedTags(
                () -> new Content.From("{\"name\":\"foo\"}".getBytes())
            ).repo().toCompletableFuture().join().value(),
            new IsEqual<>("foo")
        );
    }

    @Test
    void parsesEmptyTags() {
        MatcherAssert.assertThat(
            new ParsedTags(
                () -> new Content.From("{\"tags\":[]}".getBytes())
            ).tags().toCompletableFuture().join(),
            new IsEmptyCollection<>()
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "{}", "[]", "123", "{\"name\":\"foo\"}"})
    void failsParsingTagsFromInvalid(final String json) {
        final ParsedTags catalog = new ParsedTags(() -> new Content.From(json.getBytes()));
        Assertions.assertThrows(
            Exception.class,
            () -> catalog.tags().toCompletableFuture().join()
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "{}", "[]", "123", "{\"tags\":[]}"})
    void failsParsingRepoFromInvalid(final String json) {
        final ParsedTags catalog = new ParsedTags(() -> new Content.From(json.getBytes()));
        Assertions.assertThrows(
            Exception.class,
            () -> catalog.repo().toCompletableFuture().join()
        );
    }
}
