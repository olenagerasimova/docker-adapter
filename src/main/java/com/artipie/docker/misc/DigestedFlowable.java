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
package com.artipie.docker.misc;

import com.artipie.asto.Remaining;
import com.artipie.asto.ext.Digests;
import com.artipie.docker.Digest;
import io.reactivex.Flowable;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import org.cactoos.io.BytesOf;
import org.cactoos.text.HexOf;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

/**
 * {@link Flowable} that calculates digest of origin {@link Publisher} bytes when they pass by.
 *
 * @since 0.12
 */
public final class DigestedFlowable extends Flowable<ByteBuffer> {

    /**
     * Origin publisher.
     */
    private final Publisher<ByteBuffer> origin;

    /**
     * Calculated digest.
     */
    private final AtomicReference<Digest> dig;

    /**
     * Ctor.
     *
     * @param origin Origin publisher.
     */
    public DigestedFlowable(final Publisher<ByteBuffer> origin) {
        this.dig = new AtomicReference<>();
        this.origin = origin;
    }

    @Override
    public void subscribeActual(final Subscriber<? super ByteBuffer> subscriber) {
        final MessageDigest sha = Digests.SHA256.get();
        Flowable.fromPublisher(this.origin).map(
            buf -> {
                sha.update(new Remaining(buf, true).bytes());
                return buf;
            }
        ).doOnComplete(
            () -> this.dig.set(
                new Digest.Sha256(new HexOf(new BytesOf(sha.digest())).asString())
            )
        ).subscribe(subscriber);
    }

    /**
     * Calculated digest.
     *
     * @return Digest.
     */
    public Digest digest() {
        return Objects.requireNonNull(this.dig.get(), "Digest is not yet calculated.");
    }
}
