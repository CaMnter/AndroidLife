package com.camnter.newlife;

import com.camnter.newlife.bean.Contacts;
import com.camnter.newlife.bean.Tag;
import junit.framework.TestCase;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Description：MockitoObjectTest
 * Created by：CaMnter
 */

public class MockitoObjectTest extends TestCase {

    private Tag tag;
    @Mock
    private Contacts contacts;


    @Override protected void setUp() throws Exception {
        super.setUp();
        // 初始化 有Mock 对象
        MockitoAnnotations.initMocks(this);
        // 初始化 没注解的 对象
        this.tag = mock(Tag.class);
    }


    public void testMockObject() {
        assertNotNull(this.tag);
        assertNotNull(this.contacts);
    }

    public void testPile(){
        Contacts contacts = mock(Contacts.class);
        when(contacts.getHeader())
            .thenThrow(new RuntimeException("MockitoObjectTest >>>>>> testPile >>>>>> getHeader RuntimeException"))
            .thenReturn("RuntimeException");
        try {
            contacts.getHeader();
        }catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("MockitoObjectTest >>>>>> testPile >>>>>> "+contacts.getHeader());
        System.out.println("MockitoObjectTest >>>>>> testPile >>>>>> "+contacts.getHeader());
    }

}
