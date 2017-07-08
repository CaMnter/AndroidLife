package com.camnter.smartsave.compiler.scanner;

import com.squareup.javapoet.ClassName;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeScanner;
import java.util.Map;

/**
 * R class 变量 扫描
 * 会保存在一个 Map 内
 *
 * @author CaMnter
 */

class VarScanner extends TreeScanner {

    private final Map<QualifiedId, Id> ids;
    private final ClassName className;
    private final String respectivePackageName;


    VarScanner(Map<QualifiedId, Id> ids, ClassName className,
               String respectivePackageName) {
        this.ids = ids;
        this.className = className;
        this.respectivePackageName = respectivePackageName;
    }


    @Override
    public void visitVarDef(JCTree.JCVariableDecl jcVariableDecl) {
        if ("int".equals(jcVariableDecl.getType().toString())) {
            int id = Integer.valueOf(jcVariableDecl.getInitializer().toString());
            String resourceName = jcVariableDecl.getName().toString();
            QualifiedId qualifiedId = new QualifiedId(this.respectivePackageName, id);
            this.ids.put(qualifiedId, new Id(id, this.className, resourceName));
        }
    }

}