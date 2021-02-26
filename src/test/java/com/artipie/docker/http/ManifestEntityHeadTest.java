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

import com.artipie.docker.ExampleStorage;
import com.artipie.docker.asto.AstoDocker;
import com.artipie.http.Response;
import com.artipie.http.headers.Header;
import com.artipie.http.hm.RsHasHeaders;
import com.artipie.http.hm.RsHasStatus;
import com.artipie.http.rq.RequestLine;
import com.artipie.http.rq.RqMethod;
import com.artipie.http.rs.RsStatus;
import io.reactivex.Flowable;
import org.cactoos.list.ListOf;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.AllOf;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link DockerSlice}.
 * Manifest HEAD endpoint.
 *
 * @since 0.2
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @checkstyle MagicNumberCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class ManifestEntityHeadTest {

    /**
     * Slice being tested.
     */
    private DockerSlice slice;

    @BeforeEach
    void setUp() {
        this.slice = new DockerSlice(new AstoDocker(new ExampleStorage()));
    }

    @Test
    void shouldRespondOkWhenManifestFoundByTag() {
        MatcherAssert.assertThat(
            this.slice.response(
                new RequestLine(RqMethod.HEAD, "/v2/my-alpine/manifests/1").toString(),
                new Headers(),
                Flowable.empty()
            ),
            new ResponseMatcher(
                "sha256:cb8a924afdf0229ef7515d9e5b3024e23b3eb03ddbba287f4a19c6ac90b8d221",
                528
            )
        );
    }

    @Test
    void shouldRespondOkWhenManifestFoundByDigest() {
        final String digest = String.format(
            "%s:%s",
            "sha256",
            "cb8a924afdf0229ef7515d9e5b3024e23b3eb03ddbba287f4a19c6ac90b8d221"
        );
        MatcherAssert.assertThat(
            this.slice.response(
                new RequestLine(
                    RqMethod.HEAD,
                    String.format("/v2/my-alpine/manifests/%s", digest)
                ).toString(),
                new Headers(),
                Flowable.empty()
            ),
            new ResponseMatcher(digest, 528)
        );
    }

    @Test
    void shouldReturnNotFoundForUnknownTag() {
        MatcherAssert.assertThat(
            this.slice.response(
                new RequestLine(RqMethod.HEAD, "/v2/my-alpine/manifests/2").toString(),
                new Headers(),
                Flowable.empty()
            ),
            new IsErrorsResponse(RsStatus.NOT_FOUND, "MANIFEST_UNKNOWN")
        );
    }

    @Test
    void shouldReturnNotFoundForUnknownDigest() {
        MatcherAssert.assertThat(
            this.slice.response(
                new RequestLine(
                    RqMethod.HEAD,
                    String.format(
                        "/v2/my-alpine/manifests/%s",
                        "sha256:0123456789012345678901234567890123456789012345678901234567890123"
                    )).toString(),
                new Headers(),
                Flowable.empty()
            ),
            new IsErrorsResponse(RsStatus.NOT_FOUND, "MANIFEST_UNKNOWN")
        );
    }

    /**
     * Headers set for getting manifest.
     *
     * @since 0.4
     */
    private static class Headers extends com.artipie.http.Headers.Wrap {

        Headers() {
            super(
                new Headers.From(
                    new Header("Accept", "application/vnd.docker.distribution.manifest.v2+json")
                )
            );
        }
    }

    /**
     * Manifest entity head response matcher.
     * @since 0.3
     */
    private static final class ResponseMatcher extends AllOf<Response> {

        /**
         * Ctor.
         *
         * @param digest Expected `Docker-Content-Digest` header value.
         * @param size Expected `Content-Length` header value.
         */
        ResponseMatcher(final String digest, final long size) {
            super(
                new ListOf<Matcher<? super Response>>(
                    new RsHasStatus(RsStatus.OK),
                    new RsHasHeaders(
                        new Header(
                            "Content-type", "application/vnd.docker.distribution.manifest.v2+json"
                        ),
                        new Header("Docker-Content-Digest", digest),
                        new Header("Content-Length", String.valueOf(size))
                    )
                )
            );
        }

    }

}
