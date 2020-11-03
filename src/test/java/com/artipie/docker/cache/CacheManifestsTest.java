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

import com.artipie.asto.LoggingStorage;
import com.artipie.asto.memory.InMemoryStorage;
import com.artipie.docker.Digest;
import com.artipie.docker.ExampleStorage;
import com.artipie.docker.Layers;
import com.artipie.docker.Manifests;
import com.artipie.docker.Repo;
import com.artipie.docker.RepoName;
import com.artipie.docker.Tag;
import com.artipie.docker.Uploads;
import com.artipie.docker.asto.AstoDocker;
import com.artipie.docker.fake.FakeManifests;
import com.artipie.docker.manifest.Manifest;
import com.artipie.docker.ref.ManifestRef;
import com.google.common.base.Stopwatch;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Tests for {@link CacheManifests}.
 *
 * @since 0.3
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
final class CacheManifestsTest {
    @ParameterizedTest
    @CsvSource({
        "empty,empty,",
        "empty,full,cache",
        "full,empty,origin",
        "faulty,full,cache",
        "full,faulty,origin",
        "faulty,empty,",
        "empty,faulty,",
        "full,full,origin"
    })
    void shouldReturnExpectedValue(
        final String origin,
        final String cache,
        final String expected
    ) {
        final CacheManifests manifests = new CacheManifests(
            new SimpleRepo(new FakeManifests(origin, "origin")),
            new SimpleRepo(new FakeManifests(cache, "cache"))
        );
        MatcherAssert.assertThat(
            manifests.get(new ManifestRef.FromString("ref"))
                .toCompletableFuture().join()
                .map(Manifest::digest)
                .map(Digest::hex),
            new IsEqual<>(Optional.ofNullable(expected))
        );
    }

    @Test
    void shouldCacheManifest() throws Exception {
        final ManifestRef ref = new ManifestRef.FromTag(new Tag.Valid("1"));
        final Repo cache = new AstoDocker(new LoggingStorage(new InMemoryStorage()))
            .repo(new RepoName.Simple("my-cache"));
        new CacheManifests(
            new AstoDocker(new ExampleStorage()).repo(new RepoName.Simple("my-alpine")),
            cache
        ).get(ref).toCompletableFuture().join();
        final Stopwatch stopwatch = Stopwatch.createStarted();
        while (!cache.manifests().get(ref).toCompletableFuture().join().isPresent()) {
            final int timeout = 10;
            if (stopwatch.elapsed(TimeUnit.SECONDS) > timeout) {
                break;
            }
            final int pause = 100;
            Thread.sleep(pause);
        }
        MatcherAssert.assertThat(
            String.format(
                "Manifest is expected to be present, but it was not found after %s seconds",
                stopwatch.elapsed(TimeUnit.SECONDS)
            ),
            cache.manifests().get(ref).toCompletableFuture().join().isPresent(),
            new IsEqual<>(true)
        );
    }

    /**
     * Simple repo implementation.
     *
     * @since 0.3
     */
    private static final class SimpleRepo implements Repo {
        /**
         * Manifests.
         */
        private final Manifests mnfs;

        /**
         * Ctor.
         *
         * @param mnfs Manifests.
         */
        private SimpleRepo(final Manifests mnfs) {
            this.mnfs = mnfs;
        }

        @Override
        public Layers layers() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Manifests manifests() {
            return this.mnfs;
        }

        @Override
        public Uploads uploads() {
            throw new UnsupportedOperationException();
        }
    }
}
