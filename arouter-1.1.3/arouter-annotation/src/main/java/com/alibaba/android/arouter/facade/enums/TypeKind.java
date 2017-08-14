package com.alibaba.android.arouter.facade.enums;

/**
 * Kind of field type.
 *
 * @author Alex <a href="mailto:zhilong.liu@aliyun.com">Contact me.</a>
 * @version 1.0
 * @since 2017-03-16 19:13:38
 *
 * 主要用于 Processor 过程中，对比 Element 类型
 * 和 JavaPoet 的使用关联
 * 对比 Element 类型，写不同的生成类代码
 */
public enum TypeKind {
    // Base type
    BOOLEAN,
    BYTE,
    SHORT,
    INT,
    LONG,
    CHAR,
    FLOAT,
    DOUBLE,

    // Other type
    STRING,
    PARCELABLE,
    OBJECT;
}
