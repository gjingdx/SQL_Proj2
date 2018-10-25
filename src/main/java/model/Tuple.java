package model;

import java.util.Arrays;

/**
 * Tuple class
 * Created by Yufu Mo
 */
public class Tuple {
    private int[] data; // string array to store data

    /**
     * Constructor for tuple
     *
     * @param s
     */
    public Tuple(String s) {
        String[] sData = s.split(",");
        data = new int[sData.length];
        for (int i = 0; i < sData.length; ++i) {
            data[i] = Integer.parseInt(sData[i]);
        }
    }

    /**
     * Overload constructor for tuple with empty input
     *
     * @param length
     */
    public Tuple(int length) {
        data = new int[length];
    }

    /**
     * Overload constructor with data input
     *
     * @param data
     */
    public Tuple(int[] data) {
        this.data = data;
    }

    /**
     * Get data by index
     *
     * @param index
     * @return int
     */
    public int getDataAt(int index) {
        return data[index];
    }

    /**
     * get the length of data
     *
     * @return int
     */
    public int getDataLength() {
        return data.length;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Tuple tuple = (Tuple) o;

        return Arrays.equals(data, tuple.data);
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder(Arrays.toString(data));
        return res.toString().replaceAll("\\[", "")
                .replaceAll("\\]", "")
                .replaceAll("\\s+", "");
    }
}
