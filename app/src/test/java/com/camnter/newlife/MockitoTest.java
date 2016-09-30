package com.camnter.newlife;

import java.util.List;
import junit.framework.TestCase;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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
}
