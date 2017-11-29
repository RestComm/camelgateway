package org.restcomm.camel.extension;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.parsing.Attribute;
import org.jboss.as.controller.parsing.ParseUtils;
import org.jboss.as.controller.persistence.SubsystemMarshallingContext;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.jboss.staxmapper.XMLElementReader;
import org.jboss.staxmapper.XMLElementWriter;
import org.jboss.staxmapper.XMLExtendedStreamReader;
import org.jboss.staxmapper.XMLExtendedStreamWriter;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import java.util.Collections;
import java.util.List;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;
import static org.jboss.as.controller.parsing.ParseUtils.*;

/**
/**
 * The subsystem parser, which uses stax to read and write to and from xml
 */
class CamelSubsystemParser implements XMLStreamConstants, XMLElementReader<List<ModelNode>>,
        XMLElementWriter<SubsystemMarshallingContext> {

    private static final CamelSubsystemParser INSTANCE = new CamelSubsystemParser();

    static CamelSubsystemParser getInstance() {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeContent(XMLExtendedStreamWriter writer, SubsystemMarshallingContext context) throws XMLStreamException {
        context.startSubsystemElement(Namespace.CURRENT.getUriString(), false);

        final ModelNode node = context.getModelNode();
        final ModelNode mbean = node.get(CamelMbeanDefinition.MBEAN);

        for (Property mbeanProp : mbean.asPropertyList()) {
            writer.writeStartElement(CamelMbeanDefinition.MBEAN);

            final ModelNode mbeanEntry = mbeanProp.getValue();

            CamelMbeanDefinition.NAME_ATTR.marshallAsAttribute(mbeanEntry, true, writer);
            CamelMbeanDefinition.TYPE_ATTR.marshallAsAttribute(mbeanEntry, true, writer);

            final ModelNode property = mbeanEntry.get(CamelMbeanPropertyDefinition.PROPERTY);
            if (property != null && property.isDefined()) {
                for (Property propertyProp : property.asPropertyList()) {
                    writer.writeStartElement(CamelMbeanPropertyDefinition.PROPERTY);

                    final ModelNode propertyEntry = propertyProp.getValue();

                    CamelMbeanPropertyDefinition.NAME_ATTR.marshallAsAttribute(propertyEntry, true, writer);
                    CamelMbeanPropertyDefinition.TYPE_ATTR.marshallAsAttribute(propertyEntry, true, writer);
                    CamelMbeanPropertyDefinition.VALUE_ATTR.marshallAsAttribute(propertyEntry, true, writer);

                    writer.writeEndElement();
                }
            }

            writer.writeEndElement();
        }

        writer.writeEndElement();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void readElement(XMLExtendedStreamReader reader, List<ModelNode> list) throws XMLStreamException {
        PathAddress address = PathAddress.pathAddress(PathElement.pathElement(SUBSYSTEM, CamelExtension.SUBSYSTEM_NAME));

        final ModelNode subsystem = new ModelNode();
        subsystem.get(OP).set(ADD);
        subsystem.get(OP_ADDR).set(address.toModelNode());
        list.add(subsystem);

        // mbean elements
        while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
            switch (Namespace.forUri(reader.getNamespaceURI())) {
                case CAMELEXT_1_0: {
                    final String tagName = reader.getLocalName();
                    if (tagName.equals(CamelMbeanDefinition.MBEAN)) {
                        parseMbean(reader, address, list);
                    }
                    break;
                }
                default: {
                    throw unexpectedElement(reader);
                }
            }
        }
    }

    static void parseMbean(XMLExtendedStreamReader reader, PathAddress parent, List<ModelNode> list)
            throws XMLStreamException {
        String name = null;
        final ModelNode mbean = new ModelNode();

        // MBean Attributes
        final int count = reader.getAttributeCount();
        for (int i = 0; i < count; i++) {
            requireNoNamespaceAttribute(reader, i);
            final String attribute = reader.getAttributeLocalName(i);
            final String value = reader.getAttributeValue(i);
            switch (CamelMbeanDefinition.Element.of(attribute)) {
                case NAME: {
                    name = value;
                    CamelMbeanDefinition.NAME_ATTR.parseAndSetParameter(value, mbean, reader);
                    break;
                }
                case TYPE: {
                    CamelMbeanDefinition.TYPE_ATTR.parseAndSetParameter(value, mbean, reader);
                    break;
                }
                default: {
                    throw unexpectedAttribute(reader, i);
                }
            }
        }

        //ParseUtils.requireNoContent(reader);

        if (name == null) {
            throw missingRequired(reader, Collections.singleton(Attribute.NAME));
        }

        mbean.get(OP).set(ADD);
        PathAddress address = PathAddress.pathAddress(parent,
                PathElement.pathElement(CamelMbeanDefinition.MBEAN, name));
        mbean.get(OP_ADDR).set(address.toModelNode());
        list.add(mbean);

        // properties elements
        while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
            switch (Namespace.forUri(reader.getNamespaceURI())) {
                case CAMELEXT_1_0: {
                    final String tagName = reader.getLocalName();
                    switch (tagName) {
                        case CamelMbeanPropertyDefinition.PROPERTY: {
                            parseProperty(reader, address, list);
                            break;
                        }
                        default: {
                            break;
                        }
                    }
                    break;
                }
                default: {
                    throw unexpectedElement(reader);
                }
            }
        }
    }

    static void parseProperty(XMLExtendedStreamReader reader, PathAddress parent, List<ModelNode> list)
            throws XMLStreamException {
        String name = null;
        final ModelNode property = new ModelNode();

        final int count = reader.getAttributeCount();
        for (int i = 0; i < count; i++) {
            requireNoNamespaceAttribute(reader, i);
            final String attribute = reader.getAttributeLocalName(i);
            final String value = reader.getAttributeValue(i);
            switch (CamelMbeanPropertyDefinition.Element.of(attribute)) {
                case NAME: {
                    name = value;
                    CamelMbeanPropertyDefinition.NAME_ATTR.parseAndSetParameter(value, property, reader);
                    break;
                }
                case TYPE: {
                    CamelMbeanPropertyDefinition.TYPE_ATTR.parseAndSetParameter(value, property, reader);
                    break;
                }
                case VALUE: {
                    CamelMbeanPropertyDefinition.VALUE_ATTR.parseAndSetParameter(value, property, reader);
                    break;
                }
                default: {
                    throw unexpectedAttribute(reader, i);
                }
            }
        }

        ParseUtils.requireNoContent(reader);

        if (name == null) {
            throw missingRequired(reader, Collections.singleton(Attribute.NAME));
        }

        property.get(OP).set(ADD);
        PathAddress address = PathAddress.pathAddress(parent,
                PathElement.pathElement(CamelMbeanPropertyDefinition.PROPERTY, name));
        property.get(OP_ADDR).set(address.toModelNode());
        list.add(property);
    }
}