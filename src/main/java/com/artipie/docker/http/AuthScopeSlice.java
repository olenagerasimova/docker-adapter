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

import com.artipie.http.Response;
import com.artipie.http.Slice;
import com.artipie.http.auth.AuthScheme;
import com.artipie.http.auth.AuthSlice;
import com.artipie.http.auth.Permissions;
import java.nio.ByteBuffer;
import java.util.Map;
import org.reactivestreams.Publisher;

/**
 * Slice that implements authorization for {@link ScopeSlice}.
 *
 * @since 0.11
 */
final class AuthScopeSlice implements Slice {

    /**
     * Origin.
     */
    private final ScopeSlice origin;

    /**
     * Authentication scheme.
     */
    private final AuthScheme auth;

    /**
     * Access permissions.
     */
    private final Permissions perms;

    /**
     * Ctor.
     *
     * @param origin Origin slice.
     * @param auth Authentication scheme.
     * @param perms Access permissions.
     */
    AuthScopeSlice(
        final ScopeSlice origin,
        final AuthScheme auth,
        final Permissions perms
    ) {
        this.origin = origin;
        this.auth = auth;
        this.perms = perms;
    }

    @Override
    public Response response(
        final String line,
        final Iterable<Map.Entry<String, String>> headers,
        final Publisher<ByteBuffer> body
    ) {
        return new AuthSlice(
            this.origin,
            this.auth,
            user -> this.perms.allowed(user, this.origin.scope(line).string())
        ).response(line, headers, body);
    }
}
