package util.unionfind;

public class Constraints {
    private Integer upperBound;
    private Integer lowerBound;
    private Integer equality;
    private int id;

    public Constraints(int id) {
        lowerBound = null;
        upperBound = null;
        equality = null;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Constraints(int id, Integer upperBound, Integer lowerBound, Integer equality) {
        this.id = id;
        this.upperBound = upperBound;
        this.lowerBound = lowerBound;
        this.equality = equality;
    }

    public Integer getUpperBound() {
        return upperBound;
    }

    public void setUpperBound(Integer input) {
        if (input == null) {
            return;
        }
        if (lowerBound != null && input < lowerBound) {
            throw new IllegalArgumentException("lower > upper");
        }
        if (upperBound == null || input < upperBound) {
            upperBound = input;
        }
    }

    public Integer getLowerBound() {
        return lowerBound;
    }

    public void setLowerBound(Integer input) {
        if (input == null) {
            return;
        }
        if (upperBound != null && input > upperBound) {
            throw new IllegalArgumentException("lower > upper");
        }
        if (lowerBound == null || input > lowerBound) {
            lowerBound = input;
        }
    }

    public Integer getEquality() {
        return equality;
    }

    public void setEquality(Integer input) {
        if (input == null) {
            return;
        }
        lowerBound = null;
        upperBound = null;
        equality = input;
        setLowerBound(input);
        setUpperBound(input);
    }

    public void merge(Constraints that) {
        if (that == null) {
            throw new NullPointerException();
        }
        setLowerBound(that.getLowerBound());
        setUpperBound(that.getUpperBound());
        setEquality(that.getEquality());
    }

    public boolean isNull() {
        return upperBound == null && lowerBound == null && equality == null;
    }

    public String toString() {
        return "Lower: " + this.lowerBound + " upper: " + this.getUpperBound()
                + " equality: " + this.getEquality();
    }
}
