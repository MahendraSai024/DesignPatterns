package com.designpatterns.chainofresponsibility;

public class SupportRequest {

    private final String description;
    private final SupportLevel level;

    public SupportRequest(String description, SupportLevel level) {
        this.description = description;
        this.level = level;
    }

    public String getDescription() { return description; }
    public SupportLevel getLevel() { return level; }
}
