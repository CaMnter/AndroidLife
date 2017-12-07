package com.camnter.gradle.plugin.dex.method.counts.struct;

import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * @author CaMnter
 */

public class Node {

    public int count = 0;
    public StringBuilder stringBuilder = new StringBuilder();
    public NavigableMap<String, Node> children = new TreeMap<String, Node>();


    public StringBuilder output(String indent) {
        if (indent.length() == 0) {
            this.stringBuilder
                .append("<root>: ")
                .append(count)
                .append("\n");
        }
        indent += "    ";
        for (String name : children.navigableKeySet()) {
            Node child = children.get(name);
            this.stringBuilder
                .append(indent)
                .append(name)
                .append(": ")
                .append(child.count)
                .append("\n");
            child.output(indent);
        }
        return stringBuilder;
    }

}
