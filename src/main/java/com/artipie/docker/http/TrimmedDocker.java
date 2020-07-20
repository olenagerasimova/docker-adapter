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
import com.artipie.docker.Repo;
import com.artipie.docker.RepoName;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of {@link Docker} to remove given prefix from repository names.
 * @since 0.4
 */
public final class TrimmedDocker implements Docker {

    /**
     * Docker origin.
     */
    private final Docker origin;

    /**
     * Regex to cut prefix from repository name.
     */
    private final Pattern prefix;

    /**
     * Ctor.
     * @param origin Docker origin
     * @param prefix Prefix to cut
     */
    public TrimmedDocker(final Docker origin, final String prefix) {
        this(
            origin,
            Pattern.compile(String.format("^/?(?:%s\\/?)(.*)", prefix))
        );
    }

    /**
     * Ctor.
     * @param origin Docker origin
     * @param prefix Prefix to cut
     */
    public TrimmedDocker(final Docker origin, final Pattern prefix) {
        this.origin = origin;
        this.prefix = prefix;
    }

    @Override
    public Repo repo(final RepoName name) {
        final Matcher matcher = this.prefix.matcher(name.value());
        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                String.format(
                    "Invalid image name: name `%s` must match `%s`",
                    name.value(), this.prefix.pattern()
                )
            );
        }
        return this.origin.repo(new RepoName.Valid(matcher.group(1)));
    }
}
