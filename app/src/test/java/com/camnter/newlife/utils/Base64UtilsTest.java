package com.camnter.newlife.utils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Description：Base64UtilsTest
 * Created by：CaMnter
 */
public class Base64UtilsTest {

    private static final String TAG = "Base64UtilsTest";

    private static final String TEST_STRING
        = "{\"sid\":\"08:00:27:69:bb:5c1474966148902\",\"be\":1474966148902,\"en\":1474966148902,\"ch\":\"lmlc\",\"mo\":\"unknown-Custom Phone - 5.0.0 - API 21 - 768x1280\",\"ne\":\"wifi\",\"os\":\"5.0\",\"op\":\"Android\",\"re\":\"768x1184\",\"de\":\"08:00:27:69:bb:5c\",\"ac\":\"jjjtest34@163.com\",\"ve\":\"2.0.0\",\"ex\":\"\",\"pv\":[],\"ev\":[],\"seq\":0}";
    private static final byte[] TEST_STRING_BYTE = TEST_STRING.getBytes();
    private static final String TEST_RESULT
        = "eyJzaWQiOiIwODowMDoyNzo2OTpiYjo1YzE0NzQ5NjYxNDg5MDIiLCJiZSI6MTQ3NDk2NjE0ODkwMiwiZW4iOjE0NzQ5NjYxNDg5MDIsImNoIjoibG1sYyIsIm1vIjoidW5rbm93bi1DdXN0b20gUGhvbmUgLSA1LjAuMCAtIEFQSSAyMSAtIDc2OHgxMjgwIiwibmUiOiJ3aWZpIiwib3MiOiI1LjAiLCJvcCI6IkFuZHJvaWQiLCJyZSI6Ijc2OHgxMTg0IiwiZGUiOiIwODowMDoyNzo2OTpiYjo1YyIsImFjIjoiampqdGVzdDM0QDE2My5jb20iLCJ2ZSI6IjIuMC4wIiwiZXgiOiIiLCJwdiI6W10sImV2IjpbXSwic2VxIjowfQ==";


    @Before
    public void setUp() throws Exception {
    }


    @After
    public void tearDown() throws Exception {
    }


    @Test
    public void encode() throws Exception {
        System.out.println(TAG + " >>>>>>>> encode ");
        String encodeValue = Base64Utils.encode(TEST_STRING_BYTE);
        System.out.println(TAG + " >> encode >> string >> " + TEST_STRING);
        System.out.println(TAG + " >> encode >> encodeValue >> " + encodeValue);
        System.out.println(TAG + " >> encode >> targetEncodeValue >> " + TEST_RESULT);
        System.out.println();
        assertEquals(encodeValue, TEST_RESULT);
    }


    @Test
    public void decode() throws Exception {
        System.out.println(TAG + " >>>>>>>> decode ");
        byte[] decodeValue = Base64Utils.decode(TEST_RESULT);
        String decodeString = new String(decodeValue);
        System.out.println(TAG + " >> decode >> string >> " + TEST_RESULT);
        System.out.println(TAG + " >> decode >> decodeValue >> " + decodeString);
        System.out.println(TAG + " >> decode >> targetDecodeValue >> " + TEST_STRING);
        System.out.println();
        assertEquals(decodeString, TEST_STRING);
    }


    @Test
    public void bitmapToBase64() throws Exception {

    }


    @Test
    public void base64ToBitmap() throws Exception {

    }

}