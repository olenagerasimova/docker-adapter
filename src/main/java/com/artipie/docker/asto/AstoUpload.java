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
import com.artipie.docker.RepoName;
import com.artipie.docker.Upload;
import java.nio.ByteBuffer;
import java.util.UUID;
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
        return this.storage.exists(this.data()).thenCompose(
            exists -> {
                if (exists) {
                    throw new UnsupportedOperationException("Multiple chunks are not supported");
                }
                final Key tmp = new Key.From(this.root(), UUID.randomUUID().toString());
                return this.storage.save(tmp, new Content.From(chunk)).thenCompose(
                    ignored -> this.storage.move(tmp, this.data())
                ).thenCompose(
                    ignored -> this.storage.size(this.data()).thenApply(size -> size - 1)
                );
            }
        );
    }

    @Override
    public CompletionStage<Content> content() {
        return this.storage.exists(this.data()).thenCompose(
            exists -> {
                if (!exists) {
                    throw new IllegalStateException("No content was uploaded yet");
                }
                return this.storage.value(this.data());
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

    /**
     * Uploaded data key.
     *
     * @return Key.
     */
    private Key data() {
        return new Key.From(this.root(), "data");
    }
}
