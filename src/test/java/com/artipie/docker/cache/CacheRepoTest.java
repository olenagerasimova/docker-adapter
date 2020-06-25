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

import com.artipie.asto.memory.InMemoryStorage;
import com.artipie.docker.RepoName;
import com.artipie.docker.asto.AstoDocker;
import com.artipie.docker.proxy.ProxyRepo;
import com.artipie.http.rs.StandardRs;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsInstanceOf;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link CacheRepo}.
 *
 * @since 0.3
 */
final class CacheRepoTest {

    /**
     * Tested {@link CacheRepo}.
     */
    private CacheRepo repo;

    @BeforeEach
    void setUp() {
        this.repo = new CacheRepo(
            new ProxyRepo(
                (line, headers, body) -> StandardRs.EMPTY,
                new RepoName.Simple("test-origin")
            ),
            new AstoDocker(new InMemoryStorage()).repo(new RepoName.Simple("test-cache"))
        );
    }

    @Test
    void createsCacheLayers() {
        MatcherAssert.assertThat(
            this.repo.layers(),
            new IsInstanceOf(CacheLayers.class)
        );
    }

    @Test
    void createsCacheManifests() {
        MatcherAssert.assertThat(
            this.repo.manifests(),
            new IsInstanceOf(CacheManifests.class)
        );
    }
}
