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
import com.artipie.docker.Blob;
import com.artipie.docker.Digest;
import com.artipie.docker.Layers;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ReadWriteLayers}.
 *
 * @since 0.5
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
final class ReadWriteLayersTest {
    /**
     * Layers that implement only get method.
     */
    private CaptureGetLayers getlayers;

    /**
     * Layers that implement only put method.
     */
    private CapturePutLayers putlayers;

    /**
     * ReadWriteLayers.
     */
    private ReadWriteLayers layers;

    @BeforeEach
    void init() {
        this.getlayers = new CaptureGetLayers();
        this.putlayers = new CapturePutLayers();
        this.layers = new ReadWriteLayers(this.getlayers, this.putlayers);
    }

    @Test
    void shouldCallGetWithCorrectRef() {
        final Digest digest = new Digest.FromString("sha256:123");
        this.layers.get(digest).toCompletableFuture().join();
        MatcherAssert.assertThat(
            this.getlayers.digest(),
            new IsEqual<>(digest)
        );
    }

    @Test
    void shouldCallPutPassingCorrectData() {
        final byte[] data = "data".getBytes();
        final Digest digest = new Digest.FromString("sha256:123");
        this.layers.put(
            new Content.From(data),
            digest
        ).toCompletableFuture().join();
        MatcherAssert.assertThat(
            "Digest from put method is wrong.",
            this.putlayers.digest(),
            new IsEqual<>(digest)
        );
        MatcherAssert.assertThat(
            "Size of content from put method is wrong.",
            this.putlayers.content().size().get(),
            new IsEqual<>((long) data.length)
        );
    }

    /**
     * Layers implementation that captures get method for checking
     * correctness of parameters. Put method is unsupported.
     *
     * @since 0.5
     */
    private static class CaptureGetLayers implements Layers {
        /**
         * Layer digest.
         */
        private volatile Digest digestcheck;

        @Override
        public CompletionStage<Blob> put(final Content content, final Digest digest) {
            throw new UnsupportedOperationException();
        }

        @Override
        public CompletionStage<Blob> mount(final Blob blob) {
            throw new UnsupportedOperationException();
        }

        @Override
        public CompletionStage<Optional<Blob>> get(final Digest digest) {
            this.digestcheck = digest;
            return CompletableFuture.completedFuture(Optional.empty());
        }

        public Digest digest() {
            return this.digestcheck;
        }
    }

    /**
     * Layers implementation that captures put method for checking
     * correctness of parameters. Get method is unsupported.
     *
     * @since 0.5
     */
    private static class CapturePutLayers implements Layers {
        /**
         * Layer digest.
         */
        private volatile Digest digestcheck;

        /**
         * Layer content.
         */
        private volatile Content contentcheck;

        @Override
        public CompletionStage<Blob> put(final Content content, final Digest digest) {
            this.digestcheck = digest;
            this.contentcheck = content;
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletionStage<Blob> mount(final Blob blob) {
            throw new UnsupportedOperationException();
        }

        @Override
        public CompletionStage<Optional<Blob>> get(final Digest digest) {
            throw new UnsupportedOperationException();
        }

        public Digest digest() {
            return this.digestcheck;
        }

        public Content content() {
            return this.contentcheck;
        }
    }
}
