package com.soccerstars.server.protocol;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Base message class for all protocol messages.
 * All messages are serialized to JSON for transmission.
 */
public class Message {
    private static final Gson gson = new GsonBuilder().create();

    private MessageType type;

    public Message() {}

    public Message(MessageType type) {
        this.type = type;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String toJson() {
        return gson.toJson(this);
    }

    public static MessageType parseType(String json) {
        JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
        return MessageType.valueOf(obj.get("type").getAsString());
    }

    public static <T extends Message> T fromJson(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }

    public static Gson getGson() {
        return gson;
    }
}
