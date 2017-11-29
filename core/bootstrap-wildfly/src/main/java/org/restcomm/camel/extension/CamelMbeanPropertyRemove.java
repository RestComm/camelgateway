package org.restcomm.camel.extension;

import org.jboss.as.controller.AbstractRemoveStepHandler;

class CamelMbeanPropertyRemove extends AbstractRemoveStepHandler {

    public static final CamelMbeanPropertyRemove INSTANCE = new CamelMbeanPropertyRemove();

    private CamelMbeanPropertyRemove() {
    }
}