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
import com.artipie.docker.Layers;
import com.artipie.docker.Manifests;
import com.artipie.docker.Repo;
import com.artipie.docker.RepoName;
import com.artipie.docker.Uploads;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link TrimmedDocker}.
 * @since 0.4
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class TrimmedDockerTest {

    /**
     * Fake docker.
     */
    private static final Docker FAKE = FakeRepo::new;

    @Test
    void failsIfPrefixNotFound() {
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> new TrimmedDocker(TrimmedDockerTest.FAKE, "abc/123")
                .repo(new RepoName.Simple("xfe/oiu"))
        );
    }

    @Test
    void cutsIfPrefixStartsWithSlash() {
        final String name = "my/repo";
        final String prefix = "/abc/123";
        MatcherAssert.assertThat(
            ((FakeRepo) new TrimmedDocker(TrimmedDockerTest.FAKE, prefix)
                .repo(new RepoName.Simple(String.format("%s/%s", prefix, name)))).name(),
            new IsEqual<>(name)
        );
    }

    @Test
    void cutsIfPrefixEndsWithSlash() {
        final String name = "your/second/repo";
        final String prefix = "abc/123/";
        MatcherAssert.assertThat(
            ((FakeRepo) new TrimmedDocker(TrimmedDockerTest.FAKE, prefix)
                .repo(new RepoName.Simple(String.format("%s/%s", prefix, name)))).name(),
            new IsEqual<>(name)
        );
    }

    @Test
    void cutsIfPrefixStartsAndEndsWithSlash() {
        final String name = "my/another/repo";
        final String prefix = "/def/";
        MatcherAssert.assertThat(
            ((FakeRepo) new TrimmedDocker(TrimmedDockerTest.FAKE, prefix)
                .repo(new RepoName.Simple(String.format("%s/%s", prefix, name)))).name(),
            new IsEqual<>(name)
        );
    }

    @Test
    void cutsIfRepoNameContainsOnlyOnePart() {
        final String name = "repo";
        final String prefix = "abc/def/123";
        MatcherAssert.assertThat(
            ((FakeRepo) new TrimmedDocker(TrimmedDockerTest.FAKE, prefix)
                .repo(new RepoName.Simple(String.format("%s/%s", prefix, name)))).name(),
            new IsEqual<>(name)
        );
    }

    /**
     * Fake repo.
     * @since 0.4
     */
    static final class FakeRepo implements Repo {

        /**
         * Repo name.
         */
        private final RepoName rname;

        /**
         * Ctor.
         * @param name Repo name
         */
        FakeRepo(final RepoName name) {
            this.rname = name;
        }

        @Override
        public Layers layers() {
            return null;
        }

        @Override
        public Manifests manifests() {
            return null;
        }

        @Override
        public Uploads uploads() {
            return null;
        }

        /**
         * Name of the repo.
         * @return Name
         */
        public String name() {
            return this.rname.value();
        }
    }

}
