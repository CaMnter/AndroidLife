import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 *  Description：Register
 *  Created by：CaMnter
 */
public class Register implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.logger.error "================ 自定义插件成功！================"
    }
}