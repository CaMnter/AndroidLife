package com.camnter.algorithm.lintcode;

/**
 * Description：比较字符串
 *
 * 比较两个字符串 A 和 B，确定 A 中是否包含 B 中所有的字符。字符串 A 和 B 中的字符都是 大写字母
 *
 * 给出 A = "ABCD" B = "ACD"，返回 true
 * 给出 A = "ABCD" B = "AABC"， 返回 false
 *
 * Created by：CaMnter
 */

public class CompareStrings {

    public static void main(String[] args) {
        System.out.println(compareStrings("ABC", ""));
        System.out.println(compareStrings("ABCD", "ACD"));
        System.out.println(compareStrings("ABCD", "AABC"));
    }


    /**
     * @param A : A string includes Upper Case letters
     * @param B : A string includes Upper Case letter
     * @return :  if string A contains all of the characters in B return true else return false
     */
    public static boolean compareStrings(String A, String B) {
        if (B == null) return false;
        if ("".equals(B)) return true;
        final String[] aArray = A.split("");
        final String[] bArray = B.split("");
        for (String a : aArray) {
            for (int j = 0; j < bArray.length; j++) {
                if (bArray[j] == null) continue;
                if (bArray[j].equals(a)) {
                    bArray[j] = null;
                    break;
                }
            }
        }
        for (String b : bArray) {
            if (b != null) return false;
        }
        return true;
    }

}
