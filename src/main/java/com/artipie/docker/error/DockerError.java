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

import java.util.Optional;

/**
 * Docker registry error.
 * See <a href="https://docs.docker.com/registry/spec/api/#errors">Errors</a>.
 * Full list of errors could be found
 * <a href="https://docs.docker.com/registry/spec/api/#errors-2">here</a>.
 *
 * @since 0.5
 */
public interface DockerError {

    /**
     * Get code.
     *
     * @return Code identifier string.
     */
    String code();

    /**
     * Get message.
     *
     * @return Message describing conditions.
     */
    String message();

    /**
     * Get detail.
     *
     * @return Unstructured details, might be absent.
     */
    Optional<String> detail();
}
