package com.camnter.smartsave.compiler.scanner;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeScanner;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * R class 扫描
 *
 * 因为每个 package name 都会有一个 R
 *
 * @author CaMnter
 */

public class RClassScanner extends TreeScanner {

    // Maps the currently evaulated rPackageName to R Classes
    private final Map<String, Set<String>> rClasses = new LinkedHashMap<>();
    private String currentPackageName;


    @Override
    public void visitSelect(JCTree.JCFieldAccess jcFieldAccess) {
        Symbol symbol = jcFieldAccess.sym;
        if (symbol != null
            && symbol.getEnclosingElement() != null
            && symbol.getEnclosingElement().getEnclosingElement() != null
            && symbol.getEnclosingElement().getEnclosingElement().enclClass() != null) {
            Set<String> rClassSet = this.rClasses.get(this.currentPackageName);
            if (rClassSet == null) {
                rClassSet = new HashSet<>();
                this.rClasses.put(this.currentPackageName, rClassSet);
            }
            rClassSet.add(
                symbol.getEnclosingElement().getEnclosingElement().enclClass().className());
        }
    }


    Map<String, Set<String>> getRClasses() {
        return this.rClasses;
    }


    void setCurrentPackageName(String respectivePackageName) {
        this.currentPackageName = respectivePackageName;
    }

}
