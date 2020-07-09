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

import com.artipie.docker.Docker;
import com.artipie.docker.Repo;
import com.artipie.docker.RepoName;

/**
 * Read-write {@link Docker} implementation.
 * It delegates read operation to one {@link Docker} and writes {@link Docker} to another.
 * This class can be used to create virtual repository
 * by composing {@link com.artipie.docker.proxy.ProxyDocker}
 * and {@link com.artipie.docker.asto.AstoDocker}.
 *
 * @since 0.3
 */
public final class ReadWriteDocker implements Docker {

    /**
     * Docker for reading.
     */
    private final Docker read;

    /**
     * Docker for writing.
     */
    private final Docker write;

    /**
     * Ctor.
     *
     * @param read Docker for reading.
     * @param write Docker for writing.
     */
    public ReadWriteDocker(final Docker read, final Docker write) {
        this.read = read;
        this.write = write;
    }

    @Override
    public Repo repo(final RepoName name) {
        return new ReadWriteRepo(this.read.repo(name), this.write.repo(name));
    }
}
