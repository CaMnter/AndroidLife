package me.ele.uetool.base;

import java.util.List;
import me.ele.uetool.base.item.Item;

/**
 * 这个接口用于
 * 定义功能面板的功能属性，比如这个面板有哪些功能
 */
public interface IAttrs {

    List<Item> getAttrs(Element element);
}
