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
package com.artipie.docker.cache;

import com.artipie.docker.Catalog;
import com.artipie.docker.Docker;
import com.artipie.docker.Repo;
import com.artipie.docker.RepoName;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

/**
 * Cache {@link Docker} implementation.
 *
 * @since 0.3
 * @todo #354:30min Implement catalog method in CacheDocker
 *  `catalog` method was added without proper implementation as placeholder.
 *  Method should be implemented and covered with unit tests.
 */
public final class CacheDocker implements Docker {

    /**
     * Origin repository.
     */
    private final Docker origin;

    /**
     * Cache repository.
     */
    private final Docker cache;

    /**
     * Ctor.
     *
     * @param origin Origin repository.
     * @param cache Cache repository.
     */
    public CacheDocker(final Docker origin, final Docker cache) {
        this.origin = origin;
        this.cache = cache;
    }

    @Override
    public Repo repo(final RepoName name) {
        return new CacheRepo(this.origin.repo(name), this.cache.repo(name));
    }

    @Override
    public CompletionStage<Catalog> catalog(final Optional<RepoName> from, final int limit) {
        throw new UnsupportedOperationException();
    }
}
