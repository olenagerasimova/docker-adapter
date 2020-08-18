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

/**
 * Docker image for {@link DockerSliceITCase}.
 * The field values depend on the current OS.
 *
 * @since 0.5
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
final class DockerImagePreparation {
    /**
     * Image name.
     */
    private String imgname;

    /**
     * Manifest digest.
     */
    private String mnfdigest;

    /**
     * Short layer hash.
     */
    private String layerhash;

    /**
     * Image name.
     *
     * @return Image name.
     */
    String name() {
        return this.imgname;
    }

    /**
     * Manifest digest.
     *
     * @return Manifest digest.
     */
    String digest() {
        return this.mnfdigest;
    }

    /**
     * Layer hash.
     *
     * @return Layer hash.
     */
    String layer() {
        return this.layerhash;
    }

    /**
     * Prepares the image depending on the OS.
     */
    void prepareImage() {
        if (System.getProperty("os.name").startsWith("Windows")) {
            this.windowsImage();
        } else {
            this.linuxImage();
        }
    }

    /**
     * Prepare `mcr.microsoft.com/dotnet/core/runtime:3.1.4-nanoserver-1809` image
     * for Windows Server 2019 amd64 architecture.
     */
    private void windowsImage() {
        this.setValues(
            "mcr.microsoft.com/dotnet/core/runtime",
            String.format(
                "%s:%s",
                "sha256",
                "c91e7b0fcc21d5ee1c7d3fad7e31c71ed65aa59f448f7dcc1756153c724c8b07"
            ),
            "d9e06d032060"
        );
    }

    /**
     * Prepare `amd64/busybox:1.31.1` image for linux/amd64 architecture.
     */
    private void linuxImage() {
        this.setValues(
            "busybox",
            String.format(
                "%s:%s",
                "sha256",
                "a7766145a775d39e53a713c75b6fd6d318740e70327aaa3ed5d09e0ef33fc3df"
            ),
            "1079c30efc82"
        );
    }

    /**
     * Set underlying fields of the image.
     *
     * @param name Image name.
     * @param digest Manifest digest.
     * @param layer Layer hash.
     */
    private void setValues(final String name, final String digest, final String layer) {
        this.imgname = name;
        this.mnfdigest = digest;
        this.layerhash = layer;
    }
}
