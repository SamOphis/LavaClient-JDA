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
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.handle.VoiceStateUpdateHandler;
import org.json.JSONObject;

import samophis.lavalink.client.entities.LavaClient;
import samophis.lavalink.client.entities.LavaPlayer;
import samophis.lavalink.client.entities.internal.LavaPlayerImpl;
import samophis.lavalink.client.util.Asserter;

import javax.annotation.Nonnull;

/**
 * A SocketHandler which intercepts VOICE_STATE_UPDATE events and initializes LavaClient for the provided Guild.
 *
 * @since 0.1
 * @author SamOphis
 */

public class VoiceStateInterceptor extends VoiceStateUpdateHandler {
    private final LavaClient client;

    /**
     * Creates a new VoiceStateInterceptor with a provided LavaClient and Shard.
     * @param client A <b>not-null</b> LavaClient instance.
     * @param impl A <b>not-null</b> JDAImpl instance.
     * @throws NullPointerException If any of the provided parameters were {@code null}.
     */
    public VoiceStateInterceptor(@Nonnull LavaClient client, @Nonnull JDAImpl impl) {
        super(Asserter.requireNotNull(impl));
        this.client = Asserter.requireNotNull(client);
    }

    /**
     * Called internally by JDA to handle Voice State Update Events.
     * @param content The raw JSON.
     * @return The ID of a Guild or whatever JDA's default implementation returns.
     */
    @Override
    protected Long handleInternally(JSONObject content) {
        Long id = content.has("guild_id") ? content.getLong("guild_id") : null;
        if (id != null && api.getGuildLock().isLocked(id))
            return id;
        if (id == null)
            return super.handleInternally(content);
        /* ---- */
        long user_id = content.getLong("user_id");
        Long channel_id = content.has("channel_id") ? content.getLong("channel_id") : null;
        Guild guild = api.getGuildById(id);
        if (guild == null)
            return super.handleInternally(content);
        Member member = guild.getMemberById(user_id);
        if (member == null)
            return super.handleInternally(content);
        if (!member.equals(guild.getSelfMember()))
            return super.handleInternally(content);
        LavaPlayer player = client.newPlayer(id);
        if (channel_id == null)
            player.destroyPlayer();
        else
            ((LavaPlayerImpl) player).setChannelId(channel_id);
        api.getClient().updateAudioConnection(id, guild.getVoiceChannelById(channel_id == null ? -1 : channel_id));
        return super.handleInternally(content);
    }
}
