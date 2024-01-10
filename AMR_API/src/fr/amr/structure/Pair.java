package fr.amr.structure;

import fr.amr.utils.StringUtils;

import java.util.Arrays;

public class Pair<A, B> {

    private A one;
    private B two;

    public static Pair<?, ?> of(Object one, Object two) {
        return new Pair<>(one, two);
    }

    public Pair(A one, B two) {
        this.one = one;
        this.two = two;
    }

    public A getOne() {
        return one;
    }

    public Object getTwo() {
        return two;
    }

    public void setOne(A one) {
        this.one = one;
    }

    public void setTwo(B two) {
        this.two = two;
    }

    @Override
    public String toString() {
        return Arrays.asList(one, two).toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Pair<?, ?> pair) {
            return StringUtils.toString(one).equals(pair.getOne()) && StringUtils.toString(two).equals(pair.getTwo());
        }
        return false;
    }
}
