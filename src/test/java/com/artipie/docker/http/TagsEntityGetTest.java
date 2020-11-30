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
import com.artipie.docker.Catalog;
import com.artipie.docker.Docker;
import com.artipie.docker.Layers;
import com.artipie.docker.Manifests;
import com.artipie.docker.Repo;
import com.artipie.docker.RepoName;
import com.artipie.docker.Uploads;
import com.artipie.docker.fake.FullTagsManifests;
import com.artipie.http.Headers;
import com.artipie.http.headers.ContentLength;
import com.artipie.http.headers.ContentType;
import com.artipie.http.hm.ResponseMatcher;
import com.artipie.http.hm.SliceHasResponse;
import com.artipie.http.rq.RequestLine;
import com.artipie.http.rq.RqMethod;
import com.artipie.http.rs.RsStatus;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicReference;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link DockerSlice}.
 * Tags list GET endpoint.
 *
 * @since 0.8
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
class TagsEntityGetTest {

    @Test
    void shouldReturnTags() {
        final byte[] tags = "{...}".getBytes();
        final FakeDocker docker = new FakeDocker(
            new FullTagsManifests(() -> new Content.From(tags))
        );
        MatcherAssert.assertThat(
            "Responds with tags",
            new DockerSlice(docker),
            new SliceHasResponse(
                new ResponseMatcher(
                    RsStatus.OK,
                    new Headers.From(
                        new ContentLength(tags.length),
                        new ContentType("application/json; charset=utf-8")
                    ),
                    tags
                ),
                new RequestLine(RqMethod.GET, "/v2/my-alpine/tags/list")
            )
        );
        MatcherAssert.assertThat(
            "Gets tags for expected repository name",
            docker.capture.get().value(),
            new IsEqual<>("my-alpine")
        );
    }

    /**
     * Docker implementation that returns repository with specified manifests
     * and captures repository name.
     *
     * @since 0.8
     */
    private static class FakeDocker implements Docker {

        /**
         * Repository manifests.
         */
        private final Manifests manifests;

        /**
         * Captured repository name.
         */
        private final AtomicReference<RepoName> capture;

        FakeDocker(final Manifests manifests) {
            this.manifests = manifests;
            this.capture = new AtomicReference<>();
        }

        @Override
        public Repo repo(final RepoName name) {
            this.capture.set(name);
            return new Repo() {
                @Override
                public Layers layers() {
                    throw new UnsupportedOperationException();
                }

                @Override
                public Manifests manifests() {
                    return FakeDocker.this.manifests;
                }

                @Override
                public Uploads uploads() {
                    throw new UnsupportedOperationException();
                }
            };
        }

        @Override
        public CompletionStage<Catalog> catalog(final Optional<RepoName> from, final int limit) {
            throw new UnsupportedOperationException();
        }
    }
}
