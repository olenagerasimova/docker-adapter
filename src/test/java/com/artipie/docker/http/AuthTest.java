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
import com.artipie.asto.memory.InMemoryStorage;
import com.artipie.docker.asto.AstoDocker;
import com.artipie.http.Headers;
import com.artipie.http.Response;
import com.artipie.http.auth.Permissions;
import com.artipie.http.headers.Authorization;
import com.artipie.http.hm.RsHasStatus;
import com.artipie.http.rq.RequestLine;
import com.artipie.http.rq.RqMethod;
import com.artipie.http.rs.RsStatus;
import java.util.Arrays;
import java.util.stream.Stream;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.AllOf;
import org.hamcrest.core.IsNot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests for {@link DockerSlice}.
 * Authentication & authorization tests.
 *
 * @since 0.8
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class AuthTest {

    /**
     * Slice being tested.
     */
    private DockerSlice slice;

    @BeforeEach
    void setUp() {
        this.slice = new DockerSlice(
            new AstoDocker(new InMemoryStorage()),
            new Permissions.Single(TestAuthentication.ALICE.name(), "read"),
            new TestAuthentication()
        );
    }

    @ParameterizedTest
    @MethodSource("lines")
    void shouldReturnUnauthorizedWhenNoAuth(final RequestLine line) {
        MatcherAssert.assertThat(
            this.slice.response(line.toString(), Headers.EMPTY, Content.EMPTY),
            new IsUnauthorizedResponse()
        );
    }

    @ParameterizedTest
    @MethodSource("lines")
    void shouldReturnUnauthorizedWhenUserIsUnknown(final RequestLine line) {
        MatcherAssert.assertThat(
            this.slice.response(
                line.toString(),
                new Headers.From(new Authorization.Basic("chuck", "letmein")),
                Content.EMPTY
            ),
            new IsUnauthorizedResponse()
        );
    }

    @ParameterizedTest
    @MethodSource("lines")
    void shouldReturnForbiddenWhenUserHasNoRequiredPermissions(final RequestLine line) {
        MatcherAssert.assertThat(
            this.slice.response(
                line.toString(),
                TestAuthentication.BOB.headers(),
                Content.EMPTY
            ),
            new IsDeniedResponse()
        );
    }

    @ParameterizedTest
    @MethodSource("lines")
    void shouldNotReturnUnauthorizedOrForbiddenWhenUserHasPermissions(final RequestLine line) {
        final Response response = this.slice.response(
            line.toString(),
            TestAuthentication.ALICE.headers(),
            Content.EMPTY
        );
        MatcherAssert.assertThat(
            response,
            new AllOf<>(
                Arrays.asList(
                    new IsNot<>(new RsHasStatus(RsStatus.FORBIDDEN)),
                    new IsNot<>(new RsHasStatus(RsStatus.UNAUTHORIZED))
                )
            )
        );
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private static Stream<Arguments> lines() {
        return Stream.of(
            new RequestLine(RqMethod.GET, "/v2/"),
            new RequestLine(RqMethod.HEAD, "/v2/test/blobs/sha256:123"),
            new RequestLine(RqMethod.GET, "/v2/test/blobs/sha256:012345"),
            new RequestLine(RqMethod.HEAD, "/v2/my-alpine/manifests/1"),
            new RequestLine(RqMethod.GET, "/v2/my-alpine/manifests/2"),
            new RequestLine(RqMethod.GET, "/v2/my-alpine/tags/list"),
            new RequestLine(RqMethod.GET, "/v2/my-alpine/blobs/uploads/112233"),
            new RequestLine(RqMethod.GET, "/v2/_catalog")
        ).map(Arguments::of);
    }
}
