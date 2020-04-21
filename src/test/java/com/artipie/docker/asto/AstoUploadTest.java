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
import com.artipie.asto.Remaining;
import com.artipie.docker.ExampleStorage;
import com.artipie.docker.RepoName;
import com.artipie.docker.Upload;
import hu.akarnokd.rxjava2.interop.SingleInterop;
import io.reactivex.Flowable;
import java.nio.ByteBuffer;
import java.util.UUID;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link AstoUpload}.
 *
 * @since 0.2
 * @todo #54:30min Implement AstoUpload.
 *  Implement AstoUpload and enable `AstoUploadTest#shouldReadAppendedChunk` test.
 *  Minimal support should allow single chunk being added and read afterwards.
 */
class AstoUploadTest {

    @Test
    @Disabled("Not implemented")
    void shouldReadAppendedChunk() throws Exception {
        final Upload upload = new AstoUpload(
            new ExampleStorage(),
            new RepoName.Valid("test"),
            UUID.randomUUID().toString()
        );
        final byte[] chunk = "chunk".getBytes();
        upload.append(Flowable.just(ByteBuffer.wrap(chunk))).toCompletableFuture().join();
        MatcherAssert.assertThat(
            upload.content()
                .thenApply(Concatenation::new)
                .thenApply(Concatenation::single)
                .thenCompose(single -> single.to(SingleInterop.get()))
                .thenApply(Remaining::new)
                .thenApply(Remaining::bytes)
                .toCompletableFuture()
                .get(),
            new IsEqual<>(chunk)
        );
    }
}
