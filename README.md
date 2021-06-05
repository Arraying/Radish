# Radish
Simple and lightweight Java Redis bindings, inspired by [drm/java-redis-client](https://github.com/drm/java-redis-client).

### Overview
Radish is a lightweight Redis (or other RESP-compatible services like KeyDB) client.
It aims to address two flaws with many existing Java bindings:
1. **Bloat**. Many libraries are fairly complex and therefore bloated.
Radish has no dependencies and minimal abstraction features, allowing the user to have full control.
This has the advantage that users can fine tune what they want to handle themselves, thus removing unnecessary functionality that would be present in more feature packed libraries. KISS.
2. **Error handling**. Redis is supposed to be usable as a cache, and working with it should not feel like working with a strict database.
Failures happen from time to time, but excessive exception handling makes the code verbose.
Radish only throws an exception when the connection fails while establishing.
Each operation returns a custom result object, which wraps any exceptions that occur.
This is similar to many Go bindings.

### Examples
##### Basic Usage
```java
InetAddress inetAddress = InetAddress.getByName("example.com");
RedisClient redisClient = new RedisClient(new InetSocketAddress(inetAddress, 6379), 1 << 16);
RedisResponse response = redisClient.call("set", "hello", "world");
System.out.println(response.valueString()); // prints "OK"
response = redisClient.call("get", "hello");
System.out.println(response.valueString()); // prints "world"
```

##### Pipelining
```java
RedisPipeline pipeline = redisClient.pipeline();
List<RedisResponse> results = pipeline.call("multi")
    .call("set", "hello", "world2")
    .call("del", "hello")
    .call("exec")
    .read();
```

##### Publish/Subscribe
```java
RedisResponse redisResponse = redisClient.call("subscribe", "test");
while (true) { // Not ideal but will illustrate the concept
    // When someone does: PUBLISH test hello
    RedisResponse message = redisClient.read();
    System.out.println(message.valueList(object -> new String((byte[]) object)));
    // Prints [message, test, hello]
}
```

### Data Types

Non-failure data types will be one of the following:
- Integer (signed, 64 bit), which is represented as a `long`.
- Simple string, which is represented as a `byte[]`.
- Bulk string, which is represented as a `byte[]`.

In addition, arrays are `Object[]`s, where each object is an instance of the aforementioned types, or of an array.

`RedisResponse` provides some basic helper methods, i.e. converting the `byte[]`s to a `String`, however, everything is very basic and may require abstraction.

### Concurrency
A `RedisClient` is not thread safe, and needs to be synchronized externally.
For concurrent usage, use a pool of `RedisClient`s.
There are many existing libraries that provide pooling functionality, such as in Apache Commons.
The `RedisClient` class is not final and implements Closeable. 
A custom superclass can easily override (though it should call `super.close()` too) the method in order to return the connection to a pool.

### Installation

```xml
<repositories>
    <repository>
    	<id>arraying-releases</id>
    	<url>http://repo.arraying.de/releases/</url>
    </repository>
</repositories>
<dependencies>
    <dependency>
        <groupId>de.arraying</groupId>
        <artifactId>radish</artifactId>
        <!-- Replace this with the latest version if applicable -->
        <version>1.0.0</version>
    </dependency>
</dependencies>
```
