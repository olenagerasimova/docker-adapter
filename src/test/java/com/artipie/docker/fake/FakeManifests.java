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

import com.artipie.asto.Content;
import com.artipie.docker.Manifests;
import com.artipie.docker.Tag;
import com.artipie.docker.Tags;
import com.artipie.docker.manifest.Manifest;
import com.artipie.docker.ref.ManifestRef;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

/**
 * Auxiliary class for tests for {@link com.artipie.docker.cache.CacheManifests}.
 *
 * @since 0.5
 */
public final class FakeManifests implements Manifests {
    /**
     * Manifests.
     */
    private final Manifests mnfs;

    /**
     * Ctor.
     *
     * @param type Type of manifests.
     * @param code Code of manifests.
     */
    public FakeManifests(final String type, final String code) {
        this.mnfs = manifests(type, code);
    }

    @Override
    public CompletionStage<Manifest> put(final ManifestRef ref, final Content content) {
        return this.mnfs.put(ref, content);
    }

    @Override
    public CompletionStage<Optional<Manifest>> get(final ManifestRef ref) {
        return this.mnfs.get(ref);
    }

    @Override
    public CompletionStage<Tags> tags(final Optional<Tag> from, final int limit) {
        return this.mnfs.tags(from, limit);
    }

    /**
     * Creates manifests.
     *
     * @param type Type of manifests.
     * @param code Code of manifests.
     * @return Manifests.
     */
    private static Manifests manifests(final String type, final String code) {
        final Manifests manifests;
        switch (type) {
            case "empty":
                manifests = new EmptyGetManifests();
                break;
            case "full":
                manifests = new FullGetManifests(code);
                break;
            case "faulty":
                manifests = new FaultyGetManifests();
                break;
            default:
                throw new IllegalArgumentException(
                    String.format("Unsupported type: %s", type)
                );
        }
        return manifests;
    }
}
