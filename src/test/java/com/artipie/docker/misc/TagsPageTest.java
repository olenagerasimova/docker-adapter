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

import com.artipie.asto.ext.PublisherAs;
import com.artipie.docker.RepoName;
import com.artipie.docker.Tag;
import com.google.common.base.Splitter;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import wtf.g4s8.hamcrest.json.JsonContains;
import wtf.g4s8.hamcrest.json.JsonHas;
import wtf.g4s8.hamcrest.json.JsonValueIs;
import wtf.g4s8.hamcrest.json.StringIsJson;

/**
 * Tests for {@link TagsPage}.
 *
 * @since 0.10
 */
final class TagsPageTest {

    /**
     * Tags.
     */
    private Collection<Tag> tags;

    @BeforeEach
    void setUp() {
        this.tags = Stream.of("3", "1", "2", "4", "5", "4")
            .map(Tag.Valid::new)
            .collect(Collectors.toList());
    }

    @ParameterizedTest
    @CsvSource({
        ",,1;2;3;4;5",
        "2,,3;4;5",
        "7,,''",
        ",2,1;2",
        "2,2,3;4"
    })
    void shouldSupportPaging(final String from, final Integer limit, final String result) {
        final String repo = "my-alpine";
        MatcherAssert.assertThat(
            new PublisherAs(
                new TagsPage(
                    new RepoName.Simple(repo),
                    this.tags,
                    Optional.ofNullable(from).map(Tag.Valid::new),
                    Optional.ofNullable(limit).orElse(Integer.MAX_VALUE)
                ).json()
            ).asciiString().toCompletableFuture().join(),
            new StringIsJson.Object(
                Matchers.allOf(
                    new JsonHas("name", new JsonValueIs(repo)),
                    new JsonHas(
                        "tags",
                        new JsonContains(
                            StreamSupport.stream(
                                Splitter.on(";").omitEmptyStrings().split(result).spliterator(),
                                false
                            ).map(JsonValueIs::new).collect(Collectors.toList())
                        )
                    )
                )
            )
        );
    }
}
