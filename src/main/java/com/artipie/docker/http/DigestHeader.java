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
import com.artipie.http.rs.Header;
import java.util.Map;

/**
 * Docker-Content-Digest header.
 * See <a href="https://docs.docker.com/registry/spec/api/#blob-upload#content-digests">Content Digests</a>.
 *
 * @since 0.2
 */
final class DigestHeader implements Map.Entry<String, String> {

    /**
     * Header delegate.
     */
    private final Header delegate;

    /**
     * Ctor.
     *
     * @param digest Digest value.
     */
    DigestHeader(final Digest digest) {
        this.delegate = new Header("Docker-Content-Digest", digest.string());
    }

    @Override
    public String getKey() {
        return this.delegate.getKey();
    }

    @Override
    public String getValue() {
        return this.delegate.getValue();
    }

    @Override
    public String setValue(final String value) {
        throw new UnsupportedOperationException("Value cannot be modified");
    }

    @Override
    public boolean equals(final Object that) {
        return this.delegate.equals(that);
    }

    @Override
    public int hashCode() {
        return this.delegate.hashCode();
    }

    @Override
    public String toString() {
        return this.delegate.toString();
    }
}
