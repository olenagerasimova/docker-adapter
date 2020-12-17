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
package com.artipie.docker.misc;

import com.artipie.asto.Content;
import com.artipie.asto.ext.PublisherAs;
import com.artipie.docker.Catalog;
import com.artipie.docker.RepoName;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import javax.json.Json;
import javax.json.JsonString;

/**
 * Parsed {@link Catalog} that is capable of extracting repository names list
 * from origin {@link Catalog}.
 *
 * @since 0.10
 */
public final class ParsedCatalog implements Catalog {

    /**
     * Origin catalog.
     */
    private final Catalog origin;

    /**
     * Ctor.
     *
     * @param origin Origin catalog.
     */
    public ParsedCatalog(final Catalog origin) {
        this.origin = origin;
    }

    @Override
    public Content json() {
        return this.origin.json();
    }

    /**
     * Get repository names list from origin catalog.
     *
     * @return Repository names list.
     */
    public CompletionStage<List<RepoName>> repos() {
        return new PublisherAs(this.origin.json()).bytes().thenApply(
            bytes -> Json.createReader(new ByteArrayInputStream(bytes)).readObject()
        ).thenApply(root -> root.getJsonArray("repositories")).thenApply(
            repos -> repos.getValuesAs(JsonString.class).stream()
                .map(JsonString::getString)
                .map(RepoName.Valid::new)
                .collect(Collectors.toList())
        );
    }
}
