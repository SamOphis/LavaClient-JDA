/*
   Copyright 2018 Samuel Pritchard

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package samophis.lavalink.client.jda;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.VoiceState;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.handle.SocketHandler;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import samophis.lavalink.client.entities.LavaClient;
import samophis.lavalink.client.util.Asserter;

import javax.annotation.Nonnull;

/**
 * A SocketHandler which intercepts VOICE_SERVER_UPDATE Events and creates a new LavaPlayer instance which is automatically connected.
 *
 * @since 0.1
 * @author SamOphis
 */

public class VoiceServerInterceptor extends SocketHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(VoiceServerInterceptor.class);
    private final LavaClient client;

    /**
     * Creates a new VoiceServerInterceptor with a provided LavaClient instance and Shard.
     * @param client A <b>not-null</b> LavaClient instance.
     * @param api A <b>not-null</b> JDAImpl instance.
     * @throws NullPointerException If any of the provided parameters were {@code null}.
     */
    public VoiceServerInterceptor(@Nonnull LavaClient client, @Nonnull JDAImpl api) {
        super(Asserter.requireNotNull(api));
        this.client = Asserter.requireNotNull(client);
    }

    /**
     * Called internally by JDA whenever a VOICE_SERVER_UPDATE Event is received on the provided shard.
     * @param content The raw JSON of the event.
     * @return A <b>possibly-null</b> Guild ID.
     * @throws IllegalStateException If the Guild associated with the ID is {@code null} or if the voice state of the current member is {@code null}.
     */
    @Override
    protected Long handleInternally(JSONObject content) {
        LOGGER.debug(content.toString());
        long id = content.getLong("guild_id");
        if (api.getGuildLock().isLocked(id))
            return id;
        Guild guild = api.getGuildById(id);
        if (guild == null) {
            LOGGER.error("Failure to Connect: Guild == null");
            throw new IllegalStateException("guild == null");
        }
        VoiceState state = guild.getSelfMember().getVoiceState();
        if (state == null) {
            LOGGER.error("Failure to Connect: Voice State == null");
            throw new IllegalStateException("voice state == null");
        }
        client.newPlayer(id).connect(state.getSessionId(), content.getString("token"), content.getString("endpoint"));
        return null;
    }
}
