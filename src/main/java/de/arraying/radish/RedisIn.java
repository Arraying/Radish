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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * The response reader from Redis.
 */
public class RedisIn {
    private final InputStream inputStream;

    /**
     * Creates a new reader from the input stream.
     * @param inputStream The socket input stream.
     */
    public RedisIn(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    /**
     * Parses the response into a RedisResponse.
     * @param redisResponse The RedisResponse to use.
     */
    public void parse(RedisResponse redisResponse) {
        try {
            Object x = parseRESPValue();
            redisResponse.value =x;
        } catch (IOException exception) {
            redisResponse.exception = new RedisException(exception);
        } catch (RedisException exception) {
            redisResponse.exception = exception;
        }
    }

    /**
     * Parses the response into an object.
     * @return The object, as a response.
     * @throws RedisException Thrown when the response is either of type error, or an error occurs.
     * @throws IOException Thrown when there is an I/O exception reading the response.
     */
    private Object parseRESPValue() throws RedisException, IOException {
        int input = inputStream.read();
        switch (input) {
            case '+': // Simple string.
                return parseRESPSimpleString();
            case '-': // Error (contains simple string).
                throw new RedisException(new String(parseRESPSimpleString()));
            case ':': // Integer.
                return parseRESPInteger64();
            case '$': // Bulk string.
                return parseRESPBulkString();
            case '*': // Array.
                return parseRESPArray();
            default:
                throw new RedisException(new IllegalStateException("unknown type " + (char) input));
        }
    }

    /**
     * Parses a RESP string (simple).
     * @return A string as a byte[].
     * @throws IOException If there is an error reading.
     */
    private byte[] parseRESPSimpleString() throws IOException {
        return scanChunk();
    }

    /**
     * Parses a RESP integer (64 bit).
     * @return A number as a long.
     * @throws IOException If there is an error reading.
     */
    private long parseRESPInteger64() throws IOException {
        return Long.parseLong(new String(parseRESPSimpleString()));
    }

    /**
     * Parses a RESP string (bulk).
     * @return A string as a byte[].
     * @throws IOException If there is an error reading.
     */
    private byte[] parseRESPBulkString() throws IOException {
        int length = (int) parseRESPInteger64(); // 512MB max fits into a 32 bit signed integer.
        if (length == -1) {
            return null; // Used to represent null.
        }
        byte[] data = new byte[length];
        int read = 0;
        while (read < length) {
            read += inputStream.read(data, read, length - read);
        }
        int character = inputStream.read();
        if (character != '\r') {
            throw new IOException("protocol exception; expected \\r but got " + (char) character);
        }
        character = inputStream.read();
        if (character != '\n') {
            throw new IOException("protocol exception; expected \\n but got " + (char) character);
        }
        return data;
    }

    /**
     * Parses a RESP array.
     * This will also parse each element with the {@link #parseRESPValue()} method.
     * @return An array of different object types as an Object[].
     * @throws IOException If there is an error reading.
     */
    private Object[] parseRESPArray() throws IOException {
        int length = (int) parseRESPInteger64();
        if (length == -1) {
            // Edge cases where an array is used to represent null.
            return null;
        }
        Object[] data = new Object[length];
        for (int i = 0; i < length; i++) {
            data[i] = parseRESPValue();
        }
        return data;
    }

    /**
     * Scans a chunk.
     * This will essentially scan until Redis' CR_LF is found, which is used as a delimiter.
     * @return The chunk as a byte[].
     * @throws IOException If there is an error reading.
     */
    private byte[] scanChunk() throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int character;
        while ((character = inputStream.read()) != '\r') {
            buffer.write(character);
        }
        character = inputStream.read();
        // Technically not needed but there for consistency purposes.
        if (character != '\n') {
            throw new IOException("protocol exception; expected \\n but got " + (char) character);
        }
        return buffer.toByteArray();
    }
}
