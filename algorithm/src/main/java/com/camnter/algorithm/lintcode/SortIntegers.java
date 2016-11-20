package com.camnter.algorithm.lintcode;

/**
 * Description：整数排序
 *
 * 对于数组 [3, 2, 1, 4, 5], 排序后为：[1, 2, 3, 4, 5]。
 *
 * Created by：CaMnter
 */

public class SortIntegers {
    public static void main(String[] args) {
        int[] a = { 3, 2, 1, 4, 5 };
        sortIntegers(a);
        for (int e : a) {
            System.out.print(e + "\t");
        }
    }


    /**
     * @param A an integer array
     */
    private static void sortIntegers(int[] A) {
        // Write your code here
        for (int i = 0; i < A.length; i++) {
            int selected = i;
            for (int j = i + 1; j < A.length; j++) {
                if (A[j] < A[selected]) {
                    selected = j;
                }
            }
            int temp = A[i];
            A[i] = A[selected];
            A[selected] = temp;
        }
    }
}
