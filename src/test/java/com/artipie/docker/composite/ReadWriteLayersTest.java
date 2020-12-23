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
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ReadWriteLayers}.
 *
 * @since 0.5
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
final class ReadWriteLayersTest {

    @Test
    void shouldCallGetWithCorrectRef() {
        final Digest digest = new Digest.FromString("sha256:123");
        final CaptureGetLayers fake = new CaptureGetLayers();
        new ReadWriteLayers(fake, new CapturePutLayers()).get(digest).toCompletableFuture().join();
        MatcherAssert.assertThat(
            fake.digest(),
            new IsEqual<>(digest)
        );
    }

    @Test
    void shouldCallPutPassingCorrectData() {
        final byte[] data = "data".getBytes();
        final Digest digest = new Digest.FromString("sha256:123");
        final CapturePutLayers fake = new CapturePutLayers();
        new ReadWriteLayers(new CaptureGetLayers(), fake).put(
            new Content.From(data),
            digest
        ).toCompletableFuture().join();
        MatcherAssert.assertThat(
            "Digest from put method is wrong.",
            fake.digest(),
            new IsEqual<>(digest)
        );
        MatcherAssert.assertThat(
            "Size of content from put method is wrong.",
            fake.content().size().get(),
            new IsEqual<>((long) data.length)
        );
    }

    @Test
    void shouldCallMountPassingCorrectData() {
        final Blob original = new FakeBlob();
        final Blob mounted = new FakeBlob();
        final CaptureMountLayers fake = new CaptureMountLayers(mounted);
        final Blob result = new ReadWriteLayers(new CaptureGetLayers(), fake).mount(original)
            .toCompletableFuture().join();
        MatcherAssert.assertThat(
            "Original blob is captured",
            fake.capturedBlob(),
            new IsEqual<>(original)
        );
        MatcherAssert.assertThat(
            "Mounted blob is returned",
            result,
            new IsEqual<>(mounted)
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

    /**
     * Layers implementation that captures mount method and returns specified blob.
     * Other methods are not supported.
     *
     * @since 0.10
     */
    private static final class CaptureMountLayers implements Layers {

        /**
         * Blob that is returned by mount method.
         */
        private final Blob rblob;

        /**
         * Captured blob.
         */
        private volatile Blob cblob;

        private CaptureMountLayers(final Blob rblob) {
            this.rblob = rblob;
        }

        @Override
        public CompletionStage<Blob> put(final Content content, final Digest digest) {
            throw new UnsupportedOperationException();
        }

        @Override
        public CompletionStage<Blob> mount(final Blob pblob) {
            this.cblob = pblob;
            return CompletableFuture.completedFuture(this.rblob);
        }

        @Override
        public CompletionStage<Optional<Blob>> get(final Digest digest) {
            throw new UnsupportedOperationException();
        }

        public Blob capturedBlob() {
            return this.cblob;
        }
    }

    /**
     * Blob without any implementation.
     *
     * @since 0.10
     */
    private static final class FakeBlob implements Blob {

        @Override
        public Digest digest() {
            throw new UnsupportedOperationException();
        }

        @Override
        public CompletionStage<Long> size() {
            throw new UnsupportedOperationException();
        }

        @Override
        public CompletionStage<Content> content() {
            throw new UnsupportedOperationException();
        }
    }
}
