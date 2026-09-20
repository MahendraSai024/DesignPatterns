package com.designpatterns.singleton;
import com.designpatterns.singleton.DatabaseConnection;

public class Main {
    public static void main(String[] args) {
        DatabaseConnection db1 = DatabaseConnection.getInstance();
        DatabaseConnection db2 = DatabaseConnection.getInstance();
    }
}