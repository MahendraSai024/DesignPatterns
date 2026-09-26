package com.designpatterns.strategy;

import java.util.Arrays;

public class Main {

    public static void main(String[] args) {
        System.out.println("=== Strategy Pattern — Sorting Algorithms ===\n");

        DataSorter sorter = new DataSorter(new BubbleSortStrategy());

        System.out.println("[Bubble Sort]");
        int[] data1 = {42, 7, 3, 12, 1, 9, 5};
        sorter.sort(data1);
        System.out.println("Sorted: " + Arrays.toString(data1));

        System.out.println("\n[Quick Sort]");
        sorter.setStrategy(new QuickSortStrategy());
        int[] data2 = {30, 10, 20, 4, 8, 2, 6};
        sorter.sort(data2);
        System.out.println("Sorted: " + Arrays.toString(data2));

        System.out.println("\n[Merge Sort]");
        sorter.setStrategy(new MergeSortStrategy());
        int[] data3 = {5, 3, 1, 7, 4, 6, 2};
        sorter.sort(data3);
        System.out.println("Sorted: " + Arrays.toString(data3));
    }
}
