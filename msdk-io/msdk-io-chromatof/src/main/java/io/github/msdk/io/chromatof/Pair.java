/*
 * Copyright (C) 2015 nilshoffmann.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package io.github.msdk.io.chromatof;

/**
 * A pair of two generic objects.
 * 
 * @param <F>
 *            the type of the first element in the pair.
 * @param <S>
 *            the type of the second element in the pair.
 */
public class Pair<F, S> extends
        java.util.AbstractMap.SimpleImmutableEntry<F, S> {

    public Pair(F f, S s) {
        super(f, s);
    }

    public F getFirst() {
        return getKey();
    }

    public S getSecond() {
        return getValue();
    }

    @Override
    public String toString() {
        return "[" + getKey() + "," + getValue() + "]";
    }
}
