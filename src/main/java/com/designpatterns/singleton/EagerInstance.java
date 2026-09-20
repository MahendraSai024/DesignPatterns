package com.designpatterns.singleton;

public class EagerInstance{
    private static EagerInstance instance;
    private EagerInstance(){}

    private static class Holder{
        private static final EagerInstance instance = new EagerInstance();
    }
    public static EagerInstance getInstance(){
        return Holder.instance;
    }
}
