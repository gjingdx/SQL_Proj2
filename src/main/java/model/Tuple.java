package model;

import java.util.Arrays;

public class Tuple {
    private long[] data; // string array to store data

    /**
     * Constructor for tuple
     * @param s
     */
    public Tuple(String s) {
        String[] sData = s.split(",");
        data = new long[sData.length];
        for(int i = 0; i < sData.length; ++i){
            data[i] = Long.parseLong(sData[i]);
        }
    }

    /**
     * Overload constructor for tuple with empty input
     * @param length
     */
    public Tuple(int length) {
        data = new long[length];
    }


    /**
     * Overload constructor with data input
     * @param data
     */
    public Tuple(long[] data) {
        this.data = data;
    }

    /**
     * Get data by index
     * @param index
     * @return long
     */
    public long getDataAt(int index){
        return data[index];
    }

    /**
     * get the length of data
     * @return int
     */
    public int getDataLength(){
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
//
//    @Override
//    public int hashCode() {
//        return Arrays.hashCode(data);
//    }


    @Override
    public String toString() {
        return "Tuple{" +
                "data=" + Arrays.toString(data) +
                '}';
    }
}
