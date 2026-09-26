package com.designpatterns.chainofresponsibility;

public class SeniorEngineer extends SupportHandler {

    @Override
    public void handle(SupportRequest request) {
        if (request.getLevel() == SupportLevel.HIGH) {
            System.out.printf("[SENIOR ] Handling HIGH request: \"%s\"%n", request.getDescription());
        } else {
            System.out.printf("[SENIOR ] Cannot handle %s — escalating%n", request.getLevel());
            passToNext(request);
        }
    }
}
