package model;

public class ColumnStat {
    public Integer maxValue;
    public Integer minValue;

    public ColumnStat() {
        maxValue = null;
        minValue = null;
    }

    public ColumnStat(Integer minValue, Integer maxValue) {
        this.minValue = minValue;
        this.maxValue = maxValue;
    }
}
