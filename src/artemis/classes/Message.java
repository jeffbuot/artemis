/*
 * Copyright (c) FOREVER, Jefferson Buot. All rights reserved.
 * Build | Imagine | Think | Explore -> By: Jeff 
 */
package artemis.classes;

import artemis.chat_module.SerializableImage;
import java.io.Serializable;

/**
 *
 * @author Jeff
 * @author Capslock
 */
public class Message implements Serializable {

    final String sender;
    final int userId;
    final String message;
    final String timeInfo;
    final SerializableImage senderImage;

    public Message(String sender, String message, String timeInfo, SerializableImage senderImage, int userId) {
        this.sender = sender;
        this.message = message;
        this.timeInfo = timeInfo;
        this.senderImage = senderImage;
        this.userId = userId;
    }

    public int getUserId() {
        return userId;
    }

    public String getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

    public String getTimeInfo() {
        return timeInfo;
    }

    public SerializableImage getSenderImage() {
        return senderImage;
    }

}
