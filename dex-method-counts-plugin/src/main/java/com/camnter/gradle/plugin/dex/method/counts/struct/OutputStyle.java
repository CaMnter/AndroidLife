package com.camnter.gradle.plugin.dex.method.counts.struct;

import com.camnter.gradle.plugin.dex.method.counts.DexCount;
import java.util.Map;

/**
 * @author CaMnter
 */

public enum OutputStyle {

    TREE {
        @Override
        public void output(DexCount dexCount) {
            dexCount.getPackageTree().output("");
        }
    },
    FLAT {
        @Override
        public void output(DexCount counts) {
            for (Map.Entry<String, IntHolder> e : counts.getPackageCount().entrySet()) {
                String packageName = e.getKey();
                if (packageName == "") {
                    packageName = "<no package>";
                }
                System.out.printf("%6s %s\n", e.getValue().value, packageName);
            }
        }
    };


    public abstract void output(DexCount dexCount);

}
