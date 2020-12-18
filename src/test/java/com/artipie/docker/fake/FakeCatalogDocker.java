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
package com.artipie.docker.fake;

import com.artipie.docker.Catalog;
import com.artipie.docker.Docker;
import com.artipie.docker.Repo;
import com.artipie.docker.RepoName;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Docker implementation with specified catalog.
 * Values of parameters `from` and `limit` from last call of `catalog` method are captured.
 *
 * @since 0.10
 */
public final class FakeCatalogDocker implements Docker {

    /**
     * Catalog.
     */
    private final Catalog ctlg;

    /**
     * From parameter captured.
     */
    private final AtomicReference<Optional<RepoName>> cfrom;

    /**
     * Limit parameter captured.
     */
    private final AtomicInteger climit;

    /**
     * Ctor.
     *
     * @param ctlg Catalog.
     */
    public FakeCatalogDocker(final Catalog ctlg) {
        this.ctlg = ctlg;
        this.cfrom = new AtomicReference<>();
        this.climit = new AtomicInteger();
    }

    /**
     * Get captured from parameter.
     *
     * @return Captured from parameter.
     */
    public Optional<RepoName> from() {
        return this.cfrom.get();
    }

    /**
     * Get captured limit parameter.
     *
     * @return Captured limit parameter.
     */
    public int limit() {
        return this.climit.get();
    }

    @Override
    public Repo repo(final RepoName name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CompletionStage<Catalog> catalog(final Optional<RepoName> pfrom, final int plimit) {
        this.cfrom.set(pfrom);
        this.climit.set(plimit);
        return CompletableFuture.completedFuture(this.ctlg);
    }
}
