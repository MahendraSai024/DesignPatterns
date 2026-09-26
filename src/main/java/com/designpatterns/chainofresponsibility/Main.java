package com.designpatterns.chainofresponsibility;

public class Main {

    public static void main(String[] args) {
        System.out.println("=== Chain of Responsibility — Support Ticket System ===\n");

        FrontlineSupport   frontline  = new FrontlineSupport();
        TechnicalSupport   technical  = new TechnicalSupport();
        SeniorEngineer     senior     = new SeniorEngineer();
        ManagementEscalation management = new ManagementEscalation();

        frontline.setNext(technical).setNext(senior).setNext(management);

        System.out.println("[Scenario 1 — LOW severity]");
        frontline.handle(new SupportRequest("Password reset", SupportLevel.LOW));

        System.out.println("\n[Scenario 2 — MEDIUM severity]");
        frontline.handle(new SupportRequest("Application crash on login", SupportLevel.MEDIUM));

        System.out.println("\n[Scenario 3 — HIGH severity]");
        frontline.handle(new SupportRequest("Data corruption in prod database", SupportLevel.HIGH));

        System.out.println("\n[Scenario 4 — CRITICAL severity]");
        frontline.handle(new SupportRequest("Complete system outage", SupportLevel.CRITICAL));

        System.out.println("\n[Scenario 5 — Chain reuse (another MEDIUM)]");
        frontline.handle(new SupportRequest("API returning 500 errors", SupportLevel.MEDIUM));

        System.out.println("\n[Scenario 6 — Unhandled (partial chain, no CRITICAL handler)]");
        FrontlineSupport partialFrontline = new FrontlineSupport();
        TechnicalSupport partialTechnical = new TechnicalSupport();
        SeniorEngineer   partialSenior    = new SeniorEngineer();
        partialFrontline.setNext(partialTechnical).setNext(partialSenior);
        partialFrontline.handle(new SupportRequest("Alien invasion", SupportLevel.CRITICAL));
    }
}
