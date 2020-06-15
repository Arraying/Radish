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
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a pipeline.
 */
public class RedisPipeline implements RedisCallable<RedisPipeline> {
    private final RedisClient client;
    private int calls;

    /**
     * Creates a new pipeline from the client.
     * @param client The client.
     */
    RedisPipeline(RedisClient client) {
        this.client = client;
    }

    /**
     * See {@link RedisClient#call(Object...)} for more information.
     * This will only return a result when {@link #read()} is called.
     * @param command The command, with each argument a separate entry.
     * @return The current instance, for chaining.
     */
    public RedisPipeline call(Object... command) {
        try {
            client.out.writeRESPArray(command);
            client.out.flush();
            calls++;
        } catch (IOException exception) {
            throw new RedisException(exception);
        }
        return this;
    }

    /**
     * Gets the responses of the pipeline.
     * For more information on responses, see {@link RedisClient#read()}.
     * @return A list of responses.
     */
    public List<RedisResponse> read() {
        List<RedisResponse> responses = new ArrayList<>();
        while (--calls >= 0) {
            RedisResponse redisResponse = new RedisResponse();
            client.in.parse(redisResponse);
            responses.add(redisResponse);
        }
        return responses;
    }
}
