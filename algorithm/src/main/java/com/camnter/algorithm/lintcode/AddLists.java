package com.camnter.algorithm.lintcode;

/**
 * Description：167.链表求和
 *
 * 你有两个用链表代表的整数，其中每个节点包含一个数字。数字存储按照在原来整数中相反的顺序，使得第一个数字
 * 位于链表的开头。写出一个函数将两个整数相加，用链表形式返回和。
 *
 * 给出两个链表
 * 3->1->5->null 和 5->9->2->null
 * 返回 8->0->8->null
 *
 * Created by：CaMnter
 */

public class AddLists {

    public static void main(String args[]) {
        ListNode l1 = new ListNode(8);
        l1.next = new ListNode(6);

        ListNode l2 = new ListNode(6);
        l2.next = new ListNode(4);
        l2.next.next = new ListNode(8);

        printListNode(addLists(l1, l2));
    }


    private static void printListNode(ListNode printNode) {
        boolean first = true;
        ListNode currentNode = printNode;
        while (true) {
            if (!first) {
                System.out.print("->");
            } else {
                first = false;
            }
            if (currentNode == null) {
                System.out.print("null");
                break;
            } else {
                System.out.print(currentNode.val);
            }
            currentNode = currentNode.next;
        }
        System.out.println("");
    }


    public static class ListNode {
        int val;
        ListNode next;


        ListNode(int x) {
            val = x;
            next = null;
        }
    }


    /**
     * @param l1: the first list
     * @param l2: the second list
     * @return: the sum list of l1 and l2
     */
    public static ListNode addLists(ListNode l1, ListNode l2) {
        // write your code here
        if (l1 == null) return l2;
        if (l2 == null) return l1;

        int sum;
        int carryValue = 0;
        ListNode currentFirst = l1;
        ListNode currentSecond = l2;
        boolean nextNonNull = false;
        do {
            if (nextNonNull) {
                currentFirst = currentFirst.next;
                currentSecond = currentSecond.next;
            }
            sum = currentFirst.val + currentSecond.val + carryValue;
            if (sum >= 10) {
                carryValue = sum / 10;
                currentFirst.val = sum - 10;
            } else {
                carryValue = 0;
                currentFirst.val = sum;
            }
        } while (nextNonNull = (currentFirst.next != null && currentSecond.next != null));

        if (currentFirst.next == null && currentSecond.next == null) {
            if (carryValue > 0) currentFirst.next = new ListNode(carryValue);
        } else if (currentFirst.next == null) {

            currentFirst.next = currentSecond.next;
            currentSecond = currentFirst.next;

            while (carryValue > 0) {
                sum = currentSecond.val + carryValue;
                if (sum >= 10) {
                    carryValue = sum / 10;
                    currentSecond.val = sum - 10;
                } else {
                    carryValue = 0;
                    currentSecond.val = sum;
                }
                if (currentSecond.next != null) {
                    currentSecond = currentSecond.next;
                } else {
                    if (carryValue > 0) {
                        currentSecond.next = new ListNode(carryValue);
                    }
                    break;
                }
            }
        } else {
            while (carryValue > 0) {
                if (currentFirst.next != null) {
                    sum = carryValue + currentFirst.next.val;
                    carryValue = sum / 10;
                    currentFirst.next.val = sum >= 10 ? sum - 10 : sum;
                    currentFirst = currentFirst.next;
                } else {
                    currentFirst.next = new ListNode(carryValue);
                    break;
                }
            }
        }
        return l1;
    }
}
