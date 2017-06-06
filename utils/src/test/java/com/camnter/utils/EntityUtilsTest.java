package com.camnter.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author CaMnter
 */
public class EntityUtilsTest {

    @Test
    public void copyProperties() throws Exception {
        FromClassGetter fromClass = new FromClassGetter();
        fromClass.setStringValue("[FromClass] = Save");
        InnerClass innerClass = new InnerClass();
        innerClass.setStringValue("[InnerClass] = Save");
        fromClass.setInnerClass(innerClass);

        ToClassSetter toClass = new ToClassSetter();
        EntityUtils.copyProperties(fromClass, toClass);

        assertEquals(fromClass.getStringValue(), toClass.getStringValue());
        assertEquals(fromClass.getInnerClass(), toClass.getInnerClass());
    }


    @Test
    public void copyPropertiesExclude() throws Exception {
        FromClassGetter fromClass = new FromClassGetter();
        fromClass.setStringValue("[FromClass] = Save");
        InnerClass innerClass = new InnerClass();
        innerClass.setStringValue("[InnerClass] = Save");
        fromClass.setInnerClass(innerClass);

        ToClassSetter toClass = new ToClassSetter();
        EntityUtils.copyPropertiesExclude(fromClass, toClass, new String[] { "stringValue" });

        assertNull(toClass.getStringValue());
        assertEquals(fromClass.getInnerClass(), toClass.getInnerClass());
    }


    @Test
    public void copyPropertiesInclude() throws Exception {
        FromClassGetter fromClass = new FromClassGetter();
        fromClass.setStringValue("[FromClass] = Save");
        InnerClass innerClass = new InnerClass();
        innerClass.setStringValue("[InnerClass] = Save");
        fromClass.setInnerClass(innerClass);

        ToClassSetter toClass = new ToClassSetter();
        EntityUtils.copyPropertiesInclude(fromClass, toClass, new String[] { "innerClass" });

        assertNull(toClass.getStringValue());
        assertEquals(fromClass.getInnerClass(), toClass.getInnerClass());
    }


    private class FromClassGetter {

        private String stringValue;
        private InnerClass innerClass;


        public void setStringValue(String stringValue) {
            this.stringValue = stringValue;
        }


        public void setInnerClass(InnerClass innerClass) {
            this.innerClass = innerClass;
        }


        public String getStringValue() {
            return stringValue;
        }


        public InnerClass getInnerClass() {
            return innerClass;
        }

    }


    private class ToClassSetter {

        private String stringValue;
        private InnerClass innerClass;


        public void setStringValue(String stringValue) {
            this.stringValue = stringValue;
        }


        public void setInnerClass(InnerClass innerClass) {
            this.innerClass = innerClass;
        }


        public String getStringValue() {
            return stringValue;
        }


        public InnerClass getInnerClass() {
            return innerClass;
        }

    }


    private class InnerClass {

        private String stringValue;


        public void setStringValue(String stringValue) {
            this.stringValue = stringValue;
        }


        public String getStringValue() {
            return stringValue;
        }

    }

}