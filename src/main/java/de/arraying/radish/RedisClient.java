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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * The main client used to interact with Redis.
 * This is essentially a TCP socket connection.
 * This connection is not threadsafe, all synchronization must be done
 * It is nonfinal and can be overridden, especially the close method can be used to return the connection to a pool.
 */
public class RedisClient implements RedisCallable<RedisResponse>, Closeable {

    /**
     * The default buffer size. Currently set to
     */
    public static final int DEFAULT_BUFFER_SIZE = 1 << 16;

    /**
     * Used as a delimiter.
     */
    public static final byte[] CR_LF = new byte[] {'\r', '\n'};

    private final InetSocketAddress inetSocketAddress;
    private final Socket socket;
    final RedisOut out;
    final RedisIn in;

    /**
     * Constructs a new client.
     * @param inetSocketAddress The socket address of the Redis server, must not be null.
     * @param bufferSize The buffer size, a reasonable size would be 2^16.
     * @throws NullPointerException If the socket address is null.
     * @throws IllegalArgumentException If the buffer size is < 0.
     * @throws RedisException If the socket connection could not be established.
     */
    public RedisClient(InetSocketAddress inetSocketAddress, int bufferSize) {
        if (inetSocketAddress == null) {
            throw new NullPointerException("socket address cannot be null");
        }
        if (bufferSize < 0) {
            throw new IllegalArgumentException("buffer size cannot be < 0");
        }
        this.inetSocketAddress = inetSocketAddress;
        try {
            this.socket = new Socket(inetSocketAddress.getAddress(), inetSocketAddress.getPort());
            this.out = new RedisOut(new BufferedOutputStream(socket.getOutputStream(), bufferSize));
            this.in = new RedisIn(new BufferedInputStream(socket.getInputStream(), bufferSize));
        } catch (IOException exception) {
            throw new RedisException(exception);
        }
    }

    /**
     * Executes a Redis command by sending the packet to the server.
     * The outcome and response of this will differ depending on the executed command.
     * If an error occurs, then the error will be incorporated into the response, rather than raising an exception.
     * @param command The command, with each argument a separate entry.
     * @return The response, will never be null.
     */
    public RedisResponse call(Object... command) {
        try {
            out.writeRESPArray(command);
            out.flush();
            return read();
        } catch (IOException exception) {
            RedisResponse redisResponse = new RedisResponse();
            redisResponse.exception = new RedisException(exception);
            return redisResponse;
        }
    }

    /**
     * Reads a response from the Redis server.
     * @return The response, will never be null.
     */
    public RedisResponse read() {
        RedisResponse redisResponse = new RedisResponse();
        in.parse(redisResponse);
        return redisResponse;
    }

    /**
     * Creates a new pipeline in order to pipeline the commands.
     * @return A non-null pipeline.
     */
    public RedisPipeline pipeline() {
        return new RedisPipeline(this);
    }

    /**
     * Closes the socket.
     * @throws IOException If there was an exception closing the socket.
     */
    @Override
    public void close() throws IOException {
        socket.close();
    }
}
