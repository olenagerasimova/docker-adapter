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
import com.artipie.docker.RepoName;
import com.artipie.docker.Tag;
import com.artipie.docker.Tags;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonString;

/**
 * Parsed {@link Tags} that is capable of extracting tags list and repository name
 * from origin {@link Tags}.
 *
 * @since 0.10
 */
public final class ParsedTags implements Tags {

    /**
     * Origin tags.
     */
    private final Tags origin;

    /**
     * Ctor.
     *
     * @param origin Origin tags.
     */
    public ParsedTags(final Tags origin) {
        this.origin = origin;
    }

    @Override
    public Content json() {
        return this.origin.json();
    }

    /**
     * Get repository name from origin.
     *
     * @return Repository name.
     */
    public CompletionStage<RepoName> repo() {
        return this.root().thenApply(root -> root.getString("name"))
            .thenApply(RepoName.Valid::new);
    }

    /**
     * Get tags list from origin.
     *
     * @return Tags list.
     */
    public CompletionStage<List<Tag>> tags() {
        return this.root().thenApply(root -> root.getJsonArray("tags")).thenApply(
            repos -> repos.getValuesAs(JsonString.class).stream()
                .map(JsonString::getString)
                .map(Tag.Valid::new)
                .collect(Collectors.toList())
        );
    }

    /**
     * Read JSON root object from origin.
     *
     * @return JSON root.
     */
    private CompletionStage<JsonObject> root() {
        return new PublisherAs(this.origin.json()).bytes().thenApply(
            bytes -> Json.createReader(new ByteArrayInputStream(bytes)).readObject()
        );
    }
}
