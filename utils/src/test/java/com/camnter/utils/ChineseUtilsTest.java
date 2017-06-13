package com.camnter.utils;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Description：ChineseUtilsTest
 * Created by：CaMnter
 */
public class ChineseUtilsTest {

    private static final String TAG = "ChineseUtilsTest";

    private static final String WORD = "二";
    private static final String SENTENCE = "二次元";
    private static final String SENTENCE_CHINESE = "erciyuan";
    private static final String WORD_UPPER = "E";
    private static final String WORD_LOWER = "e";


    @Test
    public void convert() throws Exception {

    }


    @Test
    public void getSpelling() throws Exception {
        String spelling = ChineseUtils.getInstance().getSpelling(SENTENCE);
        System.out.println(TAG + " >>>>>>>> getSpelling ");
        System.out.println(TAG + " >> getSpelling >> Sentence >> " + SENTENCE);
        System.out.println(TAG + " >> getSpelling >> Sentence chinese >> " + SENTENCE_CHINESE);
        System.out.println(TAG + " >> getSpelling >> spelling >> " + spelling);
        System.out.println();
        assertEquals(SENTENCE_CHINESE, spelling);
    }


    @Test
    public void getSpellingFirstLetterUpper() throws Exception {
        String upper = ChineseUtils.getInstance().getSpellingFirstLetterUpper(WORD);
        System.out.println(TAG + " >>>>>>>> getSpellingFirstLetterUpper ");
        System.out.println(TAG + " >> getSpellingFirstLetterUpper >> word >> " + WORD);
        System.out.println(TAG + " >> getSpellingFirstLetterUpper >> work upper >> " + WORD_UPPER);
        System.out.println(TAG + " >> getSpellingFirstLetterUpper >> upper >> " + upper);
        System.out.println();
        assertEquals(WORD_UPPER, upper);
    }


    @Test
    public void getSpellingFirstLetterLower() throws Exception {
        String lower = ChineseUtils.getInstance().getSpellingFirstLetterLower(WORD);
        System.out.println(TAG + " >>>>>>>> getSpellingFirstLetterLower ");
        System.out.println(TAG + " >> getSpellingFirstLetterUpper >> word >> " + WORD);
        System.out.println(TAG + " >> getSpellingFirstLetterUpper >> work lower >> " + WORD_LOWER);
        System.out.println(TAG + " >> getSpellingFirstLetterUpper >> lower >> " + lower);
        System.out.println();
        assertEquals(WORD_LOWER, lower);
    }

}