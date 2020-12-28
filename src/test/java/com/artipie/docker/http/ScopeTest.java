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

import com.artipie.docker.RepoName;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Scope}.
 *
 * @since 0.10
 */
class ScopeTest {

    @Test
    void pullScope() {
        MatcherAssert.assertThat(
            new Scope.Repository.Pull(new RepoName.Valid("samalba/my-app")).string(),
            new IsEqual<>("repository:samalba/my-app:pull")
        );
    }

    @Test
    void pushScope() {
        MatcherAssert.assertThat(
            new Scope.Repository.Push(new RepoName.Valid("busybox")).string(),
            new IsEqual<>("repository:busybox:push")
        );
    }

    @Test
    void scopeFromString() {
        final Scope scope = new Scope.FromString("repository:my-alpine:pull");
        MatcherAssert.assertThat(
            "Has expected type",
            scope.type(),
            new IsEqual<>("repository")
        );
        MatcherAssert.assertThat(
            "Has expected name",
            scope.name(),
            new IsEqual<>("my-alpine")
        );
        MatcherAssert.assertThat(
            "Has expected action",
            scope.action(),
            new IsEqual<>("pull")
        );
    }

    @Test
    void scopeFromInvalidString() {
        final Scope scope = new Scope.FromString("something");
        Assertions.assertThrows(
            IllegalStateException.class,
            scope::name,
            "Name cannot be parsed"
        );
        Assertions.assertThrows(
            IllegalStateException.class,
            scope::action,
            "Action cannot be parsed"
        );
    }
}
