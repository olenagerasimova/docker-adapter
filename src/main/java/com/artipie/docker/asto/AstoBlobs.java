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
import com.artipie.asto.Remaining;
import com.artipie.asto.Storage;
import com.artipie.asto.ext.ContentDigest;
import com.artipie.docker.Blob;
import com.artipie.docker.Digest;
import io.reactivex.Flowable;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import org.cactoos.io.BytesOf;
import org.cactoos.text.HexOf;
import org.reactivestreams.Publisher;

/**
 * Asto {@link BlobStore} implementation.
 * @since 0.1
 */
public final class AstoBlobs implements BlobStore {

    /**
     * Storage.
     */
    private final Storage asto;

    /**
     * Ctor.
     * @param asto Storage
     */
    public AstoBlobs(final Storage asto) {
        this.asto = asto;
    }

    @Override
    public CompletionStage<Optional<Blob>> blob(final Digest digest) {
        return this.asto.exists(new BlobKey(digest)).thenApply(
            exists -> {
                final Optional<Blob> blob;
                if (exists) {
                    blob = Optional.of(new AstoBlob(this.asto, digest));
                } else {
                    blob = Optional.empty();
                }
                return blob;
            }
        );
    }

    @Override
    public CompletionStage<Blob> put(final Content blob, final Digest digest) {
        final MessageDigest sha = ContentDigest.Digests.SHA256.get();
        final Publisher<ByteBuffer> checked = Flowable.fromPublisher(blob).map(
            buf -> {
                sha.update(new Remaining(buf, true).bytes());
                return buf;
            }
        ).doOnTerminate(
            () -> {
                final String calculated = new HexOf(new BytesOf(sha.digest())).asString();
                if (!digest.hex().equals(calculated)) {
                    throw new IllegalArgumentException("Digests differ");
                }
            }
        );
        return this.asto.save(new BlobKey(digest), new Content.From(checked))
            .thenApply(ignored -> new AstoBlob(this.asto, digest));
    }
}
