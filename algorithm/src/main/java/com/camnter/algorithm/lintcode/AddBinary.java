package com.camnter.algorithm.lintcode;

/**
 * Description：408.二进制求和
 *
 * a = 11
 * b = 1
 *
 * 返回 100
 *
 * Created by：CaMnter
 */

public class AddBinary {

    public static void main(String[] args) {
        System.out.println(addBinary("01", "10"));
        System.out.println(addBinaryByJiayi("01", "10"));
        System.out.println(addBinaryByJiayi("11", "10"));
        System.out.println(addBinaryCaMnter("11", "10"));
    }


    public static String addBinaryCaMnter(String a, String b) {
        final char one = '1';
        final char zero = '0';

        String longerString = a;
        String shorterString = b;
        if (b.length() > a.length()) {
            longerString = b;
            shorterString = a;
        }

        int carryBit = 0;
        int longerLength = longerString.length();
        final int shorterLength = shorterString.length();
        char[] resultChars = new char[longerLength + 1];

        int resultIndex = resultChars.length - 1;
        int i = 0;
        for (; i < shorterLength; i++) {
            final int longerValue = longerString.charAt(longerLength - 1 - i) - zero;
            final int shorterValue = shorterString.charAt(shorterLength - 1 - i) - zero;

            // 0,1,2,3: 0,2=0; 1,3=1
            final int sum = longerValue + shorterValue + carryBit;
            carryBit = sum > 1 ? 1 : 0;
            resultChars[resultIndex] = sum == 0 || sum == 2 ? zero : one;
            resultIndex--;
        }
        for (; i < longerLength; i++) {
            final int longerValue = longerString.charAt(longerLength - 1 - i) - zero;

            // 0,1,2: 0,2=0; 1=1
            final int sum = longerValue + carryBit;
            carryBit = sum > 1 ? 1 : 0;
            resultChars[resultIndex] = sum == 0 || sum == 2 ? zero : one;
            resultIndex--;
        }
        resultChars[0] = (char) (carryBit + zero);
        if (carryBit > 0) {
            longerLength++;
        }
        return new String(resultChars, resultChars.length - longerLength, longerLength);
    }


    public static String addBinary(String a, String b) {
        // Write your code here
        final String one = "1";
        final String zero = "0";
        final StringBuilder result = new StringBuilder();
        int aIndex = a.length();
        int bIndex = b.length();
        int sum = 0;
        while (aIndex > 0 || bIndex > 0) {
            if (aIndex > 0) {
                sum += a.charAt(aIndex - 1) - '0';
                aIndex--;
            }
            if (bIndex > 0) {
                sum += b.charAt(bIndex - 1) - '0';
                bIndex--;
            }
            if (sum == 0 || sum == 1) {
                result.insert(0, sum);
                sum = 0;
            } else {
                result.insert(0, sum == 2 ? zero : one);
                sum = 1;
            }
        }
        if (sum == 1) {
            result.insert(0, one);
        }
        return result.toString();
    }


    public static String addBinaryByJiayi(String a, String b) {
        String longer = b;
        String shorter = a;

        if (a.length() > b.length()) {
            longer = a;
            shorter = b;
        }

        int carry = 0;
        int longerLength = longer.length();
        int shorterLength = shorter.length();

        char[] result = new char[longerLength + 1];
        int index = result.length - 1;
        int size = shorter.length();
        int itr = 0;
        for (; itr < size; itr++) {
            int lDigit = longer.charAt(longerLength - 1 - itr) - '0';
            int sDigit = shorter.charAt(shorterLength - 1 - itr) - '0';

            int r = lDigit + sDigit + carry;
            carry = r > 1 ? 1 : 0;
            char tmp = (r == 1 || r == 3) ? '1' : '0';
            result[index] = tmp;
            index--;
        }

        for (; itr < longerLength; itr++) {
            int r = longer.charAt(longerLength - 1 - itr) - '0' + carry;
            carry = r > 1 ? 1 : 0;
            char tmp = (r == 1 || r == 3) ? '1' : '0';
            result[index] = tmp;
            index--;
        }

        if (carry > 0) {
            result[0] = (char) (carry + '0');
            longerLength++;
        }

        return new String(result, result.length - longerLength, longerLength);
    }

}
