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

import com.artipie.asto.Concatenation;
import com.artipie.asto.Content;
import com.artipie.asto.Key;
import com.artipie.asto.Remaining;
import com.artipie.asto.Storage;
import com.artipie.docker.BlobStore;
import com.artipie.docker.Digest;
import com.artipie.docker.Repo;
import com.artipie.docker.RepoName;
import com.artipie.docker.Upload;
import com.artipie.docker.manifest.JsonManifest;
import com.artipie.docker.manifest.Layer;
import com.artipie.docker.manifest.Manifest;
import com.artipie.docker.ref.ManifestRef;
import hu.akarnokd.rxjava2.interop.SingleInterop;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Stream;

/**
 * Asto implementation of {@link Repo}.
 *
 * @since 0.1
 * @todo #168:30min Extract reading `Content` as byte array.
 *  This code is duplicated in many places in project:
 *  ```
 *  new Concatenation(content).single().map(Remaining::new).map(Remaining::bytes)
 *  ```
 *  It could be extracted into class and used everywhere.
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
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
    public CompletionStage<Manifest> addManifest(final ManifestRef ref, final Content content) {
        return new Concatenation(content)
            .single()
            .map(Remaining::new)
            .map(Remaining::bytes)
            .to(SingleInterop.get())
            .thenCompose(bytes -> this.blobs.put(new Content.From(bytes)))
            .thenCompose(
                blob -> {
                    final Digest digest = blob.digest();
                    return blob.content()
                        .thenApply(source -> new JsonManifest(digest, source))
                        .thenCompose(
                            manifest -> this.validate(manifest)
                                .thenCompose(nothing -> this.addManifestLinks(ref, digest))
                                .thenApply(nothing -> manifest)
                        );
                }
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
                                    .<Manifest>thenApply(
                                        source -> new JsonManifest(blob.digest(), source)
                                    )
                                    .thenApply(Optional::of)
                            )
                            .orElseGet(() -> CompletableFuture.completedFuture(Optional.empty()))
                    )
            ).orElseGet(() -> CompletableFuture.completedFuture(Optional.empty()))
        );
    }

    @Override
    public CompletionStage<Upload> startUpload() {
        final String uuid = UUID.randomUUID().toString();
        final AstoUpload upload = new AstoUpload(this.asto, this.name, uuid);
        return upload.start().thenApply(ignored -> upload);
    }

    @Override
    public CompletionStage<Optional<Upload>> upload(final String uuid) {
        return this.asto.list(new UploadKey(this.name, uuid)).thenApply(
            list -> {
                final Optional<Upload> upload;
                if (list.isEmpty()) {
                    upload = Optional.empty();
                } else {
                    upload = Optional.of(new AstoUpload(this.asto, this.name, uuid));
                }
                return upload;
            }
        );
    }

    /**
     * Validates manifest by checking all referenced blobs exist.
     *
     * @param manifest Manifest.
     * @return Validation completion.
     */
    private CompletionStage<Void> validate(final Manifest manifest) {
        return manifest.config()
            .thenCompose(
                config -> manifest.layers().thenApply(
                    layers -> Stream.concat(
                        Stream.of(config),
                        layers.stream().filter(layer -> layer.urls().isEmpty()).map(Layer::digest)
                    )
                )
            )
            .thenCompose(
                digests -> CompletableFuture.allOf(
                    digests.map(
                        digest -> this.blobs.blob(digest).thenCompose(
                            opt -> {
                                if (opt.isEmpty()) {
                                    throw new IllegalArgumentException(
                                        String.format("Blob does not exist: %s", digest)
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
     * Adds links to manifest blob by reference and by digest.
     *
     * @param ref Manifest reference.
     * @param digest Blob digest.
     * @return Signal that links are added.
     */
    private CompletableFuture<Void> addManifestLinks(final ManifestRef ref, final Digest digest) {
        return CompletableFuture.allOf(
            this.addLink(new ManifestRef.FromDigest(digest), digest),
            this.addLink(ref, digest)
        );
    }

    /**
     * Puts link to blob to manifest reference path.
     *
     * @param ref Manifest reference.
     * @param digest Blob digest.
     * @return Link key.
     */
    private CompletableFuture<Void> addLink(final ManifestRef ref, final Digest digest) {
        return this.asto.save(
            this.link(ref),
            new Content.From(digest.string().getBytes(StandardCharsets.US_ASCII))
        ).toCompletableFuture();
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
                        .thenCompose(
                            pub -> new Concatenation(pub).single()
                                .map(buf -> new Remaining(buf, true))
                                .map(Remaining::bytes)
                                .map(bytes -> new String(bytes, StandardCharsets.US_ASCII))
                                .to(SingleInterop.get())
                        )
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
