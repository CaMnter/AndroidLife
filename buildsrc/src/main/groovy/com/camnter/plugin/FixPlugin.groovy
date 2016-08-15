import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 *  Description：FixPlugin
 *  Created by：CaMnter
 *  */
public class FixPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.logger.error "================ 自定义插件成功！================"
        def android = project.extensions.findByType(AppExtension)
        android.registerTransform(new PreDexTransform(project))
    }
}