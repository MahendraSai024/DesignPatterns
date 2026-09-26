package com.designpatterns.chainofresponsibility;

public class FrontlineSupport extends SupportHandler {

    @Override
    public void handle(SupportRequest request) {
        if (request.getLevel() == SupportLevel.LOW) {
            System.out.printf("[FRONT  ] Handling LOW request: \"%s\"%n", request.getDescription());
        } else {
            System.out.printf("[FRONT  ] Cannot handle %s — escalating%n", request.getLevel());
            passToNext(request);
        }
    }
}
