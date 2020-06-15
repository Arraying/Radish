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

/**
 * A generic exception for Redis. It comes in two types:
 * 1) A Redis error - the exception is constructed with the error message.
 * 2) An I/O or encoding error - the exception is constructed with the parent exception.
 */
public class RedisException extends RuntimeException {

    /**
     * Creates an exception from a Redis error.
     * @param message The error message.
     */
    public RedisException(String message) {
        super(message);
    }

    /**
     * Creates an exception from another exception.
     * @param exception The exception.
     */
    public RedisException(Exception exception) {
        super(exception);
    }
}
