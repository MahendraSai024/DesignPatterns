package com.designpatterns.state;

public class VendingMachine implements State {

    private final State idleState;
    private final State hasCoinState;
    private final State dispensingState;
    private final State outOfStockState;

    private State currentState;
    private int itemCount;

    public VendingMachine(int itemCount) {
        this.itemCount      = itemCount;
        idleState           = new IdleState(this);
        hasCoinState        = new HasCoinState(this);
        dispensingState     = new DispensingState(this);
        outOfStockState     = new OutOfStockState(this);
        currentState        = (itemCount > 0) ? idleState : outOfStockState;
    }

    public void setState(State state)   { this.currentState = state; }
    public int  getItemCount()          { return itemCount; }
    public void decrementItems()        { itemCount--; }

    public State getIdleState()         { return idleState; }
    public State getHasCoinState()      { return hasCoinState; }
    public State getDispensingState()   { return dispensingState; }
    public State getOutOfStockState()   { return outOfStockState; }

    @Override public void insertCoin()    { currentState.insertCoin(); }
    @Override public void selectProduct() { currentState.selectProduct(); }
    @Override public void dispense()      { currentState.dispense(); }
    @Override public void refund()        { currentState.refund(); }

    public void printStatus() {
        if (itemCount > 0) {
            System.out.println("[MACHINE] Items remaining: " + itemCount);
        } else {
            System.out.println("[MACHINE] Out of stock!");
        }
    }
}
