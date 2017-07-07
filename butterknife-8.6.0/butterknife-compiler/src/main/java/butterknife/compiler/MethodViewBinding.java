package butterknife.compiler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 方法相关注解的 ViewBinding
 */
final class MethodViewBinding implements MemberViewBinding {
    private final String name;
    private final List<Parameter> parameters;
    private final boolean required;


    MethodViewBinding(String name, List<Parameter> parameters, boolean required) {
        this.name = name;
        this.parameters = Collections.unmodifiableList(new ArrayList<>(parameters));
        this.required = required;
    }


    public String getName() {
        return name;
    }


    public List<Parameter> getParameters() {
        return parameters;
    }


    @Override
    public String getDescription() {
        return "method '" + name + "'";
    }


    public boolean isRequired() {
        return required;
    }
}
