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
import com.artipie.http.Headers;
import com.artipie.http.rs.Header;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link DigestHeader}.
 *
 * @since 0.2
 */
public final class DigestHeaderTest {

    @Test
    void shouldHaveExpectedNameAndValue() {
        final DigestHeader header = new DigestHeader(
            new Digest.Sha256(
                "6c3c624b58dbbcd3c0dd82b4c53f04194d1247c6eebdaab7c610cf7d66709b3b"
            )
        );
        MatcherAssert.assertThat(
            header.getKey(),
            new IsEqual<>("Docker-Content-Digest")
        );
        MatcherAssert.assertThat(
            header.getValue(),
            new IsEqual<>("sha256:6c3c624b58dbbcd3c0dd82b4c53f04194d1247c6eebdaab7c610cf7d66709b3b")
        );
    }

    @Test
    void shouldExtractValueFromHeaders() {
        final String digest = "sha256:123";
        final DigestHeader header = new DigestHeader(
            new Headers.From(
                new Header("Content-Type", "application/octet-stream"),
                new Header("docker-content-digest", digest),
                new Header("X-Something", "Some Value")
            )
        );
        MatcherAssert.assertThat(header.value().string(), new IsEqual<>(digest));
    }

    @Test
    void shouldFailToExtractValueFromEmptyHeaders() {
        Assertions.assertThrows(
            IllegalStateException.class,
            () -> new DigestHeader(Headers.EMPTY).value()
        );
    }
}
