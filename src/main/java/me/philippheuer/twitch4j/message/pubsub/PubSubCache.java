package me.philippheuer.twitch4j.message.pubsub;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import me.philippheuer.twitch4j.enums.TMIConnectionState;
import me.philippheuer.twitch4j.message.pubsub.topics.PubSubTopics;
import me.philippheuer.util.conversion.RandomizeString;

import java.util.HashSet;
import java.util.Set;

public class PubSubCache {

    private enum MessageType {
        LISTEN,
        UNLISTEN
    }

    private final TwitchPubSub pubSub;
    private final Set<PubSubTopics> listenedTopics = new HashSet<>();
    private final Cache<String, ? extends PubSubTopics> cache = CacheBuilder
            .newBuilder()
            .initialCapacity(50)
            .removalListener(topicCache -> listenedTopics.add((PubSubTopics) topicCache.getValue()))
            .build();

    PubSubCache(TwitchPubSub pubSub) {
        this.pubSub = pubSub;
    }

    public int size() {
        return listenedTopics.size();
    }

    void load() {
        listenedTopics.forEach(topic -> {
            try {
                pubSub.getWs().sendText(parseMessage(MessageType.LISTEN, topic));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });
        pubSub.setConnectionState(TMIConnectionState.CONNECTED);
    }

    private String parseMessage(MessageType msgType, PubSubTopics topic) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        JsonNode root = mapper.createObjectNode();
        JsonNode data = mapper.createObjectNode();
        ArrayNode topics = mapper.createArrayNode();

        topics.add(topic.toString());

        ((ObjectNode) data).put("topics", topics);
        if (topic.getCredential() != null)
            ((ObjectNode) data).put("auth_token", topic.getCredential().getToken());

        ((ObjectNode) root).put("type", msgType.name());
        ((ObjectNode) root).put("nonce", new RandomizeString(64).toString().toUpperCase());
        ((ObjectNode) root).put("data", data);

        System.out.println(root.toString());
        return root.toString();
    }

    @SuppressWarnings("unchecked")
    public <T extends PubSubTopics> void addListener(T... topics) throws PubSubReachedLimitException, JsonProcessingException {
        for (PubSubTopics topic : topics) {
            if (size() < 50) {
                if (!listenedTopics.contains(topic)) {
                    if (this.pubSub.getConnectionState().equals(TMIConnectionState.CONNECTED))
                        this.pubSub.getWs().sendText(parseMessage(MessageType.LISTEN, topic));
                    listenedTopics.add(topic);
                    System.out.println(topic.toString());
                }
            } else throw new PubSubReachedLimitException(topic);
        }
    }

	@SuppressWarnings("unchecked")
    public <T extends PubSubTopics> void removeListener(T... topics) throws JsonProcessingException {
        for (PubSubTopics topic : topics) {
            if (listenedTopics.contains(topic)) {
                if (this.pubSub.getConnectionState().equals(TMIConnectionState.CONNECTED)) this.pubSub.getWs().sendText(parseMessage(MessageType.UNLISTEN, topic));
                listenedTopics.remove(topic);
            }
        }
    }
}
