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
package com.artipie.docker.fake;

import com.artipie.asto.Content;
import com.artipie.docker.Blob;
import com.artipie.docker.Digest;
import com.artipie.docker.Layers;
import com.artipie.docker.cache.CacheLayers;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

/**
 * Auxiliary class for tests for {@link com.artipie.docker.cache.CacheLayers}.
 *
 * @since 0.5
 */
public final class FakeLayers implements Layers {
    /**
     * Layers.
     */
    private final Layers layers;

    /**
     * Ctor.
     *
     * @param type Type of layers.
     */
    public FakeLayers(String type) {
        layers = layersFromType(type);
    }

    /**
     * Creates layers.
     *
     * @return Layers.
     */
    private Layers layersFromType(String type) {
        final Layers layers;
        switch (type) {
            case "empty":
                layers = new EmptyGetLayers();
                break;
            case "full":
                layers = new FullGetLayers();
                break;
            case "faulty":
                layers = new FaultyGetLayers();
                break;
            default:
                throw new IllegalArgumentException(
                    String.format("Unsupported type: %s", type)
                );
        }
        return layers;
    }

    @Override
    public CompletionStage<Blob> put(Content content, Digest digest) {
        return layers.put(content, digest);
    }

    @Override
    public CompletionStage<Optional<Blob>> get(Digest digest) {
        return layers.get(digest);
    }
}
