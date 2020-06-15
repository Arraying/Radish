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

import java.io.IOException;
import java.io.OutputStream;

import static de.arraying.radish.RedisClient.CR_LF;

/**
 * The request writer to Redis.
 */
public class RedisOut {
    private final OutputStream outputStream;

    /**
     * Creates a new writer from the output stream.
     * @param outputStream The socket output stream.
     */
    public RedisOut(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    /**
     * Writes a RESP integer (64 bit).
     * @param integer The integer.
     * @throws IOException If there was an error writing.
     */
    public void writeRESPInteger64(long integer) throws IOException {
        outputStream.write(':');
        outputStream.write(String.valueOf(integer).getBytes());
        outputStream.write(CR_LF);
    }

    /**
     * Writes a RESP string (bulk).
     * @param string The string as a byte[].
     * @throws IOException If there was an error writing.
     */
    public void writeRESPBulkString(byte[] string) throws IOException {
        outputStream.write('$');
        outputStream.write(String.valueOf(string.length).getBytes());
        outputStream.write(CR_LF);
        outputStream.write(string);
        outputStream.write(CR_LF);
    }

    /**
     * Writes a RESP array.
     * This will write individually for each component.
     * @param values The array.
     * @throws IOException If there was an error writing.
     */
    public void writeRESPArray(Object[] values) throws IOException {
        outputStream.write('*');
        outputStream.write(String.valueOf(values.length).getBytes());
        outputStream.write(CR_LF);
        for (Object value : values) {
            if (value == null) {
                // Handle null literally.
                writeRESPBulkString("null".getBytes());
            } else if (value instanceof Object[]) {
                writeRESPArray((Object[]) value);
            } else if (value instanceof String) {
                writeRESPBulkString(((String) value).getBytes());
            } else if (value instanceof byte[]) {
                writeRESPBulkString((byte[]) value);
            } else if (value instanceof Number) {
                writeRESPInteger64(Long.parseLong(value.toString()));
            }
        }
    }

    /**
     * Flushes the writer.
     * @throws IOException If there was an error flushing.
     */
    public void flush() throws IOException {
        outputStream.flush();
    }
}
