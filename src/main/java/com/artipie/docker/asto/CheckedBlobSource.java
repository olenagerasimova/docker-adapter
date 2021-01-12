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
import com.artipie.asto.Remaining;
import com.artipie.asto.Storage;
import com.artipie.asto.ext.Digests;
import com.artipie.docker.Digest;
import com.artipie.docker.error.InvalidDigestException;
import io.reactivex.Flowable;
import java.security.MessageDigest;
import java.util.concurrent.CompletionStage;
import org.cactoos.io.BytesOf;
import org.cactoos.text.HexOf;

/**
 * BlobSource which content is checked against digest on saving.
 *
 * @since 0.12
 */
public final class CheckedBlobSource implements BlobSource {

    /**
     * Blob content.
     */
    private final Content content;

    /**
     * Blob digest.
     */
    private final Digest dig;

    /**
     * Ctor.
     *
     * @param content Blob content.
     * @param dig Blob digest.
     */
    public CheckedBlobSource(final Content content, final Digest dig) {
        this.content = content;
        this.dig = dig;
    }

    @Override
    public Digest digest() {
        return this.dig;
    }

    @Override
    public CompletionStage<Void> saveTo(final Storage storage, final Key key) {
        final MessageDigest sha = Digests.SHA256.get();
        final Content checked = new Content.From(
            this.content.size(),
            Flowable.fromPublisher(this.content).map(
                buf -> {
                    sha.update(new Remaining(buf, true).bytes());
                    return buf;
                }
            ).doOnComplete(
                () -> {
                    final String calculated = new HexOf(new BytesOf(sha.digest())).asString();
                    final String expected = this.dig.hex();
                    if (!expected.equals(calculated)) {
                        throw new InvalidDigestException(
                            String.format("calculated: %s expected: %s", calculated, expected)
                        );
                    }
                }
            )
        );
        return new TrustedBlobSource(checked, this.dig).saveTo(storage, key);
    }
}
