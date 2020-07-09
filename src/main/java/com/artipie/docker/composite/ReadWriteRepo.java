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

import com.artipie.docker.Layers;
import com.artipie.docker.Manifests;
import com.artipie.docker.Repo;
import com.artipie.docker.Uploads;

/**
 * Read-write {@link Repo} implementation.
 *
 * @since 0.3
 */
@SuppressWarnings({"PMD.UnusedPrivateField", "PMD.SingularField"})
public final class ReadWriteRepo implements Repo {

    /**
     * Repository for reading.
     */
    private final Repo read;

    /**
     * Repository for writing.
     */
    private final Repo write;

    /**
     * Ctor.
     *
     * @param read Repository for reading.
     * @param write Repository for writing.
     */
    public ReadWriteRepo(final Repo read, final Repo write) {
        this.read = read;
        this.write = write;
    }

    @Override
    public Layers layers() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Manifests manifests() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Uploads uploads() {
        throw new UnsupportedOperationException();
    }
}
