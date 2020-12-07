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
package com.artipie.docker.asto;

import com.artipie.asto.Key;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

/**
 * Direct children keys for root from collection of keys.
 *
 * @since 0.9
 */
class Children {

    /**
     * Root key.
     */
    private final Key root;

    /**
     * List of keys inside root.
     */
    private final Collection<Key> keys;

    /**
     * Ctor.
     *
     * @param root Root key.
     * @param keys List of keys inside root.
     */
    Children(final Key root, final Collection<Key> keys) {
        this.root = root;
        this.keys = keys;
    }

    /**
     * Extract unique child names in lexicographical order.
     *
     * @return Ordered child names.
     */
    public Set<String> names() {
        final Set<String> set = new TreeSet<>();
        for (final Key key : this.keys) {
            set.add(this.child(key));
        }
        return set;
    }

    /**
     * Extract direct root child node from key.
     *
     * @param key Key.
     * @return Direct child name.
     */
    private String child(final Key key) {
        Key child = key;
        while (true) {
            final Optional<Key> parent = child.parent();
            if (!parent.isPresent()) {
                throw new IllegalStateException(
                    String.format("Key %s does not belong to root %s", key, this.root)
                );
            }
            if (parent.get().string().equals(this.root.string())) {
                break;
            }
            child = parent.get();
        }
        return child.string().substring(this.root.string().length() + 1);
    }
}
