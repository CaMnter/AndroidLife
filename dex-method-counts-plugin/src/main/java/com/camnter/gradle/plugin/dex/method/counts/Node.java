package com.camnter.gradle.plugin.dex.method.counts;

import java.io.PrintStream;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * @author CaMnter
 */

public class Node {

    private int count = 0;
    private static final PrintStream out = System.out;
    private NavigableMap<String, Node> children = new TreeMap<String, Node>();


    public void output(String indent) {
        if (indent.length() == 0) {
            out.println("<root>: " + count);
        }
        indent += "    ";
        for (String name : children.navigableKeySet()) {
            Node child = children.get(name);
            out.println(indent + name + ": " + child.count);
            child.output(indent);
        }
    }

}
