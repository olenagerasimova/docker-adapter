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
import com.artipie.asto.memory.InMemoryStorage;
import com.artipie.docker.Catalog;
import com.artipie.docker.Docker;
import com.artipie.docker.Repo;
import com.artipie.docker.RepoName;
import com.artipie.docker.asto.AstoDocker;
import com.artipie.docker.proxy.ProxyDocker;
import com.artipie.http.rs.StandardRs;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsInstanceOf;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ReadWriteDocker}.
 *
 * @since 0.3
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
final class ReadWriteDockerTest {

    @Test
    void createsReadWriteRepo() {
        final ReadWriteDocker docker = new ReadWriteDocker(
            new ProxyDocker((line, headers, body) -> StandardRs.EMPTY),
            new AstoDocker(new InMemoryStorage())
        );
        MatcherAssert.assertThat(
            docker.repo(new RepoName.Simple("test")),
            new IsInstanceOf(ReadWriteRepo.class)
        );
    }

    @Test
    void delegatesCatalog() {
        final Optional<RepoName> from = Optional.of(new RepoName.Simple("foo"));
        final int limit = 123;
        final Catalog catalog = () -> new Content.From("{...}".getBytes());
        final FakeDocker fake = new FakeDocker(catalog);
        final ReadWriteDocker docker = new ReadWriteDocker(
            fake,
            new AstoDocker(new InMemoryStorage())
        );
        final Catalog result = docker.catalog(from, limit).toCompletableFuture().join();
        MatcherAssert.assertThat(
            "Forwards from",
            fake.from.get(),
            new IsEqual<>(from)
        );
        MatcherAssert.assertThat(
            "Forwards limit",
            fake.limit.get(),
            new IsEqual<>(limit)
        );
        MatcherAssert.assertThat(
            "Returns catalog",
            result,
            new IsEqual<>(catalog)
        );
    }

    /**
     * Docker implementation with specified catalog.
     * Values of parameters `from` and `limit` from last call of `catalog` method are captured.
     *
     * @since 0.8
     */
    private static class FakeDocker implements Docker {

        /**
         * Catalog.
         */
        private final Catalog ctlg;

        /**
         * From parameter captured.
         */
        private final AtomicReference<Optional<RepoName>> from;

        /**
         * Limit parameter captured.
         */
        private final AtomicInteger limit;

        FakeDocker(final Catalog ctlg) {
            this.ctlg = ctlg;
            this.from = new AtomicReference<>();
            this.limit = new AtomicInteger();
        }

        @Override
        public Repo repo(final RepoName name) {
            throw new UnsupportedOperationException();
        }

        @Override
        public CompletionStage<Catalog> catalog(final Optional<RepoName> pfrom, final int plimit) {
            this.from.set(pfrom);
            this.limit.set(plimit);
            return CompletableFuture.completedFuture(this.ctlg);
        }
    }
}
