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
import com.artipie.docker.Tag;
import com.artipie.docker.Tags;
import com.artipie.docker.manifest.Manifest;
import com.artipie.docker.ref.ManifestRef;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

/**
 * Read-write {@link Manifests} implementation.
 *
 * @since 0.3
 * @todo #354:30min Implement tags method in ReadWriteManifests
 *  `tags` method was added without proper implementation as placeholder.
 *  Method should be implemented and covered with unit tests.
 */
public final class ReadWriteManifests implements Manifests {

    /**
     * Manifests for reading.
     */
    private final Manifests read;

    /**
     * Manifests for writing.
     */
    private final Manifests write;

    /**
     * Ctor.
     *
     * @param read Manifests for reading.
     * @param write Manifests for writing.
     */
    public ReadWriteManifests(final Manifests read, final Manifests write) {
        this.read = read;
        this.write = write;
    }

    @Override
    public CompletionStage<Manifest> put(final ManifestRef ref, final Content content) {
        return this.write.put(ref, content);
    }

    @Override
    public CompletionStage<Optional<Manifest>> get(final ManifestRef ref) {
        return this.read.get(ref);
    }

    @Override
    public CompletionStage<Tags> tags(final Optional<Tag> from, final int limit) {
        throw new UnsupportedOperationException();
    }
}
