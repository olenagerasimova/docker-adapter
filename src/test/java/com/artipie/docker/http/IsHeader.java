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

import java.util.Map;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsEqual;
import org.hamcrest.text.IsEqualIgnoringCase;

/**
 * Header matcher.
 *
 * @since 0.2
 */
public final class IsHeader extends TypeSafeMatcher<Map.Entry<String, String>> {

    /**
     * Name matcher.
     */
    private final Matcher<String> name;

    /**
     * Value matcher.
     */
    private final Matcher<String> value;

    /**
     * Ctor.
     *
     * @param name Expected header name, compared ignoring case.
     * @param value Expected header value.
     */
    public IsHeader(final String name, final String value) {
        this(name, new IsEqual<>(value));
    }

    /**
     * Ctor.
     *
     * @param name Expected header name, compared ignoring case.
     * @param value Value matcher.
     */
    public IsHeader(final String name, final Matcher<String> value) {
        this(new IsEqualIgnoringCase(name), value);
    }

    /**
     * Ctor.
     *
     * @param name Name matcher.
     * @param value Value matcher.
     */
    public IsHeader(final Matcher<String> name, final Matcher<String> value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public void describeTo(final Description description) {
        description.appendDescriptionOf(this.name)
            .appendText(" ")
            .appendDescriptionOf(this.value);
    }

    @Override
    public boolean matchesSafely(final Map.Entry<String, String> item) {
        return this.name.matches(item.getKey()) && this.value.matches(item.getValue());
    }
}
