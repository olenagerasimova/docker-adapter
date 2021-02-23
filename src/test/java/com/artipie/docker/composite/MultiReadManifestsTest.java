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
package com.artipie.docker.composite;

import com.artipie.asto.Content;
import com.artipie.docker.Digest;
import com.artipie.docker.RepoName;
import com.artipie.docker.Tag;
import com.artipie.docker.TestPublisherAs;
import com.artipie.docker.fake.FakeManifests;
import com.artipie.docker.fake.FullTagsManifests;
import com.artipie.docker.manifest.Manifest;
import com.artipie.docker.ref.ManifestRef;
import java.util.Arrays;
import java.util.Optional;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import wtf.g4s8.hamcrest.json.JsonContains;
import wtf.g4s8.hamcrest.json.JsonHas;
import wtf.g4s8.hamcrest.json.JsonValueIs;
import wtf.g4s8.hamcrest.json.StringIsJson;

/**
 * Tests for {@link MultiReadManifests}.
 *
 * @since 0.3
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
final class MultiReadManifestsTest {

    @ParameterizedTest
    @CsvSource({
        "empty,empty,",
        "empty,full,two",
        "full,empty,one",
        "faulty,full,two",
        "full,faulty,one",
        "faulty,empty,",
        "empty,faulty,",
        "full,full,one"
    })
    void shouldReturnExpectedValue(
        final String origin,
        final String cache,
        final String expected
    ) {
        final MultiReadManifests manifests = new MultiReadManifests(
            new RepoName.Simple("test"),
            Arrays.asList(
                new FakeManifests(origin, "one"),
                new FakeManifests(cache, "two")
            )
        );
        MatcherAssert.assertThat(
            manifests.get(new ManifestRef.FromString("ref"))
                .toCompletableFuture().join()
                .map(Manifest::digest)
                .map(Digest::hex),
            new IsEqual<>(Optional.ofNullable(expected))
        );
    }

    @Test
    void loadsTagsFromManifests() {
        final int limit = 3;
        final String name = "tags-test";
        MatcherAssert.assertThat(
            new MultiReadManifests(
                new RepoName.Simple(name),
                Arrays.asList(
                    new FullTagsManifests(
                        () -> new Content.From("{\"tags\":[\"one\",\"three\",\"four\"]}".getBytes())
                    ),
                    new FullTagsManifests(
                        () -> new Content.From("{\"tags\":[\"one\",\"two\"]}".getBytes())
                    )
                )
            ).tags(Optional.of(new Tag.Valid("four")), limit).thenCompose(
                tags -> new TestPublisherAs(tags.json()).asciiString()
            ).toCompletableFuture().join(),
            new StringIsJson.Object(
                Matchers.allOf(
                    new JsonHas("name", new JsonValueIs(name)),
                    new JsonHas(
                        "tags",
                        new JsonContains(
                            new JsonValueIs("one"), new JsonValueIs("three"), new JsonValueIs("two")
                        )
                    )
                )
            )
        );
    }
}
