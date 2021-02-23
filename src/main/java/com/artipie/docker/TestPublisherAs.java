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
package com.artipie.docker;

import com.artipie.asto.Concatenation;
import com.artipie.asto.Content;
import com.artipie.asto.Remaining;
import hu.akarnokd.rxjava2.interop.SingleInterop;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletionStage;
import org.reactivestreams.Publisher;

/**
 * Read bytes from content to memory.
 * Using this class keep in mind that it reads ByteBuffer from publisher into memory and is not
 * suitable for large content.
 * @since 0.24
 */
public final class TestPublisherAs {

    /**
     * Content to read bytes from.
     */
    private final Content content;

    /**
     * Ctor.
     * @param content Content
     */
    public TestPublisherAs(final Content content) {
        this.content = content;
    }

    /**
     * Ctor.
     * @param content Content
     */
    public TestPublisherAs(final Publisher<ByteBuffer> content) {
        this(new Content.From(content));
    }

    /**
     * Reads bytes from content into memory.
     * @return Byte array as CompletionStage
     */
    public CompletionStage<byte[]> bytes() {
        return new Concatenation(this.content)
            .single()
            .map(Remaining::new)
            .map(Remaining::bytes)
            .to(SingleInterop.get());
    }

    /**
     * Reads bytes from content as string.
     * @param charset Charset to read string
     * @return String as CompletionStage
     */
    public CompletionStage<String> string(final Charset charset) {
        return this.bytes().thenApply(bytes -> new String(bytes, charset));
    }

    /**
     * Reads bytes from content as {@link StandardCharsets#US_ASCII} string.
     * @return String as CompletionStage
     */
    public CompletionStage<String> asciiString() {
        return this.string(StandardCharsets.US_ASCII);
    }

}
