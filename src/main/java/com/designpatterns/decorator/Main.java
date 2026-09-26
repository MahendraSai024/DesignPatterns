package com.designpatterns.decorator;


public class Main {
    public static void main(String[] args) {
        Notification notification = new SMS(); // Can be taken care using Factory Design Pattern


        Notification notification2 = new RetryDecorator(notification);
        notification2.sendMessage("Msg from NMS");


        Notification notification3 = new FormattingDecorator(new RetryDecorator(new SMS()));
        notification3.sendMessage("It's my Birthday");
    }
}
