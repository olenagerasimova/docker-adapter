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
import com.artipie.docker.Manifests;
import com.artipie.docker.Tags;
import com.artipie.docker.manifest.Manifest;
import com.artipie.docker.ref.ManifestRef;
import com.jcabi.log.Logger;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Multi-read {@link Manifests} implementation.
 *
 * @since 0.3
 * @todo #354:30min Implement tags method in MultiReadManifests
 *  `tags` method was added without proper implementation as placeholder.
 *  Method should be implemented and covered with unit tests.
 */
public final class MultiReadManifests implements Manifests {

    /**
     * Manifests for reading.
     */
    private final List<Manifests> manifests;

    /**
     * Ctor.
     *
     * @param manifests Manifests for reading.
     */
    public MultiReadManifests(final List<Manifests> manifests) {
        this.manifests = manifests;
    }

    @Override
    public CompletionStage<Manifest> put(final ManifestRef ref, final Content content) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CompletionStage<Optional<Manifest>> get(final ManifestRef ref) {
        return firstNotEmpty(
            this.manifests.stream().map(
                mnfsts -> mnfsts.get(ref).handle(
                    (manifest, throwable) -> {
                        final CompletableFuture<Optional<Manifest>> result;
                        if (throwable == null) {
                            result = CompletableFuture.completedFuture(manifest);
                        } else {
                            Logger.error(
                                this, "Failed to read manifest %s: %[exception]s",
                                ref.string(),
                                throwable
                            );
                            result = CompletableFuture.completedFuture(Optional.empty());
                        }
                        return result;
                    }
                ).thenCompose(Function.identity())
            ).collect(Collectors.toList())
        );
    }

    @Override
    public CompletionStage<Tags> tags() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a new CompletionStage that is completed when first CompletionStage
     * from the list completes with non-empty result.
     * The result stage may be completed with empty value
     *
     * @param stages Completion stages.
     * @param <T> Result type.
     * @return Completion stage with first non-empty result.
     */
    private static <T> CompletionStage<Optional<T>> firstNotEmpty(
        final List<CompletionStage<Optional<T>>> stages
    ) {
        final CompletableFuture<Optional<T>> promise = new CompletableFuture<>();
        CompletionStage<Void> preceeding = CompletableFuture.allOf();
        for (final CompletionStage<Optional<T>> stage : stages) {
            preceeding = stage.thenCombine(
                preceeding,
                (opt, nothing) -> {
                    if (opt.isPresent()) {
                        promise.complete(opt);
                    }
                    return nothing;
                }
            );
        }
        preceeding.thenRun(() -> promise.complete(Optional.empty()));
        return promise;
    }
}
