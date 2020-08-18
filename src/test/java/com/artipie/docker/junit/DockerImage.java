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
package com.artipie.docker.junit;

import com.artipie.docker.http.DockerSliceITCase;
import org.hamcrest.Matcher;
import org.hamcrest.core.StringContains;

/**
 * Docker image for {@link DockerSliceITCase}.
 *
 * @since 0.5
 */
@SuppressWarnings("PMD.ConstructorOnlyInitializesOrCallOtherConstructors")
public final class DockerImage {
    /**
     * Repo URL.
     */
    private final String repo;

    /**
     * Manifest digest.
     */
    private String mnfdigest;

    /**
     * Short layer hash.
     */
    private String layer;

    /**
     * Image name.
     */
    private String imgname;

    /**
     * Ctor.
     *
     * @param repo Repository name.
     */
    public DockerImage(final String repo) {
        this.repo = repo;
    }

    /**
     * Initialize fields of this object.
     */
    public void initialize() {
        final DockerImagePreparation tmp = new DockerImagePreparation();
        tmp.prepareImage();
        this.imgname = tmp.name();
        this.mnfdigest = tmp.digest();
        this.layer = tmp.layer();
    }

    /**
     * Manifest digest.
     *
     * @return Manifest digest.
     */
    public String digest() {
        return this.mnfdigest;
    }

    /**
     * Update image name.
     *
     * @param name New name.
     */
    public void updateName(final String name) {
        this.imgname = name;
    }

    /**
     * Image name.
     *
     * @return Image name.
     */
    public String local() {
        return this.imgname;
    }

    /**
     * Remote name.
     *
     * @return Remote name.
     */
    public String remote() {
        return String.format("%s/%s", this.repo, this.local());
    }

    /**
     * Remote by digest.
     *
     * @return Remote by digest.
     */
    public String remoteByDigest() {
        return String.format("%s@%s", this.remote(), this.mnfdigest);
    }

    /**
     * Matcher for pushed manifest.
     *
     * @return Matcher for pushed manifest.
     */
    public Matcher<String> manifestPushed() {
        return new StringContains(false, String.format("latest: digest: %s", this.mnfdigest));
    }

    /**
     * Matcher for pushed layers.
     *
     * @return Matcher for pushed layers.
     */
    public Matcher<String> layersPushed() {
        return new StringContains(false, String.format("%s: Pushed", this.layer));
    }

    /**
     * Matcher for already exist layers.
     *
     * @return Matcher for already exist layers.
     */
    public Matcher<String> layersAlreadyExist() {
        return new StringContains(false, String.format("%s: Layer already exists", this.layer));
    }
}
