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
import com.artipie.docker.Blob;
import com.artipie.docker.Digest;
import com.artipie.docker.RepoName;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

/**
 * Asto {@link BlobStore} implementation.
 * @since 0.1
 */
public final class AstoBlobs implements BlobStore {

    /**
     * Storage.
     */
    private final Storage asto;

    /**
     * Blobs layout.
     */
    private final BlobsLayout layout;

    /**
     * Repository name.
     */
    private final RepoName name;

    /**
     * Ctor.
     * @param asto Storage
     * @param layout Blobs layout.
     * @param name Repository name.
     */
    public AstoBlobs(final Storage asto, final BlobsLayout layout, final RepoName name) {
        this.asto = asto;
        this.layout = layout;
        this.name = name;
    }

    @Override
    public CompletionStage<Optional<Blob>> blob(final Digest digest) {
        final Key key = this.layout.blob(this.name, digest);
        return this.asto.exists(key).thenApply(
            exists -> {
                final Optional<Blob> blob;
                if (exists) {
                    blob = Optional.of(new AstoBlob(this.asto, key, digest));
                } else {
                    blob = Optional.empty();
                }
                return blob;
            }
        );
    }

    @Override
    public CompletionStage<Blob> put(final Content blob, final Digest digest) {
        return this.put(new CheckedBlobSource(blob, digest));
    }

    @Override
    public CompletionStage<Blob> put(final BlobSource source) {
        final Digest digest = source.digest();
        final Key key = this.layout.blob(this.name, digest);
        return source.saveTo(this.asto, key).thenApply(
            nothing -> new AstoBlob(this.asto, key, digest)
        );
    }
}
