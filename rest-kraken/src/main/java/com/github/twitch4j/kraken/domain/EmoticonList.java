package com.github.twitch4j.kraken.domain;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.util.List;

@Data
@Setter(AccessLevel.PRIVATE)
public class EmoticonList {
    private List<Emoticon> emoticons;
}
