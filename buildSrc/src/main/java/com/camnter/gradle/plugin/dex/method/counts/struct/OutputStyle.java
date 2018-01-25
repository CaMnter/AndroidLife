package com.camnter.gradle.plugin.dex.method.counts.struct;

import com.camnter.gradle.plugin.dex.method.counts.DexCount;
import java.util.Map;
import java.util.Objects;

/**
 * @author CaMnter
 */

public enum OutputStyle {

    TREE {
        @Override
        public StringBuilder output(DexCount dexCount) {
           return dexCount.getPackageTree().output("");
        }
    },
    FLAT {
        @Override
        public StringBuilder output(DexCount counts) {
            StringBuilder stringBuilder = new StringBuilder();
            for (Map.Entry<String, IntHolder> e : counts.getPackageCount().entrySet()) {
                String packageName = e.getKey();
                if (Objects.equals(packageName, "")) {
                    packageName = "<no package>";
                }
                stringBuilder.append(String.format("%6s %s\n", e.getValue().value, packageName));
            }
            return  stringBuilder;
        }
    };


    public abstract StringBuilder output(DexCount dexCount);

}
