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

import com.artipie.asto.Content;
import com.artipie.http.Headers;
import com.artipie.http.headers.ContentLength;
import com.artipie.http.headers.Header;
import com.artipie.http.headers.WwwAuthenticate;
import com.artipie.http.hm.ResponseMatcher;
import com.artipie.http.hm.RsHasHeaders;
import com.artipie.http.rq.RequestLine;
import com.artipie.http.rq.RqMethod;
import com.artipie.http.rs.RsFull;
import com.artipie.http.rs.RsStatus;
import com.artipie.http.rs.RsWithHeaders;
import com.artipie.http.rs.RsWithStatus;
import io.reactivex.Flowable;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.AllOf;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link DockerAuthSlice}.
 *
 * @since 0.5
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class DockerAuthSliceTest {

    @Test
    void shouldReturnErrorsWhenUnathorized() {
        final Headers headers = new Headers.From(
            new WwwAuthenticate("Basic"),
            new Header("X-Something", "Value")
        );
        MatcherAssert.assertThat(
            new DockerAuthSlice(
                (rqline, rqheaders, rqbody) -> new RsWithHeaders(
                    new RsWithStatus(RsStatus.UNAUTHORIZED),
                    new Headers.From(headers)
                )
            ).response(
                new RequestLine(RqMethod.GET, "/").toString(),
                Headers.EMPTY,
                Flowable.empty()
            ),
            new AllOf<>(
                Arrays.asList(
                    new IsUnauthorizedResponse(),
                    new RsHasHeaders(new Headers.From(headers, new ContentLength("72")))
                )
            )
        );
    }

    @Test
    void shouldReturnErrorsWhenForbidden() {
        final Headers headers = new Headers.From(
            new WwwAuthenticate("Basic realm=\"123\""),
            new Header("X-Foo", "Bar")
        );
        MatcherAssert.assertThat(
            new DockerAuthSlice(
                (rqline, rqheaders, rqbody) -> new RsWithHeaders(
                    new RsWithStatus(RsStatus.FORBIDDEN),
                    new Headers.From(headers)
                )
            ).response(
                new RequestLine(RqMethod.GET, "/file.txt").toString(),
                Headers.EMPTY,
                Content.EMPTY
            ),
            new AllOf<>(
                Arrays.asList(
                    new IsDeniedResponse(),
                    new RsHasHeaders(new Headers.From(headers, new ContentLength("85")))
                )
            )
        );
    }

    @Test
    void shouldNotModifyNormalResponse() {
        final RsStatus status = RsStatus.OK;
        final Collection<Map.Entry<String, String>> headers = Collections.singleton(
            new Header("Content-Type", "text/plain")
        );
        final byte[] body = "data".getBytes();
        MatcherAssert.assertThat(
            new DockerAuthSlice(
                (rqline, rqheaders, rqbody) -> new RsFull(
                    status,
                    new Headers.From(headers),
                    Flowable.just(ByteBuffer.wrap(body))
                )
            ).response(
                new RequestLine(RqMethod.GET, "/some/path").toString(),
                Headers.EMPTY,
                Flowable.empty()
            ),
            new ResponseMatcher(status, headers, body)
        );
    }
}
