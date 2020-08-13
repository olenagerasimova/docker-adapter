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

import com.artipie.asto.Content;
import com.artipie.asto.ext.PublisherAs;
import com.artipie.asto.memory.InMemoryStorage;
import com.artipie.docker.Blob;
import com.artipie.docker.Digest;
import com.artipie.docker.Layers;
import java.util.Optional;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link AstoLayers}.
 *
 * @since 0.3
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
final class AstoLayersTest {

    /**
     * Blobs storage.
     */
    private AstoBlobs blobs;

    /**
     * Layers tested.
     */
    private Layers layers;

    @BeforeEach
    void setUp() {
        final InMemoryStorage storage = new InMemoryStorage();
        this.blobs = new AstoBlobs(storage);
        this.layers = new AstoLayers(this.blobs);
    }

    @Test
    void shouldAddLayer() {
        final byte[] data = "data".getBytes();
        final Digest digest = this.layers.put(new Content.From(data), new Digest.Sha256(data))
            .toCompletableFuture().join().digest();
        final Optional<Blob> found = this.blobs.blob(digest).toCompletableFuture().join();
        MatcherAssert.assertThat(found.isPresent(), new IsEqual<>(true));
        MatcherAssert.assertThat(bytes(found.get()), new IsEqual<>(data));
    }

    @Test
    void shouldReadExistingLayer() {
        final byte[] data = "content".getBytes();
        final Digest digest = this.blobs.put(new Content.From(data), new Digest.Sha256(data))
            .toCompletableFuture().join().digest();
        final Optional<Blob> found = this.layers.get(digest).toCompletableFuture().join();
        MatcherAssert.assertThat(found.isPresent(), new IsEqual<>(true));
        MatcherAssert.assertThat(found.get().digest(), new IsEqual<>(digest));
        MatcherAssert.assertThat(bytes(found.get()), new IsEqual<>(data));
    }

    @Test
    void shouldReadAbsentLayer() {
        final Optional<Blob> found = this.layers.get(
            new Digest.Sha256("0123456789012345678901234567890123456789012345678901234567890123")
        ).toCompletableFuture().join();
        MatcherAssert.assertThat(found.isPresent(), new IsEqual<>(false));
    }

    private static byte[] bytes(final Blob blob) {
        return new PublisherAs(blob.content().toCompletableFuture().join())
            .bytes()
            .toCompletableFuture().join();
    }
}
