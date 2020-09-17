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
import com.artipie.http.auth.Action;
import com.artipie.http.auth.Authentication;
import com.artipie.http.auth.BasicIdentities;
import com.artipie.http.auth.Identities;
import com.artipie.http.auth.Permission;
import com.artipie.http.auth.Permissions;
import com.artipie.http.auth.SliceAuth;
import com.artipie.http.rq.RqMethod;
import com.artipie.http.rt.ByMethodsRule;
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
            new ErrorHandlingSlice(
                new SliceRoute(
                    new RtRulePath(
                        new RtRule.All(
                            new RtRule.ByPath(BaseEntity.PATH),
                            ByMethodsRule.Standard.GET
                        ),
                        new BaseEntity(ids)
                    ),
                    new RtRulePath(
                        new RtRule.All(
                            new RtRule.ByPath(ManifestEntity.PATH),
                            new ByMethodsRule(RqMethod.HEAD)
                        ),
                        authRead(new ManifestEntity.Head(docker), perms, ids)
                    ),
                    new RtRulePath(
                        new RtRule.All(
                            new RtRule.ByPath(ManifestEntity.PATH),
                            ByMethodsRule.Standard.GET
                        ),
                        authRead(new ManifestEntity.Get(docker), perms, ids)
                    ),
                    new RtRulePath(
                        new RtRule.All(
                            new RtRule.ByPath(ManifestEntity.PATH),
                            ByMethodsRule.Standard.PUT
                        ),
                        authWrite(new ManifestEntity.Put(docker), perms, ids)
                    ),
                    new RtRulePath(
                        new RtRule.All(
                            new RtRule.ByPath(BlobEntity.PATH),
                            new ByMethodsRule(RqMethod.HEAD)
                        ),
                        authRead(new BlobEntity.Head(docker), perms, ids)
                    ),
                    new RtRulePath(
                        new RtRule.All(
                            new RtRule.ByPath(BlobEntity.PATH),
                            ByMethodsRule.Standard.GET
                        ),
                        authRead(new BlobEntity.Get(docker), perms, ids)
                    ),
                    new RtRulePath(
                        new RtRule.All(
                            new RtRule.ByPath(UploadEntity.PATH),
                            ByMethodsRule.Standard.POST
                        ),
                        authWrite(new UploadEntity.Post(docker), perms, ids)
                    ),
                    new RtRulePath(
                        new RtRule.All(
                            new RtRule.ByPath(UploadEntity.PATH),
                            new ByMethodsRule(RqMethod.PATCH)
                        ),
                        authWrite(new UploadEntity.Patch(docker), perms, ids)
                    ),
                    new RtRulePath(
                        new RtRule.All(
                            new RtRule.ByPath(UploadEntity.PATH),
                            ByMethodsRule.Standard.PUT
                        ),
                        authWrite(new UploadEntity.Put(docker), perms, ids)
                    ),
                    new RtRulePath(
                        new RtRule.All(
                            new RtRule.ByPath(UploadEntity.PATH),
                            ByMethodsRule.Standard.GET
                        ),
                        authRead(new UploadEntity.Get(docker), perms, ids)
                    )
                )
            )
        );
    }

    /**
     * Requires authentication and read permission for slice.
     *
     * @param origin Origin slice.
     * @param perms Access permissions.
     * @param ids Authentication mechanism.
     * @return Authorized slice.
     */
    private static Slice authRead(
        final Slice origin,
        final Permissions perms,
        final Identities ids
    ) {
        return new DockerAuthSlice(
            new SliceAuth(
                origin,
                new Permission.ByName(perms, Action.Standard.READ),
                ids
            )
        );
    }

    /**
     * Requires authentication and write permission for slice.
     *
     * @param origin Origin slice.
     * @param perms Access permissions.
     * @param ids Authentication mechanism.
     * @return Authorized slice.
     */
    private static Slice authWrite(
        final Slice origin,
        final Permissions perms,
        final Identities ids
    ) {
        return new DockerAuthSlice(
            new SliceAuth(
                origin,
                new Permission.ByName(perms, Action.Standard.WRITE),
                ids
            )
        );
    }
}
