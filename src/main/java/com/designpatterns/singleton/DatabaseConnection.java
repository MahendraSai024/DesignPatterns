package com.designpatterns.singleton;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class DatabaseConnection {

    private static DatabaseConnection instance;
    public static int connectionsCount = 0;

    // Prevent Initialization of the class from outside
    private DatabaseConnection() {}

    public static DatabaseConnection getInstance() {
        connectionsCount += 1;
        if (instance == null) {
            instance = new DatabaseConnection();
            System.out.println("New Connection Initialized");
        }
        return instance;
    }
}


