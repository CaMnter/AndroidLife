package me.ele.uetool.base.item;

import me.ele.uetool.base.Element;

/**
 * uetool「文本条目」
 */
public class ElementItem extends TitleItem {

    private Element element;

    public ElementItem(String name, Element element) {
        super(name);
        this.element = element;
    }

    public Element getElement() {
        return element;
    }
}
