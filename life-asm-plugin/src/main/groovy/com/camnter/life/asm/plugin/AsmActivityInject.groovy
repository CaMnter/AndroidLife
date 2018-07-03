package com.camnter.life.asm.plugin

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.JarInput
import org.apache.commons.codec.digest.DigestUtils
import org.gradle.api.Project

/**
 * @author CaMnter
 */

class AsmActivityInject extends BaseInject {

    private static final String TAG = AsmActivityInject.simpleName

    AsmActivityInject(Project project) {
        super(project)
    }

    @Override
    def inject(DirectoryInput directoryInput) {
        if (!checkoutAppExtension()) return
        def dirFile = directoryInput.file
        if (dirFile.isDirectory()) {
            dirFile.eachFileRecurse {
                def filePath = it.absolutePath
                if (it.name == 'AsmActivity.class') {
                    // TODO
                }
            }
        }
    }

    @Override
    def inject(JarInput jarInput) {
        def md5Path = DigestUtils.md5Hex(jarInput.file.absolutePath)
        def md5Name = jarInput.name
        if (md5Name.endsWith(".jar")) {
            md5Name = md5Name.substring(0, md5Name.length() - 4)
            md5Name = md5Name + md5Path
        }
        return md5Name
    }
}