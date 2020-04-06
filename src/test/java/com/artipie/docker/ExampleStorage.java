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
package com.artipie.docker;

import com.artipie.asto.Content;
import com.artipie.asto.Key;
import com.artipie.asto.Storage;
import com.artipie.asto.Transaction;
import com.artipie.asto.fs.FileStorage;
import io.vertx.reactivex.core.Vertx;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Storage with example docker repository data from resources folder 'example-my-alpine'.
 *
 * @since 0.2
 */
public final class ExampleStorage implements Storage {

    /**
     * Delegate storage.
     */
    private final Storage delegate;

    /**
     * Ctor.
     */
    public ExampleStorage() {
        this.delegate = new FileStorage(
            path(),
            Vertx.vertx().fileSystem()
        );
    }

    @Override
    public CompletableFuture<Boolean> exists(final Key key) {
        return this.delegate.exists(key);
    }

    @Override
    public CompletableFuture<Collection<Key>> list(final Key prefix) {
        return this.delegate.list(prefix);
    }

    @Override
    public CompletableFuture<Void> save(final Key key, final Content content) {
        return this.delegate.save(key, content);
    }

    @Override
    public CompletableFuture<Void> move(final Key source, final Key destination) {
        return this.delegate.move(source, destination);
    }

    @Override
    public CompletableFuture<Content> value(final Key key) {
        return this.delegate.value(key);
    }

    @Override
    public CompletableFuture<Void> delete(final Key key) {
        return this.delegate.delete(key);
    }

    @Override
    public CompletableFuture<Transaction> transaction(final List<Key> keys) {
        return this.delegate.transaction(keys);
    }

    /**
     * Path to example storage files.
     *
     * @return Files path.
     */
    private static Path path() {
        try {
            return Path.of(
                Thread.currentThread().getContextClassLoader()
                    .getResource("example-my-alpine").toURI()
            );
        } catch (final URISyntaxException ex) {
            throw new IllegalStateException("Failed to resolve resources path", ex);
        }
    }
}
