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
import com.artipie.http.hm.RsHasHeaders;
import com.artipie.http.hm.RsHasStatus;
import com.artipie.http.rs.Header;
import com.artipie.http.rs.RsStatus;
import org.cactoos.list.ListOf;
import org.hamcrest.Matcher;
import org.hamcrest.core.AllOf;

/**
 * Response matcher.
 * @since 0.2
 * @todo #119:30min There are a lot of `AllOf<>` matcher creation duplications in this package test
 *  classes. Use this class to get rid of duplications: for example, we can add ctor with content
 *  and get rid of the
 *  com.artipie.docker.http.ManifestEntityGetTest#success(com.artipie.asto.Key)
 *  method.
 */
final class ResponseMatcher extends AllOf<Response> {

    /**
     * Ctor.
     */
    ResponseMatcher() {
        super(
            new ListOf<Matcher<? super Response>>(
                new RsHasStatus(RsStatus.OK),
                new RsHasHeaders(
                    new Header(
                        "Content-type", "application/vnd.docker.distribution.manifest.v2+json"
                    )
                )
            )
        );
    }
}
