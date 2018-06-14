# LavaClient-JDA
[![](https://jitpack.io/v/SamOphis/LavaClient-JDA.svg)](https://jitpack.io/#SamOphis/LavaClient-JDA)
[![Build Status](https://travis-ci.org/SamOphis/LavaClient-JDA.svg?branch=master)](https://travis-ci.org/SamOphis/LavaClient-JDA)

The official [LavaClient](https://github.com/SamOphis/LavaClient) integration for [JDA](https://github.com/DV8FromTheWorld/JDA) with full documentation [that can be found here](https://samophis.github.io/LavaClient-JDA), directly in the source or [attached to the latest release.](https://github.com/SamOphis/LavaClient-JDA/releases/latest)

# Using LavaClient-JDA

To use this project, simply create a `LavaClient` instance either traditionally via LavaClient or through the `LavaClientManager` class provided in this project. An example of how to do both is below, however for beginners just starting out the `LavaClientManager`-based approach is the easiest.

Note: In both examples the created client is cached. See the note below for the answer as to why this is done.

```java
// Simple, concise method to quickly create clients.
LavaClient client = LavaClientManager.buildLavaClient(user_id, shard_count);
LavaClientManager.initShard(client, jda_builder_instance); // Initializes the JDABuilder instance so the shard can intercept events.
```
```java
// Allows users to take full advantage of all the LavaClientBuilder methods.
LavaClient client = new LavaClientBuilder()
        .setUserId(user_id)
        .setShardCount(shard_count)
        .build();
LavaClientManager.addLavaClient(client);
LavaClientManager.initShard(client, jda_builder_instance); // Initializes the JDABuilder instance so the shard can intercept events.
```

Clients can be cached (as they are in the above examples) which pretty much just makes it easier to keep track of the clients. A shutdown hook and a manual `shutdown` method exist in the `LavaClientManager` class which just iterates over the values in the map and shuts down each client individually.

It's also ***very important*** to initialize the shard before you attempt to use it, so that LavaClient-JDA can intercept the necessary events to create `LavaPlayer` instances and connect them automatically. If your bot runs multiple shards, you should only need to add the listener to one `JDABuilder` instance. You can then use its `useSharding` method to build new shards with different information, which you can then initialize with the `LavaClientManager`.

# Contributions

There shouldn't be much of a need for any big contributions with the current state of this project, however if you see something you wish to improve or add, open a pull request and wait for it to be merged. All contributions should match my coding style and be documented properly (if any features are changed or added), otherwise they'll be rejected.