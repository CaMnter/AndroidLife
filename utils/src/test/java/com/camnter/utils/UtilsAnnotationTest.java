package com.camnter.utils;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Description：UtilsAnnotationTest
 * Created by：CaMnter
 */

@RunWith(Suite.class)
@Suite.SuiteClasses({
                        Base64UtilsTest.class,
                        ChineseUtilsTest.class,
                    })
public class UtilsAnnotationTest {
}
