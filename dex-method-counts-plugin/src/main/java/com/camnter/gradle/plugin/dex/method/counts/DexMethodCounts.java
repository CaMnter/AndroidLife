package com.camnter.gradle.plugin.dex.method.counts;

import com.android.dexdeps.ClassRef;
import com.android.dexdeps.DexData;
import com.android.dexdeps.MethodRef;
import com.android.dexdeps.Output;
import com.camnter.gradle.plugin.dex.method.counts.struct.Filter;
import com.camnter.gradle.plugin.dex.method.counts.struct.IntHolder;
import com.camnter.gradle.plugin.dex.method.counts.struct.Node;
import com.camnter.gradle.plugin.dex.method.counts.struct.OutputStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author CaMnter
 */

public class DexMethodCounts extends DexCount {

    public DexMethodCounts(OutputStyle outputStyle) {
        super(outputStyle);
    }


    @Override
    public void generate(DexData dexData, boolean includeClasses, String packageFilter, int maxDepth, Filter filter) {
        final MethodInfo methodInfo = getMethodRefs(dexData, filter);
        final StringBuilder builder = methodInfo.builder;
        final MethodRef[] methodRefs = methodInfo.methodRefs;
        if (builder != null) this.builder.append(builder.toString());
        if (methodRefs == null) return;

        for (MethodRef methodRef : methodRefs) {
            final String classDescriptor = methodRef.getDeclClassName();
            final String packageName = includeClasses ?
                                       Output.descriptorToDot(classDescriptor).replace('$', '.') :
                                       Output.packageNameOnly(classDescriptor);
            if (packageFilter != null &&
                !packageName.startsWith(packageFilter)) {
                continue;
            }

            this.overallCount++;
            if (this.outputStyle == OutputStyle.TREE) {
                final String packageNamePieces[] = packageName.split("\\.");
                Node packageNode = this.packageTree;
                for (int i = 0; i < packageNamePieces.length && i < maxDepth; i++) {
                    packageNode.count++;
                    String name = packageNamePieces[i];
                    if (packageNode.children.containsKey(name)) {
                        packageNode = packageNode.children.get(name);
                    } else {
                        final Node childPackageNode = new Node();
                        if (name.length() == 0) {
                            // This method is declared in a class that is part of the default package.
                            // Typical examples are methods that operate on arrays of primitive data types.
                            name = "<default>";
                        }
                        packageNode.children.put(name, childPackageNode);
                        packageNode = childPackageNode;
                    }
                }
                packageNode.count++;
            } else if (this.outputStyle == OutputStyle.FLAT) {
                IntHolder count = this.packageCount.get(packageName);
                if (count == null) {
                    count = new IntHolder();
                    this.packageCount.put(packageName, count);
                }
                count.value++;
            }
        }
    }


    private static MethodInfo getMethodRefs(DexData dexData,
                                            Filter filter) {
        final MethodRef[] methodRefs = dexData.getMethodRefs();
        final StringBuilder builder = new StringBuilder();

        builder.append("Read in ")
            .append(methodRefs.length)
            .append(" method IDs.")
            .append("\n");
        if (filter == Filter.ALL) {
            return new MethodInfo(methodRefs, builder);
        }

        // 外部 class
        final ClassRef[] externalClassRefs = dexData.getExternalReferences();
        builder.append("Read in ")
            .append(externalClassRefs.length)
            .append(" external class references.")
            .append("\n");

        // 外部 method
        final Set<MethodRef> externalMethodRefs = new HashSet<MethodRef>();
        for (ClassRef classRef : externalClassRefs) {
            Collections.addAll(externalMethodRefs, classRef.getMethodArray());
        }
        builder.append("Read in ")
            .append(externalMethodRefs.size())
            .append(" external method references.")
            .append("\n");

        // 过滤 method
        final List<MethodRef> filteredMethodRefs = new ArrayList<MethodRef>();
        for (MethodRef methodRef : methodRefs) {
            boolean isExternal = externalMethodRefs.contains(methodRef);
            if ((filter == Filter.DEFINED_ONLY && !isExternal) ||
                (filter == Filter.REFERENCED_ONLY && isExternal)) {
                filteredMethodRefs.add(methodRef);
            }
        }
        builder.append("Filtered to ")
            .append(filteredMethodRefs.size())
            .append(" ")
            .append(filter == Filter.DEFINED_ONLY ? "defined" : "referenced")
            .append(" method IDs.")
            .append("\n");

        return new MethodInfo(
            filteredMethodRefs.toArray(new MethodRef[filteredMethodRefs.size()]),
            builder
        );

    }


    private static final class MethodInfo {

        private MethodRef[] methodRefs;
        private StringBuilder builder;


        public MethodInfo(MethodRef[] methodRefs,
                          StringBuilder builder) {
            this.methodRefs = methodRefs;
            this.builder = builder;
        }

    }

}
