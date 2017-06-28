package com.camnter.smartsave.compiler.annotation;

import com.camnter.smartsave.annotation.SaveColor;
import javax.lang.model.element.Element;

/**
 * @author CaMnter
 */

public class SaveColorField extends ResourceAnnotationField<SaveColor> {

    public SaveColorField(Element element, Class<SaveColor> clazz) {
        super(element, clazz);
    }

}
