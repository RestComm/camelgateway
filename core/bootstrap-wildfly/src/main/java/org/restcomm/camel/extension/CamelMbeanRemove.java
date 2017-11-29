package org.restcomm.camel.extension;

import org.jboss.as.controller.AbstractRemoveStepHandler;

class CamelMbeanRemove extends AbstractRemoveStepHandler {

    static final CamelMbeanRemove INSTANCE = new CamelMbeanRemove();

    private CamelMbeanRemove() {
    }
}