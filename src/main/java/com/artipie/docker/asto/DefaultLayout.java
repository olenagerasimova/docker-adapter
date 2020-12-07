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

import com.artipie.asto.Key;
import com.artipie.docker.Digest;
import com.artipie.docker.RepoName;
import com.artipie.docker.ref.ManifestRef;

/**
 * Original storage layout that is compatible with reference Docker Registry implementation.
 *
 * @since 0.7
 */
public final class DefaultLayout implements Layout {

    @Override
    public Key repositories() {
        return new Key.From("repositories");
    }

    @Override
    public Key blob(final RepoName repo, final Digest digest) {
        return new BlobKey(digest);
    }

    @Override
    public Key manifest(final RepoName repo, final ManifestRef ref) {
        return new Key.From(this.manifests(repo), ref.link().string());
    }

    @Override
    public Key tags(final RepoName repo) {
        return new Key.From(this.manifests(repo), "tags");
    }

    @Override
    public Key upload(final RepoName repo, final String uuid) {
        return new UploadKey(repo, uuid);
    }

    /**
     * Create manifests root key.
     *
     * @param repo Repository name.
     * @return Manifests key.
     */
    private Key manifests(final RepoName repo) {
        return new Key.From(this.repositories(), repo.value(), "_manifests");
    }
}
