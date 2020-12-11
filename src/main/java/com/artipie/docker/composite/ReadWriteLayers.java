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
import com.artipie.docker.Blob;
import com.artipie.docker.Digest;
import com.artipie.docker.Layers;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

/**
 * Read-write {@link Layers} implementation.
 *
 * @since 0.3
 * @todo #390:30min Implement tags method in ReadWriteLayers
 *  `mount` method was added without proper implementation as placeholder.
 *  Method should be implemented and covered with unit tests.
 */
public final class ReadWriteLayers implements Layers {

    /**
     * Layers for reading.
     */
    private final Layers read;

    /**
     * Layers for writing.
     */
    private final Layers write;

    /**
     * Ctor.
     *
     * @param read Layers for reading.
     * @param write Layers for writing.
     */
    public ReadWriteLayers(final Layers read, final Layers write) {
        this.read = read;
        this.write = write;
    }

    @Override
    public CompletionStage<Blob> put(final Content content, final Digest digest) {
        return this.write.put(content, digest);
    }

    @Override
    public CompletionStage<Blob> mount(final Blob blob) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CompletionStage<Optional<Blob>> get(final Digest digest) {
        return this.read.get(digest);
    }
}
