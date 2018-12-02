package model;

import java.util.Map;
import java.util.HashMap;

public class Histogram {
    Map <Integer, Integer> histogram;
    String columnName;
    long count;

    public Histogram(String columnName) {
        this.histogram = new HashMap<>();
        this.columnName = columnName;
        count = 0;
    }

    public String getColumnName() {
        return this.columnName;
    }

    public void add(int key) {
        if (!histogram.containsKey(key)) {
            histogram.put(key, 1);
        }
        else {
            histogram.replace(key, histogram.get(key) + 1);
        }
        count ++;
    }

    public double getProbability(int key) {
        if (!histogram.containsKey(key)) {
            return 0;
        }
        return (double)histogram.get(key) / (double)count;
    }
} 