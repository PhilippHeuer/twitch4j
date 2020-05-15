package com.github.twitch4j.pubsub.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommerceData {
    private String userName;
    private String displayName;
    private String channelName;
    private String userId;
    private String channelId;
    private String time;
    private String itemImageUrl;
    private String itemDescription;
    private Boolean supportsChannel;
    private CommerceMessage purchaseMessage;
}
