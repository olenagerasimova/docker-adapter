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
package com.artipie.docker.http;

import com.artipie.docker.Digest;

/**
 * Docker image info.
 *
 * @since 0.4
 */
public interface Image {

    /**
     * Image name.
     *
     * @return Image name string.
     */
    String name();

    /**
     * Image digest.
     *
     * @return Image digest string.
     */
    String digest();

    /**
     * Full image name in remote registry.
     *
     * @return Full image name string.
     */
    String remote();

    /**
     * Abstract decorator for Image.
     *
     * @since 0.4
     */
    abstract class Wrap implements Image {

        /**
         * Origin image.
         */
        private final Image origin;

        /**
         * Ctor.
         *
         * @param origin Origin image.
         */
        protected Wrap(final Image origin) {
            this.origin = origin;
        }

        @Override
        public final String name() {
            return this.origin.name();
        }

        @Override
        public final String digest() {
            return this.origin.digest();
        }

        @Override
        public final String remote() {
            return this.origin.remote();
        }
    }

    /**
     * Docker image built from something.
     *
     * @since 0.4
     */
    final class From implements Image {

        /**
         * Registry.
         */
        private final String registry;

        /**
         * Image name.
         */
        private final String name;

        /**
         * Manifest digest.
         */
        private final String digest;

        /**
         * Ctor.
         *
         * @param registry Registry.
         * @param name Image name.
         * @param digest Manifest digest.
         */
        public From(final String registry, final String name, final String digest) {
            this.registry = registry;
            this.name = name;
            this.digest = digest;
        }

        @Override
        public String name() {
            return this.name;
        }

        @Override
        public String digest() {
            return this.digest;
        }

        @Override
        public String remote() {
            return String.format("%s/%s@%s", this.registry, this.name, this.digest);
        }
    }

    /**
     * Docker image matching OS.
     *
     * @since 0.4
     */
    final class ForOs extends Image.Wrap {

        /**
         * Ctor.
         */
        public ForOs() {
            super(create());
        }

        /**
         * Create image by host OS.
         *
         * @return Image.
         */
        private static Image create() {
            final Image img;
            if (System.getProperty("os.name").startsWith("Windows")) {
                img = new From(
                    "mcr.microsoft.com",
                    "dotnet/core/runtime",
                    new Digest.Sha256(
                        "c91e7b0fcc21d5ee1c7d3fad7e31c71ed65aa59f448f7dcc1756153c724c8b07"
                    ).string()
                );
            } else {
                img = new Image.From(
                    "registry-1.docker.io",
                    "library/busybox",
                    new Digest.Sha256(
                        "a7766145a775d39e53a713c75b6fd6d318740e70327aaa3ed5d09e0ef33fc3df"
                    ).string()
                );
            }
            return img;
        }
    }
}
