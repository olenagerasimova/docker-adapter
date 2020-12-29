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
import com.artipie.docker.RepoName;
import com.artipie.docker.Uploads;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Multi-read {@link Repo} implementation.
 *
 * @since 0.3
 */
public final class MultiReadRepo implements Repo {

    /**
     * Repository name.
     */
    private final RepoName name;

    /**
     * Repositories for reading.
     */
    private final List<Repo> repos;

    /**
     * Ctor.
     *
     * @param name Repository name.
     * @param repos Repositories for reading.
     */
    public MultiReadRepo(final RepoName name, final List<Repo> repos) {
        this.name = name;
        this.repos = repos;
    }

    @Override
    public Layers layers() {
        return new MultiReadLayers(
            this.repos.stream().map(Repo::layers).collect(Collectors.toList())
        );
    }

    @Override
    public Manifests manifests() {
        return new MultiReadManifests(
            this.name, this.repos.stream().map(Repo::manifests).collect(Collectors.toList())
        );
    }

    @Override
    public Uploads uploads() {
        throw new UnsupportedOperationException();
    }
}
