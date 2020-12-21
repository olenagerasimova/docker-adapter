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
package com.artipie.docker.misc;

import com.artipie.docker.Catalog;
import com.artipie.docker.Docker;
import com.artipie.docker.RepoName;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

/**
 * Source of catalog built by loading and merging multiple catalogs.
 *
 * @since 0.10
 */
public final class JoinedCatalogSource {

    /**
     * Dockers for reading.
     */
    private final List<Docker> dockers;

    /**
     * From which name to start, exclusive.
     */
    private final Optional<RepoName> from;

    /**
     * Maximum number of names returned.
     */
    private final int limit;

    /**
     * Ctor.
     *
     * @param from From which tag to start, exclusive.
     * @param limit Maximum number of tags returned.
     * @param dockers Registries to load catalogs from.
     */
    public JoinedCatalogSource(
        final Optional<RepoName> from,
        final int limit,
        final Docker... dockers
    ) {
        this(Arrays.asList(dockers), from, limit);
    }

    /**
     * Ctor.
     *
     * @param dockers Registries to load catalogs from.
     * @param from From which tag to start, exclusive.
     * @param limit Maximum number of tags returned.
     */
    public JoinedCatalogSource(
        final List<Docker> dockers,
        final Optional<RepoName> from,
        final int limit
    ) {
        this.dockers = dockers;
        this.from = from;
        this.limit = limit;
    }

    /**
     * Load catalog.
     *
     * @return Catalog.
     */
    public CompletionStage<Catalog> catalog() {
        final List<CompletionStage<List<RepoName>>> all = this.dockers.stream().map(
            docker -> docker.catalog(this.from, this.limit)
                .thenApply(ParsedCatalog::new)
                .thenCompose(ParsedCatalog::repos)
                .exceptionally(err -> Collections.emptyList())
        ).collect(Collectors.toList());
        return CompletableFuture.allOf(all.toArray(new CompletableFuture<?>[0])).thenApply(
            nothing -> all.stream().flatMap(
                stage -> stage.toCompletableFuture().join().stream()
            ).collect(Collectors.toList())
        ).thenApply(names -> new CatalogPage(names, this.from, this.limit));
    }
}
