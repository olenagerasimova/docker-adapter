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
package com.artipie.docker.fake;

import com.artipie.docker.Manifests;

/**
 * Auxiliary class for tests for {@link com.artipie.docker.cache.CacheManifests}.
 *
 * @since 0.5
 */
public final class FakeManifests {

    /**
     * Type of manifests.
     */
    private final String type;

    /**
     * Code of manifests.
     */
    private final String code;

    /**
     * Ctor.
     *
     * @param type Type of manifests.
     * @param code Code of manifests.
     */
    public FakeManifests(final String type, final String code) {
        this.type = type;
        this.code = code;
    }

    /**
     * Creates manifests.
     *
     * @return Manifests.
     */
    public Manifests manifests() {
        final Manifests manifests;
        switch (this.type) {
            case "empty":
                manifests = new EmptyGetManifests();
                break;
            case "full":
                manifests = new FullGetManifests(this.code);
                break;
            case "faulty":
                manifests = new FaultyGetManifests();
                break;
            default:
                throw new IllegalArgumentException(
                    String.format("Unsupported type: %s", this.type)
                );
        }
        return manifests;
    }
}
