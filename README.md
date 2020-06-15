# Radish
Simple and lightweight Java Redis bindings.

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

### Installation

Currently, Radish is not in Maven Central. Therefore, there are two ways of adding it to the repository.
Future releases will be on a proper Maven repository.

##### Through JitPack
This is recommended.
```xml
<repositories>
    <repository>
    	<id>jitpack.io</id>
    	<url>https://jitpack.io</url>
	</repository>
</repositories>
<dependencies>
    <dependency>
        <groupId>com.github.Arraying</groupId>
        <artifactId>Radish</artifactId>
        <!-- You can also replace this with a commit hash -->
        <version>master-SNAPSHOT</version>
    </dependency>
</dependencies>
```

##### Installing to the local repository
First, install it to the local repository, then add as a dependency.
This is not recommended for team projects.
```xml
git clone https://github.com/Arraying/Radish.git
cd Radish
mvn clean install
```
```xml
<dependencies>
    <dependency>
        <groupId>de.arraying</groupId>
        <artifactId>radish</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </dependency>
</dependencies>
```