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
import com.artipie.asto.Storage;
import com.artipie.docker.Blob;
import com.artipie.docker.Digest;
import java.util.concurrent.CompletionStage;

/**
 * Asto implementation of {@link Blob}.
 *
 * @since 0.2
 */
public final class AstoBlob implements Blob {

    /**
     * Storage.
     */
    private final Storage storage;

    /**
     * Blob digest.
     */
    private final Digest dig;

    /**
     * Ctor.
     *
     * @param storage Storage.
     * @param digest Blob digest.
     */
    public AstoBlob(final Storage storage, final Digest digest) {
        this.storage = storage;
        this.dig = digest;
    }

    @Override
    public Digest digest() {
        return this.dig;
    }

    @Override
    public CompletionStage<Long> size() {
        return this.storage.size(new BlobKey(this.dig));
    }

    @Override
    public CompletionStage<Content> content() {
        return this.storage.value(new BlobKey(this.dig));
    }
}
