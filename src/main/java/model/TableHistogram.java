package model;

import java.util.Map;
import java.util.HashMap;

public class TableHistogram {
    private Map<String, Histogram> histograms;

    public TableHistogram() {
        this.histograms = new HashMap<>();
    }

    public void put(String columnName, Histogram histogram) {
        histograms.put(columnName, histogram);
    }

    public double getPropobility(String columnName, int key) {
        if (!histograms.containsKey(key)) {
            throw new IllegalArgumentException();
        }
        return histograms.get(columnName).getProbability(key);
    }

    public Histogram getHistogram (String columnName) {
        return histograms.get(columnName);
    }
}