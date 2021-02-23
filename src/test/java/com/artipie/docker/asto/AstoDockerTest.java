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

package com.artipie.docker.asto;

import com.artipie.asto.Content;
import com.artipie.asto.Key;
import com.artipie.asto.Storage;
import com.artipie.asto.memory.InMemoryStorage;
import com.artipie.docker.Catalog;
import com.artipie.docker.RepoName;
import com.artipie.docker.TestPublisherAs;
import java.util.Optional;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link AstoDocker}.
 * @since 0.1
 */
final class AstoDockerTest {
    @Test
    void createsAstoRepo() {
        MatcherAssert.assertThat(
            new AstoDocker(new InMemoryStorage()).repo(new RepoName.Simple("repo1")),
            Matchers.instanceOf(AstoRepo.class)
        );
    }

    @Test
    void shouldReadCatalogs() {
        final Storage storage = new InMemoryStorage();
        storage.save(
            new Key.From("repositories/my-alpine/something"),
            new Content.From("1".getBytes())
        ).toCompletableFuture().join();
        storage.save(
            new Key.From("repositories/test/foo/bar"),
            new Content.From("2".getBytes())
        ).toCompletableFuture().join();
        final Catalog catalog = new AstoDocker(storage)
            .catalog(Optional.empty(), Integer.MAX_VALUE)
            .toCompletableFuture().join();
        MatcherAssert.assertThat(
            new TestPublisherAs(catalog.json()).asciiString().toCompletableFuture().join(),
            new IsEqual<>("{\"repositories\":[\"my-alpine\",\"test\"]}")
        );
    }
}
