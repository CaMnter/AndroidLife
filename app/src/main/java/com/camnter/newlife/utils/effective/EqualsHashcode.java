package com.camnter.newlife.utils.effective;

/**
 * Description：EqualsHashcode
 * Created by：CaMnter
 */

public class EqualsHashcode {

    private volatile int hashCode;

    private int a;
    private int b;
    private int c;
    private int d;


    @Override public boolean equals(Object o) {
        if (o == null || !(o instanceof EqualsHashcode)) return false;
        EqualsHashcode that = (EqualsHashcode) o;
        return this.a == that.a && b == that.b && c == that.c;
    }


    @Override public int hashCode() {
        int result = this.hashCode;
        if (result == 0) {
            result = 17;
            result = 31 * result + a;
            result = 31 * result + b;
            result = 31 * result + c;
            result = 31 * result + d;
            this.hashCode = result;
        }
        return result;
    }

}
