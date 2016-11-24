package com.camnter.algorithm.lintcode;

/**
 * Description：366.斐波纳契数列
 *
 * 查找斐波纳契数列中第 N 个数。
 *
 * 所谓的斐波纳契数列是指：
 *
 * 前2个数是 0 和 1 。
 * 第 i 个数是第 i-1 个数和第i-2 个数的和。
 *
 * 斐波纳契数列的前10个数字是：
 *
 * 0, 1, 1, 2, 3, 5, 8, 13, 21, 34 ...
 *
 *
 *
 * 样例
 * 给定 1，返回 0
 * 给定 2，返回 1
 * 给定 10，返回 34
 * Created by：CaMnter
 */

public class Fibonacci {
    public static void main(String[] args) {
        // 10 > 34
        System.out.println(fibonacci(10));
    }


    private static int fibonacci(int n) {
        // write your code here
        if (n == 1) return 0;
        if (n == 2) return 1;
        int firstOne = 0;
        int firstTwo = 1;
        int sum = 0;
        for (int i = 0; i < n - 2; i++) {
            sum = firstOne + firstTwo;
            firstOne = firstTwo;
            firstTwo = sum;
        }
        return sum;
    }
}
