package com.designpatterns.state;

public class Main {

    public static void main(String[] args) {
        System.out.println("=== State Pattern — Vending Machine ===\n");

        VendingMachine machine = new VendingMachine(2);

        System.out.println("[Scenario 1 — Normal purchase]");
        System.out.println("[MACHINE] Items in stock: " + machine.getItemCount());
        machine.insertCoin();
        machine.selectProduct();

        System.out.println("\n[Scenario 2 — Refund]");
        machine.insertCoin();
        machine.refund();

        System.out.println("\n[Scenario 3 — Last item (machine goes out of stock)]");
        machine.insertCoin();
        machine.selectProduct();

        System.out.println("\n[Scenario 4 — Out of stock]");
        machine.insertCoin();
    }
}
