package com.camnter.algorithm.lintcode;

/**
 * Description：
 *
 * a 和 b 都是 32位 整数
 *
 * 使用位运算符
 *
 * 如果 a=1 并且 b=2，返回3
 *
 * Created by：CaMnter
 */

public class Aplusb {

    public static void main(String[] args) {
        System.out.println(aplusb(6, 6));
    }


    public static int aplusb(int a, int b) {
        // write your code here, try to do it without arithmetic operators.
        if (a == 0) return b;
        if (b == 0) return a;
        // 进位
        final int x = (a & b) << 1;
        // 不进位
        final int y = a ^ b;
        return aplusb(x, y);
    }
}
