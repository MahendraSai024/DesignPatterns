package com.designpatterns.state;

public class DispensingState implements State {

    private final VendingMachine machine;

    public DispensingState(VendingMachine machine) { this.machine = machine; }

    @Override public void insertCoin()    { System.out.println("[DISPENSE] Please wait, dispensing in progress"); }
    @Override public void selectProduct() { System.out.println("[DISPENSE] Already dispensing"); }
    @Override public void refund()        { System.out.println("[DISPENSE] Cannot refund while dispensing"); }

    @Override
    public void dispense() {
        machine.decrementItems();
        System.out.println("[DISPENSE] Dispensing your item!");
        if (machine.getItemCount() > 0) {
            machine.setState(machine.getIdleState());
        } else {
            machine.setState(machine.getOutOfStockState());
        }
        machine.printStatus();
    }
}
