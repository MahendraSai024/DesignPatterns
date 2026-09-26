package com.designpatterns.chainofresponsibility;

public class ManagementEscalation extends SupportHandler {

    @Override
    public void handle(SupportRequest request) {
        if (request.getLevel() == SupportLevel.CRITICAL) {
            System.out.printf("[MGMT   ] Handling CRITICAL request: \"%s\"%n", request.getDescription());
        } else {
            System.out.printf("[MGMT   ] Cannot handle %s — escalating%n", request.getLevel());
            passToNext(request);
        }
    }
}
