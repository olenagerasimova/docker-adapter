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

import com.artipie.docker.error.DockerError;
import com.artipie.http.Response;
import com.artipie.http.rs.RsStatus;
import com.artipie.http.rs.RsWithBody;
import com.artipie.http.rs.RsWithHeaders;
import com.artipie.http.rs.RsWithStatus;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;

/**
 * Docker errors response.
 *
 * @since 0.5
 */
final class ErrorsResponse extends Response.Wrap {

    /**
     * Charset used for JSON encoding.
     */
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    /**
     * Ctor.
     *
     * @param status Response status.
     * @param errors Errors.
     */
    protected ErrorsResponse(final RsStatus status, final DockerError... errors) {
        this(status, Arrays.asList(errors));
    }

    /**
     * Ctor.
     *
     * @param status Response status.
     * @param errors Errors.
     */
    protected ErrorsResponse(final RsStatus status, final Collection<DockerError> errors) {
        super(
            new RsWithBody(
                new RsWithHeaders(
                    new RsWithStatus(status),
                    new JsonContentType(ErrorsResponse.CHARSET)
                ),
                json(errors),
                ErrorsResponse.CHARSET
            )
        );
    }

    /**
     * Represent error in JSON format.
     *
     * @param errors Errors.
     * @return JSON string.
     */
    private static String json(final Collection<DockerError> errors) {
        final JsonArrayBuilder array = Json.createArrayBuilder();
        for (final DockerError error : errors) {
            final JsonObjectBuilder obj = Json.createObjectBuilder()
                .add("code", error.code())
                .add("message", error.message());
            error.detail().ifPresent(detail -> obj.add("detail", detail));
            array.add(obj);
        }
        return Json.createObjectBuilder().add("errors", array).build().toString();
    }
}
