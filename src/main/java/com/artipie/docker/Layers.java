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

package com.artipie.docker;

import com.artipie.docker.asto.BlobSource;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

/**
 * Docker repository files and metadata.
 *
 * @since 0.3
 */
public interface Layers {

    /**
     * Add layer to repository.
     *
     * @param source Blob source.
     * @return Added layer blob.
     */
    CompletionStage<Blob> put(BlobSource source);

    /**
     * Mount blob to repository.
     *
     * @param blob Blob.
     * @return Mounted blob.
     */
    CompletionStage<Blob> mount(Blob blob);

    /**
     * Find layer by digest.
     *
     * @param digest Layer digest.
     * @return Flow with manifest data, or empty if absent
     */
    CompletionStage<Optional<Blob>> get(Digest digest);

    /**
     * Abstract decorator for Layers.
     *
     * @since 0.3
     */
    abstract class Wrap implements Layers {

        /**
         * Origin layers.
         */
        private final Layers layers;

        /**
         * Ctor.
         *
         * @param layers Layers.
         */
        protected Wrap(final Layers layers) {
            this.layers = layers;
        }

        @Override
        public final CompletionStage<Blob> put(final BlobSource source) {
            return this.layers.put(source);
        }

        @Override
        public final CompletionStage<Optional<Blob>> get(final Digest digest) {
            return this.layers.get(digest);
        }
    }
}
