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
package com.artipie.docker.http;

import com.artipie.docker.Digest;
import com.artipie.http.Headers;
import com.artipie.http.headers.Header;
import com.artipie.http.rq.RqHeaders;

/**
 * Docker-Content-Digest header.
 * See <a href="https://docs.docker.com/registry/spec/api/#blob-upload#content-digests">Content Digests</a>.
 *
 * @since 0.2
 */
public final class DigestHeader extends Header.Wrap {

    /**
     * Header name.
     */
    private static final String NAME = "Docker-Content-Digest";

    /**
     * Ctor.
     *
     * @param digest Digest value.
     */
    public DigestHeader(final Digest digest) {
        this(digest.string());
    }

    /**
     * Ctor.
     *
     * @param headers Headers to extract header from.
     */
    public DigestHeader(final Headers headers) {
        this(new RqHeaders.Single(headers, DigestHeader.NAME).asString());
    }

    /**
     * Ctor.
     *
     * @param digest Digest value.
     */
    private DigestHeader(final String digest) {
        super(new Header(DigestHeader.NAME, digest));
    }

    /**
     * Read header as numeric value.
     *
     * @return Header value.
     */
    public Digest value() {
        return new Digest.FromString(this.getValue());
    }
}
