package com.camnter.utils;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Description：UtilsTest
 * Created by：CaMnter
 */

public class UtilsTest extends TestCase {
    public static Test suite() {
        TestSuite suite = new TestSuite("com.camnter.newlife.utils");
        // 添加测试用例
        suite.addTest(new JUnit4TestAdapter(Base64UtilsTest.class));
        suite.addTest(new JUnit4TestAdapter(ChineseUtilsTest.class));
        return suite;
    }
}
