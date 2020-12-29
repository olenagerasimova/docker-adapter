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

import com.artipie.asto.Storage;
import com.artipie.asto.memory.InMemoryStorage;
import com.artipie.docker.RepoName;
import com.artipie.docker.Uploads;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link AstoUploads}.
 *
 * @since 0.5
 */
@SuppressWarnings("PMD.TooManyMethods")
final class AstoUploadsTest {
    /**
     * Slice being tested.
     */
    private Uploads uploads;

    /**
     * Storage.
     */
    private Storage storage;

    /**
     * RepoName.
     */
    private RepoName reponame;

    @BeforeEach
    void setUp() {
        this.storage = new InMemoryStorage();
        this.reponame = new RepoName.Valid("test");
        this.uploads = new AstoUploads(
            this.storage,
            new DefaultLayout(),
            this.reponame
        );
    }

    @Test
    void checkUniquenessUuids() {
        final String uuid = this.uploads.start()
            .toCompletableFuture().join()
            .uuid();
        final String otheruuid = this.uploads.start()
            .toCompletableFuture().join()
            .uuid();
        MatcherAssert.assertThat(
            uuid.equals(otheruuid),
            new IsEqual<>(false)
        );
    }

    @Test
    void shouldStartNewAstoUpload() {
        final String uuid = this.uploads.start()
            .toCompletableFuture().join()
            .uuid();
        MatcherAssert.assertThat(
            this.storage.list(
                new UploadKey(this.reponame, uuid)
            ).join().isEmpty(),
            new IsEqual<>(false)
        );
    }

    @Test
    void shouldFindUploadByUuid() {
        final String uuid = this.uploads.start()
            .toCompletableFuture().join()
            .uuid();
        MatcherAssert.assertThat(
            this.uploads.get(uuid)
                .toCompletableFuture().join()
                .get().uuid(),
            new IsEqual<>(uuid)
        );
    }

    @Test
    void shouldNotFindUploadByEmptyUuid() {
        MatcherAssert.assertThat(
            this.uploads.get("")
                .toCompletableFuture().join()
                .isPresent(),
            new IsEqual<>(false)
        );
    }

    @Test
    void shouldReturnEmptyOptional() {
        MatcherAssert.assertThat(
            this.uploads.get("uuid")
                .toCompletableFuture().join()
                .isPresent(),
            new IsEqual<>(false)
        );
    }
}
