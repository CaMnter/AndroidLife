package com.camnter.algorithm.lintcode;

/**
 * Description：452.删除链表中的元素
 *
 * 删除链表中等于给定值val的所有节点。
 *
 * 样例
 * 给出链表 1->2->3->3->4->5->3, 和 val = 3, 你需要返回删除3之后的链表：1->2->4->5。
 *
 * Created by：CaMnter
 */

public class RemoveElements {

    public class ListNode {
        int val;
        ListNode next;


        ListNode(int x) { val = x; }
    }


    public ListNode removeElements(ListNode head, int val) {
        // Write your code here
        if (head == null) return null;
        ListNode first = head, second = head.next;
        while (second != null) {
            if (second.val == val) {
                first.next = second.next;
                second = second.next;
            } else {
                first = first.next;
                second = second.next;
            }

        }
        return head.val != val ? head : head.next;
    }

}
