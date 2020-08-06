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
package com.artipie.docker.error;

import com.artipie.docker.ref.ManifestRef;
import java.util.Optional;

/**
 * This error is returned when the manifest, identified by name and tag
 * is unknown to the repository.
 *
 * @since 0.5
 */
public final class ManifestError implements DockerError {

    /**
     * Manifest reference.
     */
    private final ManifestRef ref;

    /**
     * Ctor.
     *
     * @param ref Manifest reference.
     */
    public ManifestError(final ManifestRef ref) {
        this.ref = ref;
    }

    @Override
    public String code() {
        return "MANIFEST_UNKNOWN";
    }

    @Override
    public String message() {
        return "manifest unknown";
    }

    @Override
    public Optional<String> detail() {
        return Optional.of(this.ref.string());
    }
}
