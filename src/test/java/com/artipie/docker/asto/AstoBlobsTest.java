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
import com.artipie.asto.Key;
import com.artipie.asto.Storage;
import com.artipie.asto.memory.InMemoryStorage;
import com.artipie.docker.Digest;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link AstoBlobs}.
 *
 * @since 0.6
 */
final class AstoBlobsTest {

    @Test
    void shouldNotSaveExistingBlob() {
        final byte[] bytes = new byte[]{0x00, 0x01, 0x02, 0x03};
        final Digest digest = new Digest.Sha256(
            "054edec1d0211f624fed0cbca9d4f9400b0e491c43742af2c5b0abebf0c990d8"
        );
        final FakeStorage storage = new FakeStorage();
        final AstoBlobs blobs = new AstoBlobs(storage);
        blobs.put(new Content.From(bytes), digest).toCompletableFuture().join();
        blobs.put(new Content.From(bytes), digest).toCompletableFuture().join();
        MatcherAssert.assertThat(storage.saves, new IsEqual<>(1));
    }

    /**
     * Fake storage that stores everything in memory and counts save operations.
     *
     * @since 0.6
     */
    private static final class FakeStorage implements Storage {

        /**
         * Origin storage.
         */
        private final Storage origin;

        /**
         * Save operations counter.
         */
        private int saves;

        private FakeStorage() {
            this.origin = new InMemoryStorage();
        }

        @Override
        public CompletableFuture<Boolean> exists(final Key key) {
            return this.origin.exists(key);
        }

        @Override
        public CompletableFuture<Collection<Key>> list(final Key key) {
            return this.origin.list(key);
        }

        @Override
        public CompletableFuture<Void> save(final Key key, final Content content) {
            this.saves += 1;
            return this.origin.save(key, content);
        }

        @Override
        public CompletableFuture<Void> move(final Key source, final Key target) {
            return this.origin.move(source, target);
        }

        @Override
        public CompletableFuture<Long> size(final Key key) {
            return this.origin.size(key);
        }

        @Override
        public CompletableFuture<Content> value(final Key key) {
            return this.origin.value(key);
        }

        @Override
        public CompletableFuture<Void> delete(final Key key) {
            return this.origin.delete(key);
        }

        @Override
        public <T> CompletionStage<T> exclusively(
            final Key key,
            final Function<Storage, CompletionStage<T>> function
        ) {
            return this.origin.exclusively(key, function);
        }
    }
}
