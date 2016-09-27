package com.camnter.newlife;

import com.camnter.newlife.utils.Base64UtilsTest;
import com.camnter.newlife.utils.ChineseUtilsTest;
import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Description：UtilsTest
 * Created by：CaMnter
 */

public class UtilsTest {
    public static Test suite() {
        TestSuite suite = new TestSuite("com.camnter.newlife.utils");
        // 添加测试用例
        suite.addTest(new JUnit4TestAdapter(Base64UtilsTest.class));
        suite.addTest(new JUnit4TestAdapter(ChineseUtilsTest.class));
        return suite;
    }
}
