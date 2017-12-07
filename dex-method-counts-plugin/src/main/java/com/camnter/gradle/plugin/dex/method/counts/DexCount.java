package com.camnter.gradle.plugin.dex.method.counts;

import com.android.dexdeps.DexData;
import com.camnter.gradle.plugin.dex.method.counts.struct.Filter;
import com.camnter.gradle.plugin.dex.method.counts.struct.IntHolder;
import com.camnter.gradle.plugin.dex.method.counts.struct.Node;
import com.camnter.gradle.plugin.dex.method.counts.struct.OutputStyle;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author CaMnter
 */

public abstract class DexCount {

    final OutputStyle outputStyle;
    final Node packageTree;
    final Map<String, IntHolder> packageCount;
    final StringBuilder builder;
    int overallCount = 0;


    DexCount(OutputStyle outputStyle) {
        this.outputStyle = outputStyle;
        this.packageTree = this.outputStyle == OutputStyle.TREE ? new Node() : null;
        this.packageCount = this.outputStyle == OutputStyle.FLAT
                            ? new TreeMap<>() : null;
        this.builder = new StringBuilder();
    }


    public abstract void generate(DexData dexData,
                                  boolean includeClasses,
                                  String packageFilter,
                                  int maxDepth,
                                  Filter filter);


    public StringBuilder output() {
        return this.outputStyle.output(this);
    }


    public int getOverallCount() {
        return this.overallCount;
    }


    public Node getPackageTree() {
        return this.packageTree;
    }


    public StringBuilder getBuilder() {
        return this.builder;
    }


    public Map<String, IntHolder> getPackageCount() {
        return this.packageCount;
    }

}
