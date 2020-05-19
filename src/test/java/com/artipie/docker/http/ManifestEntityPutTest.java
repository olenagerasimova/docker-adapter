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

import com.artipie.asto.memory.InMemoryStorage;
import com.artipie.docker.asto.AstoDocker;
import com.artipie.http.hm.RsHasHeaders;
import com.artipie.http.hm.RsHasStatus;
import com.artipie.http.rq.RequestLine;
import com.artipie.http.rs.Header;
import com.artipie.http.rs.RsStatus;
import io.reactivex.Flowable;
import java.nio.ByteBuffer;
import java.util.Collections;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link DockerSlice}.
 * Manifest PUT endpoint.
 *
 * @checkstyle ClassDataAbstractionCouplingCheck (2 lines)
 * @since 0.2
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class ManifestEntityPutTest {

    /**
     * Slice being tested.
     */
    private DockerSlice slice;

    @BeforeEach
    void setUp() {
        this.slice = new DockerSlice(new AstoDocker(new InMemoryStorage()));
    }

    @Test
    void shouldPushManifestByTag() {
        final String path = "/v2/my-alpine/manifests/1";
        MatcherAssert.assertThat(
            this.slice.response(
                new RequestLine("PUT", path, "HTTP/1.1").toString(),
                Collections.emptyList(),
                this.manifest()
            ),
            Matchers.allOf(
                new RsHasStatus(RsStatus.CREATED),
                new RsHasHeaders(
                    new Header("Location", path),
                    new Header("Content-Length", "0"),
                    new Header(
                        "Docker-Content-Digest",
                        "sha256:c824a9aa7d2e3471306648c6d4baa1abbcb97ff0276181ab4722ca27127cdba0"
                    )
                )
            )
        );
    }

    @Test
    void shouldPushManifestByDigest() {
        final String digest = String.format(
            "%s:%s",
            "sha256",
            "c824a9aa7d2e3471306648c6d4baa1abbcb97ff0276181ab4722ca27127cdba0"
        );
        final String path = String.format("/v2/my-alpine/manifests/%s", digest);
        MatcherAssert.assertThat(
            this.slice.response(
                new RequestLine("PUT", path, "HTTP/1.1").toString(),
                Collections.emptyList(),
                this.manifest()
            ),
            Matchers.allOf(
                new RsHasStatus(RsStatus.CREATED),
                new RsHasHeaders(
                    new Header("Location", path),
                    new Header("Content-Length", "0"),
                    new Header("Docker-Content-Digest", digest)
                )
            )
        );
    }

    private Flowable<ByteBuffer> manifest() {
        return Flowable.just(ByteBuffer.wrap("{\"layers\":[]}".getBytes()));
    }
}
