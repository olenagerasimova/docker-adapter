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
import com.artipie.docker.Blob;
import com.artipie.docker.BlobStore;
import com.artipie.docker.Digest;
import com.artipie.docker.Repo;
import com.artipie.docker.RepoName;
import com.artipie.docker.Upload;
import com.artipie.docker.manifest.JsonManifest;
import com.artipie.docker.manifest.Manifest;
import com.artipie.docker.misc.BytesFlowAs;
import com.artipie.docker.ref.ManifestRef;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Asto implementation of {@link Repo}.
 *
 * @since 0.1
 */
public final class AstoRepo implements Repo {

    /**
     * Asto storage.
     */
    private final Storage asto;

    /**
     * Repository name.
     */
    private final RepoName name;

    /**
     * Blobs storage.
     */
    private final BlobStore blobs;

    /**
     * Ctor.
     *
     * @param asto Asto storage
     * @param blobs Blobs storage.
     * @param name Repository name
     */
    public AstoRepo(final Storage asto, final BlobStore blobs, final RepoName name) {
        this.asto = asto;
        this.blobs = blobs;
        this.name = name;
    }

    @Override
    public CompletionStage<Void> addManifest(final ManifestRef ref, final Blob blob) {
        final Digest digest = blob.digest();
        return blob.content()
            .thenApply(JsonManifest::new)
            .thenCompose(this::validate)
            .thenCompose(
                ignored -> CompletableFuture.allOf(
                    this.addLink(new ManifestRef.FromDigest(digest), digest).toCompletableFuture(),
                    this.addLink(ref, digest).toCompletableFuture()
                )
            );
    }

    @Override
    public CompletionStage<Optional<Manifest>> manifest(final ManifestRef ref) {
        return this.readLink(ref).thenCompose(
            digestOpt -> digestOpt.map(
                digest -> this.blobs.blob(digest)
                    .thenCompose(
                        blobOpt -> blobOpt
                            .map(
                                blob -> blob.content()
                                    .<Manifest>thenApply(JsonManifest::new)
                                    .thenApply(Optional::of)
                            )
                            .orElseGet(() -> CompletableFuture.completedFuture(Optional.empty()))
                    )
            ).orElseGet(() -> CompletableFuture.completedFuture(Optional.empty()))
        );
    }

    @Override
    public Upload upload(final String uuid) {
        return new AstoUpload(this.asto, this.name, uuid);
    }

    /**
     * Validates manifest by checking all referenced blobs exist.
     *
     * @param manifest Manifest.
     * @return Validation completion.
     */
    private CompletionStage<Void> validate(final Manifest manifest) {
        return manifest.layers().thenCompose(
            digests -> CompletableFuture.allOf(
                digests.stream().map(
                    digest -> this.blobs.blob(digest).thenCompose(
                        opt -> {
                            if (opt.isEmpty()) {
                                throw new IllegalArgumentException(
                                    String.format("Blob not exists: %s", digest)
                                );
                            }
                            return CompletableFuture.allOf();
                        }
                    ).toCompletableFuture()
                ).toArray(CompletableFuture[]::new)
            )
        );
    }

    /**
     * Puts link to blob to manifest reference path.
     *
     * @param ref Manifest reference.
     * @param digest Blob digest.
     * @return Link key.
     */
    private CompletionStage<Void> addLink(final ManifestRef ref, final Digest digest) {
        return this.asto.save(this.link(ref), new Content.From(digest.string().getBytes()));
    }

    /**
     * Reads link to blob by manifest reference.
     *
     * @param ref Manifest reference.
     * @return Blob digest, empty if no link found.
     */
    private CompletableFuture<Optional<Digest>> readLink(final ManifestRef ref) {
        final Key key = this.link(ref);
        return this.asto.exists(key).thenCompose(
            exists -> {
                final CompletionStage<Optional<Digest>> stage;
                if (exists) {
                    stage = this.asto.value(key)
                        .thenCompose(pub -> new BytesFlowAs.Text(pub).future())
                        .<Digest>thenApply(Digest.FromString::new)
                        .thenApply(Optional::of);
                } else {
                    stage = CompletableFuture.completedFuture(Optional.empty());
                }
                return stage;
            }
        );
    }

    /**
     * Create link key from manifest reference.
     *
     * @param ref Manifest reference.
     * @return Link key.
     */
    private Key link(final ManifestRef ref) {
        return new Key.From(
            RegistryRoot.V2, "repositories", this.name.value(),
            "_manifests", ref.link().string()
        );
    }
}
