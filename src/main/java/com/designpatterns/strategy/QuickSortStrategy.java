package com.designpatterns.strategy;

import java.util.Arrays;

public class QuickSortStrategy implements SortStrategy {

    @Override
    public void sort(int[] data) {
        Arrays.sort(data);
    }
}
