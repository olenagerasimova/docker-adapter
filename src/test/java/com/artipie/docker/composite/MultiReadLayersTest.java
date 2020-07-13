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
import com.artipie.docker.Layers;
import com.artipie.docker.fake.EmptyGetLayers;
import com.artipie.docker.fake.FaultyGetLayers;
import com.artipie.docker.fake.FullGetLayers;
import java.util.Arrays;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Tests for {@link MultiReadLayers}.
 *
 * @since 0.3
 */
final class MultiReadLayersTest {

    @ParameterizedTest
    @CsvSource({
        "empty,empty,false",
        "empty,full,true",
        "full,empty,true",
        "faulty,full,true",
        "full,faulty,true",
        "faulty,empty,false",
        "empty,faulty,false"
    })
    void shouldReturnExpectedValue(final String one, final String two, final boolean present) {
        MatcherAssert.assertThat(
            new MultiReadLayers(Arrays.asList(layers(one), layers(two)))
                .get(new Digest.FromString("123"))
                .toCompletableFuture().join()
                .isPresent(),
            new IsEqual<>(present)
        );
    }

    private static Layers layers(final String type) {
        final Layers layers;
        switch (type) {
            case "empty":
                layers = new EmptyGetLayers();
                break;
            case "full":
                layers = new FullGetLayers();
                break;
            case "faulty":
                layers = new FaultyGetLayers();
                break;
            default:
                throw new IllegalArgumentException(String.format("Unsupported type: %s", type));
        }
        return layers;
    }
}
