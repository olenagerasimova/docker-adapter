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
import com.artipie.docker.RepoName;
import com.artipie.docker.Upload;
import hu.akarnokd.rxjava2.interop.SingleInterop;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletionStage;
import org.reactivestreams.Publisher;

/**
 * Asto implementation of {@link Upload}.
 *
 * @since 0.2
 */
public final class AstoUpload implements Upload {

    /**
     * Storage.
     */
    private final Storage storage;

    /**
     * Repository name.
     */
    private final RepoName name;

    /**
     * Upload UUID.
     */
    private final String uuid;

    /**
     * Ctor.
     *
     * @param storage Storage.
     * @param name Repository name.
     * @param uuid Upload UUID.
     */
    public AstoUpload(final Storage storage, final RepoName name, final String uuid) {
        this.storage = storage;
        this.name = name;
        this.uuid = uuid;
    }

    @Override
    public CompletionStage<Long> append(final Publisher<ByteBuffer> chunk) {
        final Key root = this.root();
        return this.storage.list(root).thenCompose(
            chunks -> {
                if (!chunks.isEmpty()) {
                    throw new UnsupportedOperationException("Multiple chunks are not supported");
                }
                return new Concatenation(chunk).single().to(SingleInterop.get()).thenCompose(
                    buf -> {
                        final byte[] bytes = new Remaining(buf, true).bytes();
                        final long offset = bytes.length;
                        return this.storage.save(
                            new Key.From(root, String.valueOf(offset)),
                            new Content.From(bytes)
                        ).thenApply(ignored -> offset);
                    }
                );
            }
        );
    }

    @Override
    public CompletionStage<Content> content() {
        return this.storage.list(this.root()).thenCompose(
            chunks -> {
                if (chunks.size() == 0) {
                    throw new IllegalStateException("No content was uploaded yet");
                }
                if (chunks.size() > 1) {
                    throw new UnsupportedOperationException(
                        "Multiple chunks are not supported yet"
                    );
                }
                return this.storage.value(chunks.iterator().next());
            }
        );
    }

    /**
     * Root key for upload chunks.
     *
     * @return Root key.
     */
    private Key root() {
        return new Key.From(
            RegistryRoot.V2, "repositories", this.name.value(),
            "_uploads", this.uuid
        );
    }
}
