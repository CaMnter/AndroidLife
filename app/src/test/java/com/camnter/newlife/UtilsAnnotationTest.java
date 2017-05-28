package com.camnter.newlife;

import com.camnter.utils.Base64UtilsTest;
import com.camnter.newlife.utils.ChineseUtilsTest;
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
