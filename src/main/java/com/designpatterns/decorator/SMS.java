package com.designpatterns.decorator;

public class SMS implements Notification {
    @Override
    public void sendMessage(String msg) {
        System.out.println("SMS Sent to SMS shop");
    }
}
