package com.designpatterns.state;

public class OutOfStockState implements State {

    private final VendingMachine machine;

    public OutOfStockState(VendingMachine machine) { this.machine = machine; }

    @Override
    public void insertCoin() {
        System.out.println("[EMPTY  ] Machine is out of stock, refunding coin");
    }

    @Override public void selectProduct() { System.out.println("[EMPTY  ] Machine is out of stock"); }
    @Override public void dispense()      { System.out.println("[EMPTY  ] No items to dispense"); }
    @Override public void refund()        { System.out.println("[EMPTY  ] No coin inserted"); }
}
