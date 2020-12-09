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

import com.artipie.docker.Docker;
import com.artipie.docker.RepoName;
import com.artipie.docker.Tag;
import com.artipie.docker.misc.RqByRegex;
import com.artipie.http.Response;
import com.artipie.http.Slice;
import com.artipie.http.async.AsyncResponse;
import com.artipie.http.rq.RequestLineFrom;
import com.artipie.http.rq.RqParams;
import com.artipie.http.rs.RsStatus;
import com.artipie.http.rs.RsWithBody;
import com.artipie.http.rs.RsWithHeaders;
import com.artipie.http.rs.RsWithStatus;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.regex.Pattern;
import org.reactivestreams.Publisher;

/**
 * Tags entity in Docker HTTP API.
 * See <a href="https://docs.docker.com/registry/spec/api/#tags">Tags</a>.
 *
 * @since 0.8
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
final class TagsEntity {

    /**
     * RegEx pattern for path.
     */
    public static final Pattern PATH = Pattern.compile("^/v2/(?<name>.*)/tags/list$");

    /**
     * Ctor.
     */
    private TagsEntity() {
    }

    /**
     * Slice for GET method, getting tags list.
     *
     * @since 0.8
     */
    public static class Get implements Slice {

        /**
         * Docker repository.
         */
        private final Docker docker;

        /**
         * Ctor.
         *
         * @param docker Docker repository.
         */
        Get(final Docker docker) {
            this.docker = docker;
        }

        @Override
        public Response response(
            final String line,
            final Iterable<Map.Entry<String, String>> headers,
            final Publisher<ByteBuffer> body
        ) {
            final RqParams params = new RqParams(new RequestLineFrom(line).uri().getQuery());
            return new AsyncResponse(
                this.docker.repo(
                    new RepoName.Valid(new RqByRegex(line, TagsEntity.PATH).path().group("name"))
                ).manifests().tags(
                    params.value("last").map(Tag.Valid::new),
                    params.value("n").map(Integer::parseInt).orElse(Integer.MAX_VALUE)
                ).thenApply(
                    tags -> new RsWithBody(
                        new RsWithHeaders(
                            new RsWithStatus(RsStatus.OK),
                            new JsonContentType()
                        ),
                        tags.json()
                    )
                )
            );
        }
    }
}
