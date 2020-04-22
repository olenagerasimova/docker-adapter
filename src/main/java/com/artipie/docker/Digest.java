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

/**
 * Content Digest.
 * See <a href="https://docs.docker.com/registry/spec/api/#content-digests">Content Digests</a>
 *
 * @since 0.1
 */
public interface Digest {

    /**
     * Digest algorithm part.
     * @return Algorithm string
     */
    String alg();

    /**
     * Digest hex.
     * @return Link digest hex string
     */
    String hex();

    /**
     * Digest hex.
     * @return Link digest hex string
     */
    default String string() {
        return String.format("%s:%s", this.alg(), this.hex());
    }

    /**
     * SHA256 digest implementation.
     * @since 0.1
     */
    final class Sha256 implements Digest {

        /**
         * SHA256 hex string.
         */
        private final String hex;

        /**
         * Ctor.
         * @param hex SHA256 hex string
         */
        public Sha256(final String hex) {
            this.hex = hex;
        }

        @Override
        public String alg() {
            return "sha256";
        }

        @Override
        public String hex() {
            return this.hex;
        }

        @Override
        public String toString() {
            return this.string();
        }
    }

    /**
     * Digest parsed from string.
     * <p>
     * See <a href="https://docs.docker.com/registry/spec/api/#content-digests">Content Digests</a>
     * <p>
     * Docker registry digest is a string with digest formatted
     * by joining algorithm name with hex string using {@code :} as separator.
     * E.g. if algorithm is {@code sha256} and the digest is {@code 0000}, the link will be
     * {@code sha256:0000}.
     * @since 0.1
     */
    final class FromString implements Digest {

        /**
         * Digest string.
         */
        private final String original;

        /**
         * Ctor.
         *
         * @param original Digest string.
         */
        public FromString(final String original) {
            this.original = original;
        }

        @Override
        public String alg() {
            return this.part(0);
        }

        @Override
        public String hex() {
            return this.part(1);
        }

        @Override
        public String toString() {
            return this.string();
        }

        /**
         * Validates digest string.
         *
         * @return True if string is valid digest, false otherwise.
         */
        public boolean valid() {
            return this.original.split(":").length == 2;
        }

        /**
         * Part from input string split by {@code :}.
         * @param pos Part position
         * @return Part
         */
        private String part(final int pos) {
            if (!this.valid()) {
                throw new IllegalStateException(
                    String.format(
                        "Expected two parts separated by `:`, but was `%s`", this.original
                    )
                );
            }
            return this.original.split(":")[pos];
        }
    }
}
