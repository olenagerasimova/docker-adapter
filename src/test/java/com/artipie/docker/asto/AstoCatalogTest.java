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
package com.artipie.docker.asto;

import com.artipie.asto.Key;
import com.artipie.asto.ext.PublisherAs;
import com.artipie.docker.RepoName;
import com.google.common.base.Splitter;
import java.io.StringReader;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.json.Json;
import javax.json.JsonReader;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import wtf.g4s8.hamcrest.json.JsonContains;
import wtf.g4s8.hamcrest.json.JsonHas;
import wtf.g4s8.hamcrest.json.JsonValueIs;

/**
 * Tests for {@link AstoCatalog}.
 *
 * @since 0.9
 */
final class AstoCatalogTest {

    /**
     * Tag keys.
     */
    private Collection<Key> keys;

    @BeforeEach
    void setUp() {
        this.keys = Stream.of("foo/my-alpine", "foo/test", "foo/bar", "foo/busybox")
            .map(Key.From::new)
            .collect(Collectors.toList());
    }

    @ParameterizedTest
    @CsvSource({
        ",,bar;busybox;my-alpine;test",
        "busybox,,my-alpine;test",
        "xyz,,''",
        ",2,bar;busybox",
        "bar,2,busybox;my-alpine"
    })
    void shouldSupportPaging(final String from, final Integer limit, final String result) {
        MatcherAssert.assertThat(
            new PublisherAs(
                new AstoCatalog(
                    new Key.From("foo"),
                    this.keys,
                    Optional.ofNullable(from).map(RepoName.Simple::new),
                    Optional.ofNullable(limit).orElse(Integer.MAX_VALUE)
                ).json()
            ).asciiString().thenApply(
                str -> {
                    try (JsonReader reader = Json.createReader(new StringReader(str))) {
                        return reader.readObject();
                    }
                }
            ).toCompletableFuture().join(),
            new JsonHas(
                "repositories",
                new JsonContains(
                    StreamSupport.stream(
                        Splitter.on(";").omitEmptyStrings().split(result).spliterator(),
                        false
                    ).map(JsonValueIs::new).collect(Collectors.toList())
                )
            )
        );
    }
}
