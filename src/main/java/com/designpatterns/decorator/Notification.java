package com.designpatterns.decorator;

public interface Notification {
    void sendMessage(String msg);
}

abstract class NotificationDecorator implements Notification{
    protected Notification notification; // Wrapping the Notification
    NotificationDecorator(Notification notification) {
        this.notification = notification;
    }
}

class RetryDecorator extends NotificationDecorator {
    public RetryDecorator(Notification notification) {
        // Pass it to the parent - as parent is already taking care of it
        super(notification);
    }
    @Override
    public void sendMessage(String msg) {
        System.out.println("Retry logic triggered");
        notification.sendMessage(msg);
    }
}

class FormattingDecorator extends NotificationDecorator{
    public FormattingDecorator(Notification notification) {
        super(notification);
    }

    @Override
    public void sendMessage(String msg) {
        System.out.println("Formatting the msg");
        notification.sendMessage(msg);
    }
}