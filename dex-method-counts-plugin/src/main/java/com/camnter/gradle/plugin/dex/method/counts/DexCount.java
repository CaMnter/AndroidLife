package com.camnter.gradle.plugin.dex.method.counts;

import java.io.PrintStream;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author CaMnter
 */

public class DexCount {

    private static final PrintStream out = System.out;
    private final OutputStyle outputStyle;
    private final Node packageTree;
    private final Map<String, IntHolder> packageCount;
    private int overallCount = 0;


    DexCount(OutputStyle outputStyle) {
        this.outputStyle = outputStyle;
        this.packageTree = this.outputStyle == OutputStyle.TREE ? new Node() : null;
        this.packageCount = this.outputStyle == OutputStyle.FLAT
                            ? new TreeMap<>() : null;
    }

    // TODO generate method


    void output() {
        outputStyle.output(this);
    }


    int getOverallCount() {
        return overallCount;
    }


    public Node getPackageTree() {
        return this.packageTree;
    }


    public Map<String, IntHolder> getPackageCount() {
        return this.packageCount;
    }

}
