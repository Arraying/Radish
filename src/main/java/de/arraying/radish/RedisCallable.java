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
 * A simple blueprint for an object that can call a Redis command.
 * This is used to ensure that the main client and pipeline are consistent.
 * @param <T> The return type.
 */
public interface RedisCallable<T> {

    /**
     * Calls a command.
     * @param command The command, with each argument a separate entry.
     * @return Depends on the implementation.
     */
    T call(Object... command);
}
