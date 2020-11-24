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

import com.artipie.asto.memory.InMemoryStorage;
import com.artipie.docker.Layers;
import com.artipie.docker.Manifests;
import com.artipie.docker.Repo;
import com.artipie.docker.RepoName;
import com.artipie.docker.Uploads;
import com.artipie.docker.asto.AstoRepo;
import com.artipie.docker.asto.AstoUploads;
import com.artipie.docker.asto.DefaultLayout;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsInstanceOf;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ReadWriteRepo}.
 *
 * @since 0.3
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
final class ReadWriteRepoTest {

    @Test
    void createsReadWriteLayers() {
        MatcherAssert.assertThat(
            new ReadWriteRepo(repo(), repo()).layers(),
            new IsInstanceOf(ReadWriteLayers.class)
        );
    }

    @Test
    void createsReadWriteManifests() {
        MatcherAssert.assertThat(
            new ReadWriteRepo(repo(), repo()).manifests(),
            new IsInstanceOf(ReadWriteManifests.class)
        );
    }

    @Test
    void createsWriteUploads() {
        final Uploads uploads = new AstoUploads(
            new InMemoryStorage(),
            new DefaultLayout(),
            new RepoName.Simple("test")
        );
        MatcherAssert.assertThat(
            new ReadWriteRepo(
                repo(),
                new Repo() {
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
                        return uploads;
                    }
                }
            ).uploads(),
            new IsEqual<>(uploads)
        );
    }

    private static Repo repo() {
        return new AstoRepo(
            new InMemoryStorage(),
            new DefaultLayout(),
            new RepoName.Simple("test-repo")
        );
    }
}
