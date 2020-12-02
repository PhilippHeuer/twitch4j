package com.github.twitch4j;

import com.github.philippheuer.events4j.core.EventManager;
import com.github.twitch4j.chat.ITwitchChat;
import com.github.twitch4j.common.annotation.Unofficial;
import com.github.twitch4j.extensions.TwitchExtensions;
import com.github.twitch4j.graphql.TwitchGraphQL;
import com.github.twitch4j.helix.TwitchHelix;
import com.github.twitch4j.kraken.TwitchKraken;
import com.github.twitch4j.modules.ModuleLoader;
import com.github.twitch4j.pubsub.ITwitchPubSub;
import com.github.twitch4j.tmi.TwitchMessagingInterface;

public interface ITwitchClient extends AutoCloseable {

    /**
     * Get the event manager
     *
     * @return EventManager
     */
    EventManager getEventManager();


    /**
     * Get Extensions
     *
     * @return TwitchExtensions
     */
    TwitchExtensions getExtensions();

    /**
     * Get Helix
     *
     * @return TwitchHelix
     */
    TwitchHelix getHelix();

    /**
     * Get Kraken
     *
     * @return TwitchKraken
     */
    @Deprecated
    TwitchKraken getKraken();

    /**
     * Get MessagingInterface (API)
     *
     * @return TwitchMessagingInterface
     */
    @Unofficial
    TwitchMessagingInterface getMessagingInterface();

    /**
     * Get Chat
     *
     * @return ITwitchChat
     */
    ITwitchChat getChat();

    /**
     * Get PubSub
     *
     * @return ITwitchPubSub
     */
    ITwitchPubSub getPubSub();


    /**
     * Get GraphQL
     *
     * @return TwitchGraphQL
     */
    @Unofficial
    TwitchGraphQL getGraphQL();

    /**
     * Get Module Loader
     *
     * @return ModuleLoader
     */
    ModuleLoader getModuleLoader();

    /**
     * Get TwitchClientHelper
     *
     * @return TwitchClientHelper
     */
    TwitchClientHelper getClientHelper();

    @Override
    default void close() {
        ITwitchChat chat = getChat();
        if (chat != null)
            chat.close();

        ITwitchPubSub pubsub = getPubSub();
        if (pubsub != null)
            pubsub.close();

        TwitchClientHelper clientHelper = getClientHelper();
        if (clientHelper != null)
            clientHelper.close();
    }

}
