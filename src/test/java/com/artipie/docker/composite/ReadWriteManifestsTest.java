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
import com.artipie.docker.Manifests;
import com.artipie.docker.manifest.Manifest;
import com.artipie.docker.ref.ManifestRef;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests for {@link ReadWriteManifests}.
 *
 * @since 0.5
 */
final class ReadWriteManifestsTest {
    /**
     * Manifest reference.
     */
    private ManifestRef refcheck;

    /**
     * Manifest content.
     */
    private Content contentcheck;

    /**
     * Manifests.
     */
    private Manifests mnfs;

    @BeforeEach
    void init() {
        this.mnfs = new ReadWriteManifests(new ReadManifests(), new WriteManifests());
    }

    @Test
    void shouldCallGetWithCorrectRef() {
        final ManifestRef ref = new ManifestRef.FromString("get");
        this.mnfs.get(ref).toCompletableFuture().join();
        MatcherAssert.assertThat(
            this.refcheck,
            new IsEqual<>(ref)
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"data", "<p>**data**</p>"})
    void shouldCallPutPassingCorrectData(final String str) {
        final byte[] data = str.getBytes();
        final ManifestRef ref = new ManifestRef.FromString(str);
        this.mnfs.put(
            ref,
            new Content.From(data)
        ).toCompletableFuture().join();
        MatcherAssert.assertThat(
            this.refcheck,
            new IsEqual<>(ref)
        );
        MatcherAssert.assertThat(
            this.contentcheck.size().get(),
            new IsEqual<>((long) data.length)
        );
    }

    /**
     * ReadManifests implementation.
     *
     * @since 0.5
     */
    private class ReadManifests implements Manifests {
        @Override
        public CompletionStage<Manifest> put(final ManifestRef ref, final Content content) {
            throw new UnsupportedOperationException();
        }

        @Override
        public CompletionStage<Optional<Manifest>> get(final ManifestRef ref) {
            ReadWriteManifestsTest.this.refcheck = ref;
            return CompletableFuture.completedFuture(Optional.empty());
        }
    }

    /**
     * WriteManifests implementation.
     *
     * @since 0.5
     */
    private class WriteManifests implements Manifests {
        @Override
        public CompletionStage<Manifest> put(final ManifestRef ref, final Content content) {
            ReadWriteManifestsTest.this.refcheck = ref;
            ReadWriteManifestsTest.this.contentcheck = content;
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletionStage<Optional<Manifest>> get(final ManifestRef ref) {
            throw new UnsupportedOperationException();
        }
    }
}
