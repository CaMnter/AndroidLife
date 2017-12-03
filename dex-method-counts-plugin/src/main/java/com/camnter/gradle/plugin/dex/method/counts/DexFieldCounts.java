package com.camnter.gradle.plugin.dex.method.counts;

import com.android.dexdeps.ClassRef;
import com.android.dexdeps.DexData;
import com.android.dexdeps.FieldRef;
import com.android.dexdeps.Output;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author CaMnter
 */

public class DexFieldCounts extends DexCount {

    private final StringBuilder builder;


    DexFieldCounts(OutputStyle outputStyle) {
        super(outputStyle);
        this.builder = new StringBuilder();
    }


    @Override
    public void generate(DexData dexData,
                         boolean includeClasses,
                         String packageFilter,
                         int maxDepth,
                         Filter filter) {
        final FieldInfo fieldInfo = getFieldRef(dexData, filter);
        final FieldRef[] fieldRefs;
        if ((fieldRefs = fieldInfo.fieldRefs) == null) return;

        for (FieldRef fieldRef : fieldRefs) {
            final String classDescriptor = fieldRef.getDeclClassName();
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
                            // This field is declared in a class that is part of the default package.
                            name = "<default>";
                        }
                        packageNode.children.put(name, childPackageNode);
                        packageNode = childPackageNode;
                    }
                }
                packageNode.count++;
            }else {
                IntHolder count = this.packageCount.get(packageName);
                if (count == null) {
                    count = new IntHolder();
                    this.packageCount.put(packageName, count);
                }
                count.value++;
            }
        }
    }


    private static FieldInfo getFieldRef(DexData dexData, Filter filter) {
        final FieldRef[] fieldRefs = dexData.getFieldRefs();
        final StringBuilder builder = new StringBuilder();

        builder.append("Read in ")
            .append(fieldRefs.length)
            .append(" field IDs.")
            .append("\n");
        if (filter == Filter.ALL) {
            return new FieldInfo(fieldRefs, builder);
        }

        // 外部 class
        final ClassRef[] externalClassRefs = dexData.getExternalReferences();
        builder.append("Read in ")
            .append(externalClassRefs.length)
            .append(" external class references.")
            .append("\n");

        // 外部 field
        final Set<FieldRef> externalFieldRefs = new HashSet<>();
        for (ClassRef classRef : externalClassRefs) {
            Collections.addAll(externalFieldRefs, classRef.getFieldArray());
        }
        builder.append("Read in ")
            .append(externalFieldRefs.size())
            .append(" external field references.")
            .append("\n");

        // 过滤 field
        final List<FieldRef> filteredFieldRefs = new ArrayList<>();
        for (FieldRef FieldRef : fieldRefs) {
            boolean isExternal = externalFieldRefs.contains(FieldRef);
            if ((filter == Filter.DEFINED_ONLY && !isExternal) ||
                (filter == Filter.REFERENCED_ONLY && isExternal)) {
                filteredFieldRefs.add(FieldRef);
            }
        }

        builder.append("Filtered to ")
            .append(filteredFieldRefs.size())
            .append(" ")
            .append(filter == Filter.DEFINED_ONLY ? "defined" : "referenced")
            .append(" field IDs.")
            .append("\n");

        return new FieldInfo(
            filteredFieldRefs.toArray(new FieldRef[filteredFieldRefs.size()]),
            builder
        );
    }


    private static final class FieldInfo {

        private FieldRef[] fieldRefs;
        private StringBuilder builder;


        public FieldInfo(FieldRef[] fieldRefs,
                         StringBuilder builder) {
            this.fieldRefs = fieldRefs;
            this.builder = builder;
        }

    }

}
