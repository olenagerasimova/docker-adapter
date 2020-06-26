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
import com.artipie.docker.Blob;
import com.artipie.docker.Digest;
import com.artipie.docker.Layers;
import com.artipie.docker.asto.AstoBlob;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Tests for {@link CacheManifests}.
 *
 * @since 0.3
 */
final class CacheLayersTest {

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
    void shouldReturnExpectedValue(
        final String origin,
        final String cache,
        final boolean expected
    ) {
        MatcherAssert.assertThat(
            new CacheLayers(layers(origin), layers(cache)).get(new Digest.FromString("123"))
                .toCompletableFuture().join()
                .isPresent(),
            new IsEqual<>(expected)
        );
    }

    private static Layers layers(final String type) {
        final Layers layers;
        switch (type) {
            case "empty":
                layers = new EmptyLayers();
                break;
            case "full":
                layers = new FullLayers();
                break;
            case "faulty":
                layers = new FaultyLayers();
                break;
            default:
                throw new IllegalArgumentException(String.format("Unsupported type: %s", type));
        }
        return layers;
    }

    /**
     * Layers implementation that contains no blob.
     *
     * @since 0.3
     */
    private static class EmptyLayers implements Layers {

        @Override
        public CompletionStage<Blob> put(final Content content, final Digest digest) {
            throw new UnsupportedOperationException();
        }

        @Override
        public CompletionStage<Optional<Blob>> get(final Digest digest) {
            return CompletableFuture.completedFuture(Optional.empty());
        }
    }

    /**
     * Layers implementation that contains blob.
     *
     * @since 0.3
     */
    private static class FullLayers implements Layers {

        @Override
        public CompletionStage<Blob> put(final Content content, final Digest digest) {
            throw new UnsupportedOperationException();
        }

        @Override
        public CompletionStage<Optional<Blob>> get(final Digest digest) {
            return CompletableFuture.completedFuture(
                Optional.of(new AstoBlob(new InMemoryStorage(), digest))
            );
        }
    }

    /**
     * Layers implementation that fails to get blob.
     *
     * @since 0.3
     */
    private static class FaultyLayers implements Layers {

        @Override
        public CompletionStage<Blob> put(final Content content, final Digest digest) {
            throw new UnsupportedOperationException();
        }

        @Override
        public CompletionStage<Optional<Blob>> get(final Digest digest) {
            return CompletableFuture.failedFuture(new IllegalStateException());
        }
    }
}
