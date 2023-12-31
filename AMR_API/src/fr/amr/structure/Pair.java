package fr.amr.structure;

import fr.amr.utils.StringUtils;

import java.util.Arrays;

public class Pair {

    private Object one;
    private Object two;

    public static Pair of(Object one, Object two) {
        return new Pair(one, two);
    }

    public Pair(Object one, Object two) {
        this.one = one;
        this.two = two;
    }

    public Object getOne() {
        return one;
    }

    public Object getTwo() {
        return two;
    }

    public void setOne(Object one) {
        this.one = one;
    }

    public void setTwo(Object two) {
        this.two = two;
    }

    @Override
    public String toString() {
        return Arrays.asList(one, two).toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Pair pair) {
            return StringUtils.toString(one).equals(pair.getOne()) && StringUtils.toString(two).equals(pair.getTwo());
        }
        return false;
    }
}
