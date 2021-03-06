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

package com.artipie.docker.ref;

import com.artipie.asto.Key;
import com.artipie.docker.Digest;
import com.artipie.docker.Tag;
import java.util.Arrays;

/**
 * Manifest reference.
 * <p>
 * Can be resolved by image tag or digest.
 * </p>
 *
 * @since 0.1
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public interface ManifestRef {

    /**
     * Builds key for manifest blob link.
     *
     * @return Key to link.
     */
    Key link();

    /**
     * String representation.
     *
     * @return Reference as string.
     */
    String string();

    /**
     * Manifest reference from {@link Digest}.
     *
     * @since 0.2
     */
    final class FromDigest implements ManifestRef {

        /**
         * Digest.
         */
        private final Digest digest;

        /**
         * Ctor.
         *
         * @param digest Digest.
         */
        public FromDigest(final Digest digest) {
            this.digest = digest;
        }

        @Override
        public Key link() {
            return new Key.From(
                Arrays.asList("revisions", this.digest.alg(), this.digest.hex(), "link")
            );
        }

        @Override
        public String string() {
            return this.digest.string();
        }
    }

    /**
     * Manifest reference from {@link Tag}.
     *
     * @since 0.2
     */
    final class FromTag implements ManifestRef {

        /**
         * Tag.
         */
        private final Tag tag;

        /**
         * Ctor.
         *
         * @param tag Tag.
         */
        public FromTag(final Tag tag) {
            this.tag = tag;
        }

        @Override
        public Key link() {
            return new Key.From(
                Arrays.asList("tags", this.tag.value(), "current", "link")
            );
        }

        @Override
        public String string() {
            return this.tag.value();
        }
    }

    /**
     * Manifest reference from a string.
     * <p>
     * String may be tag or digest.
     *
     * @since 0.2
     */
    final class FromString implements ManifestRef {

        /**
         * Manifest reference string.
         */
        private final String value;

        /**
         * Ctor.
         *
         * @param value Manifest reference string.
         */
        public FromString(final String value) {
            this.value = value;
        }

        @Override
        public Key link() {
            final ManifestRef ref;
            final Digest.FromString digest = new Digest.FromString(this.value);
            final Tag.Valid tag = new Tag.Valid(this.value);
            if (digest.valid()) {
                ref = new ManifestRef.FromDigest(digest);
            } else if (tag.valid()) {
                ref = new ManifestRef.FromTag(tag);
            } else {
                throw new IllegalStateException(
                    String.format("Unsupported reference: `%s`", this.value)
                );
            }
            return ref.link();
        }

        @Override
        public String string() {
            return this.value;
        }
    }
}

