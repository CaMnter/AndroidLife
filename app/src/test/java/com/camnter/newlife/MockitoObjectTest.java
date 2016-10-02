package com.camnter.newlife;

import com.camnter.newlife.bean.Contacts;
import com.camnter.newlife.bean.Tag;
import junit.framework.TestCase;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.mock;

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

}
