/*
 * Copyright (c) FOREVER, Jefferson Buot. All rights reserved.
 * Build | Imagine | Think | Explore -> By: Jeff 
 */
package artemis.chat_module;

import artemis.sched_board_module.ChatMessage;
import com.jeff.graphics.Canvas;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JScrollPane;

/**
 *
 * @author Jefferson
 */
public class ChatMessageContainer extends Canvas {

    List<ChatMessage> chatMessages;
    JScrollPane parent;
    int currentY;
    ChatMessage cm;
    boolean showInfo;

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public ChatMessageContainer(JScrollPane scroll) {
        chatMessages = new ArrayList<>();
        this.parent = scroll;
        parent.getVerticalScrollBar().setUnitIncrement(16);
        parent.setViewportView(this);
    }

    public void clearChat() {
        chatMessages.clear();
        repaint();
    }

    public void appendMessage(ChatMessage cm) {
        chatMessages.add(cm);
        refreshScroll();
        ref();
    }

    public void refreshScroll() {
        setPreferredSize(new Dimension(0, currentY));
        parent.setViewportView(this);
        parent.getVerticalScrollBar().setValue(currentY);
        parent.validate();
        parent.repaint();
    }

    @Override
    public void canvasMouseClicked(MouseEvent evt) {
        if (evt.getClickCount() == 2) {
            chatMessages.stream().filter(chat -> chat.bounds(evt.getX(), evt.getY()) & chat.isIsSender()).forEach(c -> {
                cm = c;
            });
        } else {
        }
    }

    private void ref() {
        java.awt.EventQueue.invokeLater(() -> {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
            }
            refreshScroll();
        });
    }

    @Override
    public void draw(Graphics2D g2d) {
        currentY = 0;
        chatMessages.stream().forEach((chatMessage) -> {
            currentY = chatMessage.draw(g2d, getWidth(), currentY);
        });
    }

}
