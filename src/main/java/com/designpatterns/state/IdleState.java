package com.designpatterns.state;

public class IdleState implements State {

    private final VendingMachine machine;

    public IdleState(VendingMachine machine) { this.machine = machine; }

    @Override
    public void insertCoin() {
        System.out.println("[IDLE   ] Coin inserted");
        machine.setState(machine.getHasCoinState());
    }

    @Override public void selectProduct() { System.out.println("[IDLE   ] Insert a coin first"); }
    @Override public void dispense()      { System.out.println("[IDLE   ] No coin inserted"); }
    @Override public void refund()        { System.out.println("[IDLE   ] No coin to refund"); }
}
