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
import com.artipie.docker.Digest;
import com.artipie.docker.Manifests;
import com.artipie.docker.Tags;
import com.artipie.docker.manifest.JsonManifest;
import com.artipie.docker.manifest.Manifest;
import com.artipie.docker.ref.ManifestRef;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Manifests implementation that contains manifest.
 *
 * @since 0.3
 */
public final class FullGetManifests implements Manifests {

    /**
     * Digest hex of manifest.
     */
    private final String hex;

    /**
     * Manifest content.
     */
    private final String content;

    /**
     * Ctor.
     *
     * @param hex Digest hex of manifest.
     */
    public FullGetManifests(final String hex) {
        this(hex, "");
    }

    /**
     * Ctor.
     *
     * @param hex Digest hex of manifest.
     * @param content Manifest content.
     */
    public FullGetManifests(final String hex, final String content) {
        this.hex = hex;
        this.content = content;
    }

    @Override
    public CompletionStage<Manifest> put(final ManifestRef ref, final Content ignored) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CompletionStage<Optional<Manifest>> get(final ManifestRef ref) {
        return CompletableFuture.completedFuture(
            Optional.of(
                new JsonManifest(
                    new Digest.Sha256(this.hex),
                    this.content.getBytes()
                )
            )
        );
    }

    @Override
    public CompletionStage<Tags> tags() {
        throw new UnsupportedOperationException();
    }
}
