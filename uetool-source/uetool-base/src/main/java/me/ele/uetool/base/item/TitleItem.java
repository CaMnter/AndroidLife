package me.ele.uetool.base.item;

/**
 * uetool「标题条目」
 */
public class TitleItem extends Item {

    private String name;

    public TitleItem(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
