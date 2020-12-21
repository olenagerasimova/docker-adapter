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

import com.artipie.docker.Catalog;
import com.artipie.docker.Docker;
import com.artipie.docker.Repo;
import com.artipie.docker.RepoName;
import com.artipie.docker.misc.JoinedCatalogSource;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

/**
 * Multi-read {@link Docker} implementation.
 * It delegates all read operations to multiple other {@link Docker} instances.
 * List of Docker instances is prioritized.
 * It means that if more then one of repositories contains an image for given name
 * then image from repository coming first is returned.
 * Write operations are not supported.
 * Might be used to join multiple proxy Dockers into single repository.
 *
 * @since 0.3
 */
public final class MultiReadDocker implements Docker {

    /**
     * Dockers for reading.
     */
    private final List<Docker> dockers;

    /**
     * Ctor.
     *
     * @param dockers Dockers for reading.
     */
    public MultiReadDocker(final Docker... dockers) {
        this(Arrays.asList(dockers));
    }

    /**
     * Ctor.
     *
     * @param dockers Dockers for reading.
     */
    public MultiReadDocker(final List<Docker> dockers) {
        this.dockers = dockers;
    }

    @Override
    public Repo repo(final RepoName name) {
        return new MultiReadRepo(
            this.dockers.stream().map(docker -> docker.repo(name)).collect(Collectors.toList())
        );
    }

    @Override
    public CompletionStage<Catalog> catalog(final Optional<RepoName> from, final int limit) {
        return new JoinedCatalogSource(this.dockers, from, limit).catalog();
    }
}
