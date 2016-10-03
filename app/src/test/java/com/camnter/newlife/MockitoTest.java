package com.camnter.newlife;

import java.util.LinkedList;
import java.util.List;
import junit.framework.TestCase;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * Description：MockitoTest
 * Created by：CaMnter
 */

public class MockitoTest extends TestCase {

    private static final String MOCK_TEXT = "CaMnter";


    /**
     * 1. 让我们验证某些行为
     * 一旦创建，mock对象会记住所有的交互。然后你可以有选择性的验证你感兴趣的任何交互。
     */
    @SuppressWarnings("unchecked")
    public void test1() {
        System.out.println("\nMockitoTest >>>>>> [test1] >>>>>>");
        // 创建mock
        List mockedList = mock(List.class);

        // 使用 mock 对象
        mockedList.add("one");
        mockedList.clear();

        // 验证
        verify(mockedList).add("one");
        verify(mockedList).clear();
    }


    /**
     * 2. 再来一点 stubbing?
     *
     * 默认情况， 对于返回一个值的所有方法， mock 对象在适当的时候要不返回 null，基本类型/基本类型包装类，或
     * 者一个空集合。比如 int/Integer 返回0, boolean/Boolean 返回 false。
     *
     * 存根 ( stub ) 可以覆盖： 例如通用存根可以固定搭建但是测试方法可以覆盖它。请注意覆盖存根是潜在的代码异
     * 味( code smell )，说明存根太多了
     *
     * 一旦做了存根，方法将总是返回存根的值，无论这个方法被调用多少次
     *
     * 最后一个存根总是更重要 - 当你用同样的参数对同一个方法做了多次存根时。换句话说：存根顺序相关，但是它
     * 只在极少情况下有意义。例如，当需要存根精确的方法调用次数，或者使用参数匹配器等。
     */
    public void test2() {
        System.out.println("\nMockitoTest >>>>>> [test2] >>>>>>");
        // 可以mock具体的类，而不仅仅是接口
        LinkedList mockedList = mock(LinkedList.class);

        // 存根(stubbing)
        when(mockedList.get(0)).thenReturn("first");
        when(mockedList.get(1)).thenThrow(new RuntimeException());

        // 下面会打印 "first"
        System.out.println(mockedList.get(0));

        // 下面会抛出运行时异常
        try {
            System.out.println(mockedList.get(1));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 下面会打印"null" 因为get(999)没有存根(stub)
        System.out.println(mockedList.get(999));

        // 虽然可以验证一个存根的调用，但通常这是多余的
        // 如果你的代码关心get(0)返回什么，那么有某些东西会出问题(通常在verify()被调用之前)
        // 如果你的代码不关系get(0)返回什么，那么它不需要存根。如果不确信，那么还是验证吧
        verify(mockedList).get(0);
    }


    /**
     * 3. 参数匹配器
     * mockito使用java原生风格来验证参数的值： 使用equals()方法。有些时候，如果需要额外的灵活性，应该使用参数匹配器：
     */
    public void test3() {
        System.out.println("\nMockitoTest >>>>>> [test3] >>>>>>");
        LinkedList mockedList = mock(LinkedList.class);

        //使用内建anyInt()参数匹配器
        when(mockedList.get(anyInt())).thenReturn("element");

        //使用自定义匹配器( 这里的 isValid() 返回自己的匹配器实现 )
        when(mockedList.contains(argThat(null))).thenReturn(false);

        //下面会打印 "element"
        System.out.println(mockedList.get(999));

        // 同样可以用参数匹配器做验证
        verify(mockedList).get(anyInt());
    }


    /**
     * 4. 验证精确调用次数/至少X次/从不
     */
    @SuppressWarnings("unchecked")
    public void test4() {
        System.out.println("\nMockitoTest >>>>>> [test4] >>>>>>");
        LinkedList mockedList = mock(LinkedList.class);
        // 使用 mock
        mockedList.add("once");

        mockedList.add("twice");
        mockedList.add("twice");

        mockedList.add("three times");
        mockedList.add("three times");
        mockedList.add("three times");

        // 下面两个验证是等同的 - 默认使用 times(1)
        verify(mockedList).add("once");
        verify(mockedList, times(1)).add("once");

        // 验证精确调用次数
        verify(mockedList, times(2)).add("twice");
        verify(mockedList, times(3)).add("three times");

        //使用using never()来验证. never()相当于 times(0)
        verify(mockedList, never()).add("never happened");

        //使用 atLeast()/atMost()来验证
        verify(mockedList, atLeastOnce()).add("three times");
        verify(mockedList, atMost(5)).add("three times");
    }


    /**
     * 5. 使用 exception 做 void 方法的存根
     */
    public void test5() {
        System.out.println("\nMockitoTest >>>>>> [test5] >>>>>>");
        LinkedList mockedList = mock(LinkedList.class);
        doThrow(new RuntimeException()).when(mockedList).clear();

        // 下面会抛出 RuntimeException:
        try {
            mockedList.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 7. 确保交互从未在mock对象上发生
     */
    @SuppressWarnings("unchecked")
    public void test7() {
        System.out.println("\nMockitoTest >>>>>> [test7] >>>>>>");
        LinkedList mockOne = mock(LinkedList.class);
        LinkedList mockTwo = mock(LinkedList.class);
        LinkedList mockThree = mock(LinkedList.class);

        // 使用 mock - 仅有 mockOne 有交互
        mockOne.add("one");

        // 普通验证
        verify(mockOne).add("one");

        // 验证方法从未在 mock 对象上调用
        verify(mockOne, never()).add("two");

        //验证其他mock没有交互
        verifyZeroInteractions(mockTwo, mockThree);
    }

}
