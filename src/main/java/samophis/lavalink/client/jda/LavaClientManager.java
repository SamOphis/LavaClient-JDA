package samophis.lavalink.client.jda;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.dv8tion.jda.core.JDABuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import samophis.lavalink.client.entities.LavaClient;
import samophis.lavalink.client.entities.builders.LavaClientBuilder;
import samophis.lavalink.client.util.Asserter;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents the manager used to initialize shards, easily build LavaClient instances and cache/remove them.
 * <br><p>Note: It's <b>VERY</b> important to initialize a shard before it is built so that LavaClient-JDA can intercept the READY Event.</p>
 *
 * @since 0.1
 * @author SamOphis
 */

@SuppressWarnings("unused")
public class LavaClientManager {
    private LavaClientManager() {}
    private static final Long2ObjectMap<LavaClient> CLIENTS = new Long2ObjectOpenHashMap<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(LavaClientManager.class);
    private static boolean isShutdown;

    /**
     * Used to create and cache a very simple LavaClient instance.
     * @param user_id The <b>positive</b> Bot ID.
     * @param shard_count The <b>positive</b> Shard Count (starting from 1).
     * @return A <b>not-null</b>, cached and very simple LavaClient instance.
     * @throws IllegalArgumentException If the provided User ID or Shard Count were negative.
     */
    @Nonnull
    public static LavaClient buildLavaClient(@Nonnegative long user_id, @Nonnegative int shard_count) {
        LavaClient client = new LavaClientBuilder()
                .setUserId(Asserter.requireNotNegative(user_id))
                .setShardCount(Asserter.requireNotNegative(shard_count))
                .build();
        CLIENTS.put(user_id, client);
        return client;
    }

    /**
     * Used to cache a manually-configured LavaClient instance.
     * @param client A <b>not-null</b> LavaClient instance.
     * @return The provided LavaClient instance, now cached.
     * @throws NullPointerException If the provided LavaClient instance was {@code null}.
     */
    @Nonnull
    public static LavaClient addLavaClient(@Nonnull LavaClient client) {
        CLIENTS.put(Asserter.requireNotNull(client).getUserId(), client);
        return client;
    }

    /**
     * Attempts to remove the LavaClient instance associated with the provided User ID from the cache.
     * <br><p>If found, this will shut-down the LavaClient instance and return it. If not found, nothing will happen and {@code null} will be returned.</p>
     * @param user_id The <b>positive</b> Bot ID.
     * @return A <b>possibly-null</b> LavaClient instance.
     * @throws IllegalArgumentException If the provided User ID was negative.
     */
    @Nullable
    public static LavaClient removeLavaClient(@Nonnegative long user_id) {
        LavaClient client = CLIENTS.remove(Asserter.requireNotNegative(user_id));
        if (client != null)
            client.shutdown();
        return client;
    }

    /**
     * Adds a {@link ClientInitializer ClientInitializer} to a JDABuilder so that it can intercept the READY, VOICE_STATE_UPDATE and VOICE_SERVER_UPDATE Events.
     * @param client A <b>not-null</b> LavaClient instance.
     * @param builder A <b>not-null</b> JDABuilder instance.
     * @return The provided and updated JDABuilder instance.
     * @throws NullPointerException If any of the provided parameters were {@code null}.
     */
    @Nonnull
    public static JDABuilder initShard(@Nonnull LavaClient client, @Nonnull JDABuilder builder) {
        Asserter.requireNotNull(builder).addEventListener(new ClientInitializer(Asserter.requireNotNull(client)));
        return builder;
    }

    /**
     * Attempts to shut-down and remove every single LavaClient instance from the cache.
     * @throws IllegalStateException If the manager has already been shut-down.
     */
    public static void shutdown() {
        if (isShutdown) {
            LOGGER.warn("Attempt to shutdown LavaClientManager more than once!");
            throw new IllegalStateException("manager already shutdown!");
        }
        isShutdown = true;
        CLIENTS.values().forEach(client -> {
            client.shutdown();
            CLIENTS.remove(client.getUserId());
        });
    }
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!isShutdown) {
                CLIENTS.values().forEach(client -> {
                    client.shutdown();
                    CLIENTS.remove(client.getUserId());
                });
            }
        }));
    }
}
