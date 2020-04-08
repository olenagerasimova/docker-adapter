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
package com.artipie.docker.manifest;

import com.artipie.asto.Concatenation;
import com.artipie.asto.Content;
import com.artipie.asto.Remaining;
import hu.akarnokd.rxjava2.interop.SingleInterop;
import java.io.ByteArrayInputStream;
import java.util.concurrent.CompletionStage;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

/**
 * Image manifest in JSON format.
 *
 * @since 0.2
 */
public final class JsonManifest implements Manifest {

    /**
     * JSON bytes.
     */
    private final Content source;

    /**
     * Ctor.
     *
     * @param source JSON bytes.
     */
    public JsonManifest(final Content source) {
        this.source = source;
    }

    @Override
    public CompletionStage<String> mediaType() {
        return this.json().thenApply(root -> root.getString("mediaType"));
    }

    @Override
    public Content content() {
        return this.source;
    }

    /**
     * Reads content as JSON object.
     *
     * @return JSON object.
     */
    private CompletionStage<JsonObject> json() {
        return new Concatenation(this.source)
            .single()
            .map(buf -> new Remaining(buf, true))
            .map(Remaining::bytes)
            .map(ByteArrayInputStream::new)
            .map(
                stream -> {
                    try (JsonReader reader = Json.createReader(stream)) {
                        return reader.readObject();
                    }
                }
            )
            .to(SingleInterop.get());
    }
}
