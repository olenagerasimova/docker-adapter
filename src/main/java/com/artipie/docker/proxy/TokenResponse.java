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
package com.artipie.docker.proxy;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import javax.json.Json;

/**
 * Authentication token response.
 * See <a href="https://docs.docker.com/registry/spec/auth/token/#requesting-a-token">Requesting a Token</a>
 *
 * @since 0.3
 */
final class TokenResponse {

    /**
     * Response content.
     */
    private final byte[] content;

    /**
     * Ctor.
     *
     * @param content Response content.
     */
    TokenResponse(final byte[] content) {
        this.content = Arrays.copyOf(content, content.length);
    }

    /**
     * Reads token string.
     *
     * @return Token string.
     */
    public String token() {
        return Json.createReader(new ByteArrayInputStream(this.content))
            .readObject()
            .getString("token");
    }
}
