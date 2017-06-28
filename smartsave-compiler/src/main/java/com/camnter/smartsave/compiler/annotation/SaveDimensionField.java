package com.camnter.smartsave.compiler.annotation;

import com.camnter.smartsave.annotation.SaveDimension;
import javax.lang.model.element.Element;

/**
 * @author CaMnter
 */

public class SaveDimensionField extends ResourceAnnotationField<SaveDimension> {

    public SaveDimensionField(Element element, Class<SaveDimension> clazz) {
        super(element, clazz);
    }

}
