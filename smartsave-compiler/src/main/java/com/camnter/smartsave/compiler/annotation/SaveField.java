package com.camnter.smartsave.compiler.annotation;

import com.camnter.smartsave.annotation.Save;
import javax.lang.model.element.Element;

/**
 * @author CaMnter
 */

public class SaveField extends ResourceAnnotationField<Save> {

    public SaveField(Element element, Class<Save> clazz) {
        super(element, clazz);
    }

}
