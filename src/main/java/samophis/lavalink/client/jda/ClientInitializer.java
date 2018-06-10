package samophis.lavalink.client.jda;

import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.handle.SocketHandler;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import samophis.lavalink.client.entities.LavaClient;
import samophis.lavalink.client.util.Asserter;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * An event listener which listens for the initial Ready Event from JDA, subsequently adding interceptor handlers to the shard.
 * <br><p>This listener intercepts events from JDA and passes them to LavaClient in order to connect to Lavalink Nodes.</p>
 *
 * @since 0.1
 * @author SamOphis
 */

public class ClientInitializer extends ListenerAdapter {
    private final LavaClient client;

    /**
     * Used internally to create the initializer with a <b>not-null</b> LavaClient instance attached.
     * @param client The <b>not-null</b> LavaClient instance.
     * @throws NullPointerException If the provided client was {@code null}.
     */
    @SuppressWarnings("WeakerAccess")
    public ClientInitializer(@Nonnull LavaClient client) {
        this.client = Asserter.requireNotNull(client);
    }

    /**
     * Called internally by JDA when the shard in question becomes "Ready".
     * @param event The ReadyEvent used to grab the shard and add handlers to it.
     */
    @Override
    public void onReady(ReadyEvent event) {
        JDAImpl impl = (JDAImpl) event.getJDA();
        Map<String, SocketHandler> handlers = impl.getClient().getHandlers();
        handlers.put("VOICE_SERVER_UPDATE", new VoiceServerInterceptor(client, impl));
        handlers.put("VOICE_STATE_UPDATE", new VoiceStateInterceptor(client, impl));
    }
}
