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
package com.artipie.docker.proxy;

import com.artipie.docker.RepoName;
import com.artipie.docker.Tag;
import com.google.common.base.Joiner;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * URI for tags list resource.
 *
 * @since 0.10
 */
final class TagsListUri {

    /**
     * Repository name.
     */
    private final RepoName name;

    /**
     * From which tag to start, exclusive.
     */
    private final Optional<Tag> from;

    /**
     * Maximum number of tags returned.
     */
    private final int limit;

    /**
     * Ctor.
     *
     * @param name Repository name.
     * @param from From which tag to start, exclusive.
     * @param limit Maximum number of tag returned.
     */
    TagsListUri(final RepoName name, final Optional<Tag> from, final int limit) {
        this.name = name;
        this.from = from;
        this.limit = limit;
    }

    /**
     * Build URI string.
     *
     * @return URI string.
     */
    public String string() {
        final Stream<String> nparam;
        if (this.limit < Integer.MAX_VALUE) {
            nparam = Stream.of(String.format("n=%d", this.limit));
        } else {
            nparam = Stream.empty();
        }
        final List<String> params = Stream.concat(
            nparam,
            this.from.map(last -> Stream.of(String.format("last=%s", last.value())))
                .orElseGet(Stream::empty)
        ).collect(Collectors.toList());
        final StringBuilder uri = new StringBuilder("/v2/")
            .append(this.name.value())
            .append("/tags/list");
        if (!params.isEmpty()) {
            uri.append(String.format("?%s", Joiner.on("&").join(params)));
        }
        return uri.toString();
    }
}
