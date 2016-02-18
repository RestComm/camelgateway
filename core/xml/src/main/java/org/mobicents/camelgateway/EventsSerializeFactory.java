/**
 * TeleStax, Open Source Cloud Communications  Copyright 2012. 
 * and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.restcomm.camelgateway;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javolution.xml.XMLBinding;
import javolution.xml.XMLObjectReader;
import javolution.xml.XMLObjectWriter;
import javolution.xml.stream.XMLStreamException;

import org.mobicents.protocols.ss7.cap.api.CAPDialog;
import org.mobicents.protocols.ss7.cap.errors.CAPErrorMessageCancelFailedImpl;
import org.mobicents.protocols.ss7.cap.errors.CAPErrorMessageParameterlessImpl;
import org.mobicents.protocols.ss7.cap.errors.CAPErrorMessageRequestedInfoErrorImpl;
import org.mobicents.protocols.ss7.cap.errors.CAPErrorMessageSystemFailureImpl;
import org.mobicents.protocols.ss7.cap.errors.CAPErrorMessageTaskRefusedImpl;
import org.mobicents.protocols.ss7.sccp.impl.parameter.GlobalTitle0001Impl;
import org.mobicents.protocols.ss7.sccp.impl.parameter.GlobalTitle0010Impl;
import org.mobicents.protocols.ss7.sccp.impl.parameter.GlobalTitle0011Impl;
import org.mobicents.protocols.ss7.sccp.impl.parameter.GlobalTitle0100Impl;
import org.mobicents.protocols.ss7.sccp.parameter.GlobalTitle0001;
import org.mobicents.protocols.ss7.sccp.parameter.GlobalTitle0010;
import org.mobicents.protocols.ss7.sccp.parameter.GlobalTitle0011;
import org.mobicents.protocols.ss7.sccp.parameter.GlobalTitle0100;

/**
 * <p>
 * Factory Object used to serialize/de-serialize the {@link CAPDialog} objects
 * </p>
 * 
 * @author amit bhayani
 * 
 */
public class EventsSerializeFactory {

	private static final String DIALOG = "dialog";
	private static final String TYPE = "type";
	private static final String TAB = "\t";

	final XMLBinding binding = new XMLBinding();

	public EventsSerializeFactory() {
        // CAPErrorMessage classes
        binding.setAlias(CAPErrorMessageCancelFailedImpl.class, ErrorComponentCap.CAP_ERROR_MESSAGE_CANCEL_FAILED);
        binding.setAlias(CAPErrorMessageParameterlessImpl.class, ErrorComponentCap.CAP_ERROR_MESSAGE_PARAMETERLESS);
        binding.setAlias(CAPErrorMessageRequestedInfoErrorImpl.class, ErrorComponentCap.CAP_ERROR_MESSAGE_REQUESTED_INFO_ERROR);
        binding.setAlias(CAPErrorMessageSystemFailureImpl.class, ErrorComponentCap.CAP_ERROR_MESSAGE_SYSTEM_FAILURE);
        binding.setAlias(CAPErrorMessageTaskRefusedImpl.class, ErrorComponentCap.CAP_ERROR_MESSAGE_TASK_REFUSED);

		//SCCP Gt classes
		binding.setAlias(GlobalTitle0001Impl.class, GlobalTitle0001.class.getSimpleName());
		binding.setAlias(GlobalTitle0010Impl.class, GlobalTitle0010.class.getSimpleName());
		binding.setAlias(GlobalTitle0011Impl.class, GlobalTitle0011.class.getSimpleName());
		binding.setAlias(GlobalTitle0100Impl.class, GlobalTitle0100.class.getSimpleName());
				
		binding.setAlias(XmlCAPDialog.class, DIALOG);
		binding.setClassAttribute(TYPE);
	}

	/**
	 * Serialize passed {@link CAPDialog} object
	 * 
	 * @param dialog
	 * @return serialized byte array
	 * @throws XMLStreamException
	 *             Exception if serialization fails
	 */
	public byte[] serialize(XmlCAPDialog dialog) throws XMLStreamException {

		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final XMLObjectWriter writer = XMLObjectWriter.newInstance(baos);

		try {

			writer.setBinding(binding);
			writer.setIndentation(TAB);

			writer.write(dialog, DIALOG, XmlCAPDialog.class);
			writer.flush();
			byte[] data = baos.toByteArray();

			return data;
		} finally {
			writer.close();
		}
	}

	/**
	 * De-serialize the byte[] into {@link CAPDialog} object
	 * 
	 * @param data
	 * @return de-serialized Dialog Object
	 * @throws XMLStreamException
	 *             Exception if de-serialization fails
	 */
	public XmlCAPDialog deserialize(byte[] data) throws XMLStreamException {
		final ByteArrayInputStream bais = new ByteArrayInputStream(data);
		final XMLObjectReader reader = XMLObjectReader.newInstance(bais);
		try {
			reader.setBinding(binding);
			XmlCAPDialog dialog = reader.read(DIALOG, XmlCAPDialog.class);
			return dialog;
		} finally {
			reader.close();
		}
	}

	/**
	 * De-serialize passed {@link InputStream} into {@link CAPDialog} object
	 * 
	 * @param is
	 * @return de-serialized Dialog Object
	 * @throws XMLStreamException
	 *             Exception if de-serialization fails
	 */
	public XmlCAPDialog deserialize(InputStream is) throws XMLStreamException {
		final XMLObjectReader reader = XMLObjectReader.newInstance(is);
		try {
			reader.setBinding(binding);
			XmlCAPDialog dialog = reader.read(DIALOG, XmlCAPDialog.class);
			return dialog;
		} finally {
			reader.close();
		}
	}
}
