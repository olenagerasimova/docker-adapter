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

import com.artipie.docker.Manifests;
import com.artipie.docker.RepoName;
import com.artipie.docker.Tag;
import com.artipie.docker.Tags;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

/**
 * Source of tags built by loading and merging multiple tag lists.
 *
 * @since 0.10
 */
public final class JoinedTagsSource {

    /**
     * Repository name.
     */
    private final RepoName repo;

    /**
     * Manifests for reading.
     */
    private final List<Manifests> manifests;

    /**
     * From which tag to start, exclusive.
     */
    private final Optional<Tag> from;

    /**
     * Maximum number of tags returned.
     */
    private final int limit;

    /**
     * Ctor.
     *
     * @param repo Repository name.
     * @param from From which tag to start, exclusive.
     * @param limit Maximum number of tags returned.
     * @param manifests Sources to load tags from.
     * @checkstyle ParameterNumberCheck (2 lines)
     */
    public JoinedTagsSource(
        final RepoName repo,
        final Optional<Tag> from,
        final int limit,
        final Manifests... manifests
    ) {
        this(repo, Arrays.asList(manifests), from, limit);
    }

    /**
     * Ctor.
     *
     * @param repo Repository name.
     * @param manifests Sources to load tags from.
     * @param from From which tag to start, exclusive.
     * @param limit Maximum number of tags returned.
     * @checkstyle ParameterNumberCheck (2 lines)
     */
    public JoinedTagsSource(
        final RepoName repo,
        final List<Manifests> manifests,
        final Optional<Tag> from,
        final int limit
    ) {
        this.repo = repo;
        this.manifests = manifests;
        this.from = from;
        this.limit = limit;
    }

    /**
     * Load tags.
     *
     * @return Tags.
     */
    public CompletionStage<Tags> tags() {
        final List<CompletionStage<List<Tag>>> all = this.manifests.stream().map(
            mnfsts -> mnfsts.tags(this.from, this.limit)
                .thenApply(ParsedTags::new)
                .thenCompose(ParsedTags::tags)
                .exceptionally(err -> Collections.emptyList())
        ).collect(Collectors.toList());
        return CompletableFuture.allOf(all.toArray(new CompletableFuture<?>[0])).thenApply(
            nothing -> all.stream().flatMap(
                stage -> stage.toCompletableFuture().join().stream()
            ).collect(Collectors.toList())
        ).thenApply(names -> new TagsPage(this.repo, names, this.from, this.limit));
    }
}
