import org.gradle.api.Plugin
import org.gradle.api.Project

class LifePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.task('LifeTask') << {
            println "[LifePlugin]   [println]   Save you from anything"
        }
    }

}