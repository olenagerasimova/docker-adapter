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
package com.artipie.docker;

import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests for {@link Tag.Valid}.
 *
 * @since 0.1
 */
class TagValidTest {

    @ParameterizedTest
    @ValueSource(strings = {
        "latest",
        "1.0",
        "my-tag",
        "MY_TAG",
        "My.Tag.1",
        "_some_tag",
        //@checkstyle LineLengthCheck (1 line)
        "01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567"
    })
    void shouldGetValueWhenValid(final String original) {
        final Tag tag = new Tag.Valid(original);
        MatcherAssert.assertThat(tag.value(), new IsEqual<>(original));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "",
        "*",
        "\u00ea",
        "-my-tag",
        //@checkstyle LineLengthCheck (1 line)
        "012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678"
    })
    void shouldFailToGetValueWhenInvalid(final String original) {
        final Tag tag = new Tag.Valid(original);
        Assertions.assertThrows(IllegalStateException.class, tag::value);
    }
}
