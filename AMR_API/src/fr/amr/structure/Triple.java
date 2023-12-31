package fr.amr.structure;

import fr.amr.utils.StringUtils;

import java.util.Arrays;

public class Triple extends Pair {

    private Object three;

    public static Triple of(Object one, Object two, Object three) {
        return new Triple(one, two, three);
    }

    public Triple(Object one, Object two, Object three) {
        super(one, two);
        this.three = three;
    }

    public Object getThree() {
        return three;
    }

    public void setThree(Object three) {
        this.three = three;
    }

    @Override
    public String toString() {
        return Arrays.asList(getOne(), getTwo(), three).toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Triple triple) {
            return super.equals(triple) && StringUtils.toString(three).equals(triple.getThree());
        }
        return false;
    }
}
