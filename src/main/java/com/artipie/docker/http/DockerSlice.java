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
import com.artipie.http.Slice;
import com.artipie.http.auth.Authentication;
import com.artipie.http.auth.BasicIdentities;
import com.artipie.http.auth.Identities;
import com.artipie.http.auth.Permissions;
import com.artipie.http.rq.RqMethod;
import com.artipie.http.rt.RtRule;
import com.artipie.http.rt.RtRulePath;
import com.artipie.http.rt.SliceRoute;

/**
 * Slice implementing Docker Registry HTTP API.
 * See <a href="https://docs.docker.com/registry/spec/api/">Docker Registry HTTP API V2</a>.
 *
 * @since 0.1
 * @checkstyle ClassDataAbstractionCouplingCheck (2 lines)
 */
public final class DockerSlice extends Slice.Wrap {

    /**
     * Ctor.
     *
     * @param docker Docker repository.
     */
    public DockerSlice(final Docker docker) {
        this(docker, Permissions.FREE, Identities.ANONYMOUS);
    }

    /**
     * Ctor.
     *
     * @param docker Docker repository.
     * @param perms Access permissions.
     * @param auth Authentication mechanism.
     */
    public DockerSlice(final Docker docker, final Permissions perms, final Authentication auth) {
        this(docker, perms, new BasicIdentities(auth));
    }

    /**
     * Ctor.
     *
     * @param docker Docker repository.
     * @param perms Access permissions.
     * @param ids User identities.
     */
    @SuppressWarnings("PMD.UnusedFormalParameter")
    public DockerSlice(final Docker docker, final Permissions perms, final Identities ids) {
        super(
            new SliceRoute(
                new RtRulePath(
                    new RtRule.All(
                        new RtRule.ByPath(BaseEntity.PATH),
                        new RtRule.ByMethod(RqMethod.GET)
                    ),
                    new BaseEntity()
                ),
                new RtRulePath(
                    new RtRule.All(
                        new RtRule.ByPath(ManifestEntity.PATH),
                        new RtRule.ByMethod(RqMethod.HEAD)
                    ),
                    new ManifestEntity.Head(docker)
                ),
                new RtRulePath(
                    new RtRule.All(
                        new RtRule.ByPath(ManifestEntity.PATH),
                        new RtRule.ByMethod(RqMethod.GET)
                    ),
                    new ManifestEntity.Get(docker)
                ),
                new RtRulePath(
                    new RtRule.All(
                        new RtRule.ByPath(ManifestEntity.PATH),
                        new RtRule.ByMethod(RqMethod.PUT)
                    ),
                    new ManifestEntity.Put(docker)
                ),
                new RtRulePath(
                    new RtRule.All(
                        new RtRule.ByPath(BlobEntity.PATH),
                        new RtRule.ByMethod(RqMethod.HEAD)
                    ),
                    new BlobEntity.Head(docker)
                ),
                new RtRulePath(
                    new RtRule.All(
                        new RtRule.ByPath(BlobEntity.PATH),
                        new RtRule.ByMethod(RqMethod.GET)
                    ),
                    new BlobEntity.Get(docker)
                ),
                new RtRulePath(
                    new RtRule.All(
                        new RtRule.ByPath(UploadEntity.PATH),
                        new RtRule.ByMethod(RqMethod.POST)
                    ),
                    new UploadEntity.Post(docker)
                ),
                new RtRulePath(
                    new RtRule.All(
                        new RtRule.ByPath(UploadEntity.PATH),
                        new RtRule.ByMethod(RqMethod.PATCH)
                    ),
                    new UploadEntity.Patch(docker)
                ),
                new RtRulePath(
                    new RtRule.All(
                        new RtRule.ByPath(UploadEntity.PATH),
                        new RtRule.ByMethod(RqMethod.PUT)
                    ),
                    new UploadEntity.Put(docker)
                ),
                new RtRulePath(
                    new RtRule.All(
                        new RtRule.ByPath(UploadEntity.PATH),
                        new RtRule.ByMethod(RqMethod.GET)
                    ),
                    new UploadEntity.Get(docker)
                )
            )
        );
    }
}
