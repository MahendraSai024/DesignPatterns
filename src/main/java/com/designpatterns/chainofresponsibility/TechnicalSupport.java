package com.designpatterns.chainofresponsibility;

public class TechnicalSupport extends SupportHandler {

    @Override
    public void handle(SupportRequest request) {
        if (request.getLevel() == SupportLevel.MEDIUM) {
            System.out.printf("[TECH   ] Handling MEDIUM request: \"%s\"%n", request.getDescription());
        } else {
            System.out.printf("[TECH   ] Cannot handle %s — escalating%n", request.getLevel());
            passToNext(request);
        }
    }
}
