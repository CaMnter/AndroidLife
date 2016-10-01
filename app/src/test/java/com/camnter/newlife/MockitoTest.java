package com.camnter.newlife;

import java.util.LinkedList;
import java.util.List;
import junit.framework.TestCase;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Description：MockitoTest
 * Created by：CaMnter
 */

public class MockitoTest extends TestCase {

    private static final String MOCK_TEXT = "CaMnter";


    @SuppressWarnings("unchecked")
    public void testList() {
        List mockedList = mock(List.class);

        mockedList.add(MOCK_TEXT);
        mockedList.clear();

        verify(mockedList).add(MOCK_TEXT);
        verify(mockedList).clear();
        // verify(mockedList).remove(MOCK_TEXT);
    }


    public void testStub() {
        System.out.println("MockitoTest >>>>>> testStub  ");
        LinkedList mockedList = mock(LinkedList.class);

        // 测试桩,调用 get(0) 时,返回 "CaMnter"
        when(mockedList.get(0)).thenReturn(MOCK_TEXT);
        // 测试桩,调用 get(1) 时,抛出异常
        when(mockedList.get(1)).thenThrow(new RuntimeException("testStub custom exception"));

        System.out.println(
            "MockitoTest >>>>>> testStub >>>>>> mockedList.get(0) >>>>>>" + mockedList.get(0));
        try {
            System.out.println(
                "MockitoTest >>>>>> testStub >>>>>> mockedList.get(1) >>>>>> " + mockedList.get(1));
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(
            "MockitoTest >>>>>> testStub >>>>>> mockedList.get(666) >>>>>> " + mockedList.get(666));

    }


    @SuppressWarnings("unchecked")
    public void testArgument() {
        System.out.println("MockitoTest >>>>>> testArgument  ");
        LinkedList mockedList = mock(LinkedList.class);

        when(mockedList.get(anyInt())).thenReturn(MOCK_TEXT);
        System.out.println(
            "MockitoTest >>>>>> testStub >>>>>> mockedList.get(666) >>>>>> " + mockedList.get(666));
    }


    @SuppressWarnings("unchecked")
    public void testCount() {
        LinkedList mockedList = mock(LinkedList.class);
        mockedList.add(MOCK_TEXT);
        mockedList.add(MOCK_TEXT);
        mockedList.add(MOCK_TEXT);
        mockedList.add(MOCK_TEXT);
        mockedList.add(MOCK_TEXT);
        mockedList.add(MOCK_TEXT);
        verify(mockedList, times(6)).add(MOCK_TEXT);
        verify(mockedList, atLeast(2)).add(MOCK_TEXT);
        verify(mockedList, atMost(6)).add(MOCK_TEXT);
        verify(mockedList, never()).remove(MOCK_TEXT);
    }

}
