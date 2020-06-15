/* Copyright 2020 Arraying
 *
 * This file is part of Radish.
 *
 * Radish is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Radish is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Radish. If not, see http://www.gnu.org/licenses/.
 */

package de.arraying.radish;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Represents a response for a Redis command and/or PUB/SUB.
 */
public class RedisResponse {
    Object value;
    RedisException exception;

    /**
     * Whether or not the action was successful.
     * @return True if it was, false otherwise.
     */
    public boolean success() {
        return exception != null;
    }

    /**
     * Gets the error.
     * @return The error, can be null.
     */
    public RedisException error() {
        return exception;
    }

    /**
     * Whether the response is null.
     * @return True if it is null, false otherwise.
     */
    public boolean nil() {
        return value == null;
    }

    /**
     * Gets the object as a raw object that can then be processed further.
     * @return The object, can be null.
     * @throws RedisException If the response was an error and no object exist.
     */
    public Object valueRaw() {
        if (exception != null) {
            throw exception;
        }
        return value;
    }

    /**
     * @return Casts {@link #valueRaw()} to a byte[].
     */
    public byte[] valueBytes() {
        return (byte[]) valueRaw();
    }

    /**
     * @return Casts {@link #valueRaw()} to a long.
     */
    public long valueInteger() {
        return (Long) valueRaw();
    }

    /**
     * @return Casts {@link #valueRaw()} to an Object[].
     */
    public Object[] valueArray() {
        return (Object[]) value;
    }

    /**
     * @return Converts {@link #valueBytes()} to a string.
     */
    public String valueString() {
        return new String(valueBytes());
    }

    /**
     * Converts the value array to a list.
     * @param converter The converter function, may not be null.
     * @param <T> The type of collection.
     * @return A non-null list.
     * @throws NullPointerException If the converter is null.
     */
    public <T> List<T> valueList(Function<Object, T> converter) {
        if (converter == null) {
            throw new NullPointerException("converter is null");
        }
        return Arrays.stream(valueArray())
            .map(converter)
            .collect(Collectors.toList());
    }

    /**
     * Converts the value array to a set.
     * @param converter The converter function, may not be null.
     * @param <T> The type of collection.
     * @return A non-null set.
     * @throws NullPointerException If the converter is null.
     */
    public <T> Set<T> valueSet(Function<Object, T> converter) {
        if (converter == null) {
            throw new NullPointerException("converter is null");
        }
        return Arrays.stream(valueArray())
            .map(converter)
            .collect(Collectors.toSet());
    }
}
