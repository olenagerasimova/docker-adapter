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

import com.artipie.docker.Digest;
import com.artipie.docker.Manifests;
import com.artipie.docker.fake.EmptyGetManifests;
import com.artipie.docker.fake.FaultyGetManifests;
import com.artipie.docker.fake.FullGetManifests;
import com.artipie.docker.manifest.Manifest;
import com.artipie.docker.ref.ManifestRef;
import java.util.Arrays;
import java.util.Optional;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Tests for {@link MultiReadManifests}.
 *
 * @since 0.3
 */
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
            Arrays.asList(manifests(origin, "one"), manifests(cache, "two"))
        );
        MatcherAssert.assertThat(
            manifests.get(new ManifestRef.FromString("ref"))
                .toCompletableFuture().join()
                .map(Manifest::digest)
                .map(Digest::hex),
            new IsEqual<>(Optional.ofNullable(expected))
        );
    }

    private static Manifests manifests(final String type, final String code) {
        final Manifests manifests;
        switch (type) {
            case "empty":
                manifests = new EmptyGetManifests();
                break;
            case "full":
                manifests = new FullGetManifests(code);
                break;
            case "faulty":
                manifests = new FaultyGetManifests();
                break;
            default:
                throw new IllegalArgumentException(String.format("Unsupported type: %s", type));
        }
        return manifests;
    }
}
