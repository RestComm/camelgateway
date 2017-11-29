package org.restcomm.camel.extension;

import static org.restcomm.camel.extension.CamelMbeanPropertyDefinition.PROPERTY_ATTRIBUTES;

import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.dmr.ModelNode;

class CamelMbeanPropertyAdd extends AbstractAddStepHandler {

    public static final CamelMbeanPropertyAdd INSTANCE = new CamelMbeanPropertyAdd();

    private CamelMbeanPropertyAdd() {
    }

    @Override
    protected void populateModel(final ModelNode operation, final ModelNode model) throws OperationFailedException {
        CamelMbeanPropertyDefinition.NAME_ATTR.validateAndSet(operation, model);
        for (SimpleAttributeDefinition def : PROPERTY_ATTRIBUTES) {
            def.validateAndSet(operation, model);
        }
    }
}