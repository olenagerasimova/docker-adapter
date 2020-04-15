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
import com.artipie.http.rq.RequestLineFrom;
import com.artipie.http.rs.RsStatus;
import com.artipie.http.rs.RsWithHeaders;
import com.artipie.http.rs.RsWithStatus;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.cactoos.map.MapEntry;
import org.reactivestreams.Publisher;

/**
 * Slice for start upload endpoint.
 * See <a href="https://docs.docker.com/registry/spec/api/#pushing-an-image">Pushing An Image</a>.
 *
 * @since 0.2
 */
public final class StartUpload implements Slice {

    /**
     * RegEx pattern for path.
     */
    public static final Pattern PATH = Pattern.compile(
        "^/v2/(?<name>[^/]*)/blobs/uploads/$"
    );

    @Override
    public Response response(
        final String line,
        final Iterable<Map.Entry<String, String>> headers,
        final Publisher<ByteBuffer> body
    ) {
        final String path = new RequestLineFrom(line).uri().getPath();
        final Matcher matcher = PATH.matcher(path);
        if (!matcher.find()) {
            throw new IllegalStateException(
                String.format("Unexpected path: %s", path)
            );
        }
        final String name = matcher.group("name");
        final String uuid = UUID.randomUUID().toString();
        return new RsWithHeaders(
            new RsWithStatus(RsStatus.ACCEPTED),
            Arrays.asList(
                new MapEntry<>("Location", String.format("/v2/%s/blobs/uploads/%s", name, uuid)),
                new MapEntry<>("Range", "bytes=0-0"),
                new MapEntry<>("Content-Length", "0"),
                new MapEntry<>("Docker-Upload-UUID", uuid)
            )
        );
    }
}
