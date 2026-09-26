package com.designpatterns.state;

// State — defines the operations every concrete state must handle
public interface State {
    void insertCoin();
    void selectProduct();
    void dispense();
    void refund();
}
