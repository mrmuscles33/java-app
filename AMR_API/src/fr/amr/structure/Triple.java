package fr.amr.structure;

import fr.amr.utils.StringUtils;

import java.util.Arrays;

public class Triple<A,B,C> extends Pair<A,B> {

    private C three;

    public static Triple<?,?,?> of(Object one, Object two, Object three) {
        return new Triple<>(one, two, three);
    }

    public Triple(A one, B two, C three) {
        super(one, two);
        this.three = three;
    }

    public C getThree() {
        return three;
    }

    public void setThree(C three) {
        this.three = three;
    }

    @Override
    public String toString() {
        return Arrays.asList(getOne(), getTwo(), three).toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Triple<?, ?, ?> triple) {
            return super.equals(triple) && StringUtils.toString(three).equals(triple.getThree());
        }
        return false;
    }
}
