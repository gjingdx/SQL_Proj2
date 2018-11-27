package model;

/**
 * ColumnStat record the range of certain column
 */
public class ColumnStat {
    public Integer maxValue; // upper bound
    public Integer minValue; // lower bound

    public ColumnStat() {
        maxValue = null;
        minValue = null;
    }

    public ColumnStat(Integer minValue, Integer maxValue) {
        this.minValue = minValue;
        this.maxValue = maxValue;
    }
}
