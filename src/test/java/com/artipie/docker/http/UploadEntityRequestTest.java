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
import com.artipie.docker.RepoName;
import com.artipie.http.rq.RequestLine;
import com.artipie.http.rq.RqMethod;
import java.util.Optional;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.StringContains;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link UploadEntity.Request}.
 *
 * @since 0.2
 * @checkstyle ClassDataAbstractionCouplingCheck (2 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class UploadEntityRequestTest {

    @Test
    void shouldReadName() {
        final UploadEntity.Request request = new UploadEntity.Request(
            new RequestLine(RqMethod.POST, "/v2/my-repo/blobs/uploads/").toString()
        );
        MatcherAssert.assertThat(request.name().value(), new IsEqual<>("my-repo"));
    }

    @Test
    void shouldReadCompositeName() {
        final String name = "zero-one/two.three/four_five";
        MatcherAssert.assertThat(
            new UploadEntity.Request(
                new RequestLine(
                    RqMethod.POST, String.format("/v2/%s/blobs/uploads/", name)
                ).toString()
            ).name().value(),
            new IsEqual<>(name)
        );
    }

    @Test
    void shouldReadUuid() {
        final UploadEntity.Request request = new UploadEntity.Request(
            new RequestLine(
                RqMethod.PATCH,
                "/v2/my-repo/blobs/uploads/a9e48d2a-c939-441d-bb53-b3ad9ab67709"
            ).toString()
        );
        MatcherAssert.assertThat(
            request.uuid(),
            new IsEqual<>("a9e48d2a-c939-441d-bb53-b3ad9ab67709")
        );
    }

    @Test
    void shouldReadEmptyUuid() {
        final UploadEntity.Request request = new UploadEntity.Request(
            new RequestLine(RqMethod.PATCH, "/v2/my-repo/blobs/uploads//123").toString()
        );
        MatcherAssert.assertThat(
            request.uuid(),
            new IsEqual<>("")
        );
    }

    @Test
    void shouldReadDigest() {
        final UploadEntity.Request request = new UploadEntity.Request(
            new RequestLine(
                RqMethod.PUT,
                "/v2/my-repo/blobs/uploads/123-abc?digest=sha256:12345"
            ).toString()
        );
        MatcherAssert.assertThat(request.digest().string(), new IsEqual<>("sha256:12345"));
    }

    @Test
    void shouldThrowExceptionOnInvalidPath() {
        MatcherAssert.assertThat(
            Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new UploadEntity.Request(
                    new RequestLine(RqMethod.PUT, "/one/two").toString()
                ).name()
            ).getMessage(),
            new StringContains(false, "Unexpected path")
        );
    }

    @Test
    void shouldThrowExceptionWhenDigestIsAbsent() {
        MatcherAssert.assertThat(
            Assertions.assertThrows(
                IllegalStateException.class,
                () -> new UploadEntity.Request(
                    new RequestLine(
                        RqMethod.PUT,
                        "/v2/my-repo/blobs/uploads/123-abc?what=nothing"
                    ).toString()
                ).digest()
            ).getMessage(),
            new StringContains(false, "Unexpected query")
        );
    }

    @Test
    void shouldReadMountWhenPresent() {
        final UploadEntity.Request request = new UploadEntity.Request(
            new RequestLine(
                RqMethod.POST,
                "/v2/my-repo/blobs/uploads/?mount=sha256:12345&from=foo"
            ).toString()
        );
        MatcherAssert.assertThat(
            request.mount().map(Digest::string),
            new IsEqual<>(Optional.of("sha256:12345"))
        );
    }

    @Test
    void shouldReadMountWhenAbsent() {
        final UploadEntity.Request request = new UploadEntity.Request(
            new RequestLine(RqMethod.POST, "/v2/my-repo/blobs/uploads/").toString()
        );
        MatcherAssert.assertThat(
            request.mount().isPresent(),
            new IsEqual<>(false)
        );
    }

    @Test
    void shouldReadFromWhenPresent() {
        final UploadEntity.Request request = new UploadEntity.Request(
            new RequestLine(
                RqMethod.POST,
                "/v2/my-repo/blobs/uploads/?mount=sha256:12345&from=foo"
            ).toString()
        );
        MatcherAssert.assertThat(
            request.from().map(RepoName::value),
            new IsEqual<>(Optional.of("foo"))
        );
    }

    @Test
    void shouldReadFromWhenAbsent() {
        final UploadEntity.Request request = new UploadEntity.Request(
            new RequestLine(RqMethod.POST, "/v2/my-repo/blobs/uploads/").toString()
        );
        MatcherAssert.assertThat(
            request.from().isPresent(),
            new IsEqual<>(false)
        );
    }
}
