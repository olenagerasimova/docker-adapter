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
import com.artipie.docker.Upload;
import com.artipie.http.Connection;
import com.artipie.http.Response;
import com.artipie.http.Slice;
import com.artipie.http.async.AsyncResponse;
import com.artipie.http.rq.RequestLineFrom;
import com.artipie.http.rs.Header;
import com.artipie.http.rs.RsStatus;
import com.artipie.http.rs.RsWithHeaders;
import com.artipie.http.rs.RsWithStatus;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.reactivestreams.Publisher;

/**
 * Blob Upload entity in Docker HTTP API.
 * See <a href="https://docs.docker.com/registry/spec/api/#initiate-blob-upload">Initiate Blob Upload</a>
 * and <a href="https://docs.docker.com/registry/spec/api/#blob-upload">Blob Upload</a>.
 *
 * @since 0.2
 * @checkstyle ClassDataAbstractionCouplingCheck (2 lines)
 */
public final class UploadEntity {

    /**
     * RegEx pattern for path.
     */
    public static final Pattern PATH = Pattern.compile(
        "^/v2/(?<name>[^/]*)/blobs/uploads/(?<uuid>.*)$"
    );

    /**
     * Ctor.
     */
    private UploadEntity() {
    }

    /**
     * Slice for POST method.
     *
     * @since 0.2
     */
    public static final class Post implements Slice {

        @Override
        public Response response(
            final String line,
            final Iterable<Map.Entry<String, String>> headers,
            final Publisher<ByteBuffer> body
        ) {
            final RepoName name = new Request(line).name();
            final String uuid = UUID.randomUUID().toString();
            return new StatusResponse(name, uuid, 0);
        }
    }

    /**
     * Slice for PATCH method.
     *
     * @since 0.2
     */
    public static final class Patch implements Slice {

        /**
         * Docker repository.
         */
        private final Docker docker;

        /**
         * Ctor.
         *
         * @param docker Docker repository.
         */
        Patch(final Docker docker) {
            this.docker = docker;
        }

        @Override
        public Response response(
            final String line,
            final Iterable<Map.Entry<String, String>> headers,
            final Publisher<ByteBuffer> body
        ) {
            final Request request = new Request(line);
            final RepoName name = request.name();
            final String uuid = request.uuid();
            final Upload upload = this.docker.repo(name).upload(uuid);
            return new AsyncResponse(
                upload.append(body).thenApply(offset -> new StatusResponse(name, uuid, offset))
            );
        }
    }

    /**
     * Slice for PUT method.
     *
     * @since 0.2
     */
    public static final class Put implements Slice {

        @Override
        public Response response(
            final String line,
            final Iterable<Map.Entry<String, String>> headers,
            final Publisher<ByteBuffer> body
        ) {
            return new RsWithStatus(RsStatus.CREATED);
        }
    }

    /**
     * HTTP request to upload blob entity.
     *
     * @since 0.2
     * @todo #54:30min Add unit tests for UploadEntity.Request class.
     *  Now this class responsible for UploadEntity requests parsing has no unit tests coverage.
     *  It should be made accessible for testing and tested.
     */
    private static final class Request {

        /**
         * HTTP request line.
         */
        private final String line;

        /**
         * Ctor.
         *
         * @param line HTTP request line.
         */
        Request(final String line) {
            this.line = line;
        }

        /**
         * Get repository name.
         *
         * @return Repository name.
         */
        RepoName name() {
            return new RepoName.Valid(this.path().group("name"));
        }

        /**
         * Get upload UUID.
         *
         * @return Upload UUID.
         */
        String uuid() {
            return this.path().group("uuid");
        }

        /**
         * Matches request path by RegEx pattern.
         *
         * @return Path matcher.
         */
        private Matcher path() {
            final String path = new RequestLineFrom(this.line).uri().getPath();
            final Matcher matcher = PATH.matcher(path);
            if (!matcher.find()) {
                throw new IllegalStateException(String.format("Unexpected path: %s", path));
            }
            return matcher;
        }
    }

    /**
     * Upload blob status HTTP response.
     *
     * @since 0.2
     */
    private static class StatusResponse implements Response {

        /**
         * Repository name.
         */
        private final RepoName name;

        /**
         * Upload UUID.
         */
        private final String uuid;

        /**
         * Current upload offset.
         */
        private final long offset;

        /**
         * Ctor.
         *
         * @param name Repository name.
         * @param uuid Upload UUID.
         * @param offset Current upload offset.
         */
        StatusResponse(final RepoName name, final String uuid, final long offset) {
            this.name = name;
            this.uuid = uuid;
            this.offset = offset;
        }

        @Override
        public CompletionStage<Void> send(final Connection connection) {
            return new RsWithHeaders(
                new RsWithStatus(RsStatus.ACCEPTED),
                new Header(
                    "Location",
                    String.format("/v2/%s/blobs/uploads/%s", this.name.value(), this.uuid)
                ),
                new Header("Range", String.format("0-%d", this.offset)),
                new Header("Content-Length", "0"),
                new Header("Docker-Upload-UUID", this.uuid)
            ).send(connection);
        }
    }
}
