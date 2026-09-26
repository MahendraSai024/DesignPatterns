package com.designpatterns.chainofresponsibility;

public abstract class SupportHandler {

    protected SupportHandler next;

    public SupportHandler setNext(SupportHandler next) {
        this.next = next;
        return next;
    }

    public abstract void handle(SupportRequest request);

    protected void passToNext(SupportRequest request) {
        if (next != null) {
            next.handle(request);
        } else {
            System.out.printf("[CHAIN  ] No handler could process: \"%s\" (level: %s)%n",
                    request.getDescription(), request.getLevel());
        }
    }
}
