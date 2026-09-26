package com.designpatterns.strategy;

public class DataSorter {

    private SortStrategy strategy;

    public DataSorter(SortStrategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategy(SortStrategy strategy) {
        this.strategy = strategy;
    }

    public void sort(int[] data) {
        System.out.println("Sorting with: " + strategy.getClass().getSimpleName());
        strategy.sort(data);
    }
}
