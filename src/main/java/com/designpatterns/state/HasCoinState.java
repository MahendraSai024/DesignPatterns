package com.designpatterns.state;

public class HasCoinState implements State {

    private final VendingMachine machine;

    public HasCoinState(VendingMachine machine) { this.machine = machine; }

    @Override
    public void insertCoin() {
        System.out.println("[COIN   ] Coin already inserted, refunding extra coin");
    }

    @Override
    public void selectProduct() {
        System.out.println("[COIN   ] Product selected");
        machine.setState(machine.getDispensingState());
        machine.dispense();
    }

    @Override public void dispense() { System.out.println("[COIN   ] Select a product first"); }

    @Override
    public void refund() {
        System.out.println("[COIN   ] Refunding coin");
        machine.setState(machine.getIdleState());
    }
}
