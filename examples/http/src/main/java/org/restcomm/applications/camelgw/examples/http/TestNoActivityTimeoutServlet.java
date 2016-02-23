package org.restcomm.applications.camelgw.examples.http;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import javolution.util.FastList;
import javolution.xml.stream.XMLStreamException;

import org.apache.log4j.Logger;
import org.restcomm.camelgateway.EventsSerializeFactory;
import org.restcomm.camelgateway.XmlCAPDialog;
import org.mobicents.protocols.ss7.cap.api.CAPApplicationContext;
import org.mobicents.protocols.ss7.cap.api.CAPException;
import org.mobicents.protocols.ss7.cap.api.CAPMessage;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.ContinueRequest;
import org.mobicents.protocols.ss7.cap.service.circuitSwitchedCall.ApplyChargingReportRequestImpl;
import org.mobicents.protocols.ss7.cap.service.circuitSwitchedCall.ContinueRequestImpl;
import org.mobicents.protocols.ss7.cap.service.circuitSwitchedCall.EventReportBCSMRequestImpl;
import org.mobicents.protocols.ss7.cap.service.circuitSwitchedCall.InitialDPRequestImpl;
import org.mobicents.protocols.ss7.indicator.RoutingIndicator;
import org.mobicents.protocols.ss7.sccp.impl.parameter.SccpAddressImpl;
import org.mobicents.protocols.ss7.sccp.parameter.SccpAddress;

/**
 * 
 * @author amit bhayani
 * 
 */
public class TestNoActivityTimeoutServlet extends HttpServlet {

	private static final Logger logger = Logger.getLogger(TestNoActivityTimeoutServlet.class);

	private EventsSerializeFactory factory = null;

	@Override
	public void init() {
		factory = new EventsSerializeFactory();
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		PrintWriter out = response.getWriter();
		out.println("<html>");
		out.println("<body>");
		out.println("<h1>CAMEL Gw Demo Get</h1>");
		SccpAddress orgAddress = new SccpAddressImpl(RoutingIndicator.ROUTING_BASED_ON_DPC_AND_SSN, null, 1, 8);
		SccpAddress dstAddress = new SccpAddressImpl(RoutingIndicator.ROUTING_BASED_ON_DPC_AND_SSN, null, 2, 8);

		XmlCAPDialog copy = new XmlCAPDialog(CAPApplicationContext.CapV1_gsmSSF_to_gsmSCF, orgAddress, dstAddress, 12l,
				13l);

		try {
			byte[] data = factory.serialize(copy);
			System.out.println(Arrays.toString(data));
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}

		out.println("</body>");
		out.println("</html>");
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		ServletInputStream is = request.getInputStream();
		try {
			XmlCAPDialog original = factory.deserialize(is);
			HttpSession session = request.getSession(true);
			if (logger.isInfoEnabled()) {
				logger.info("doPost. HttpSession=" + session.getId() + " Dialog = " + original);
			}
			
			if((original.getNoActivityTimeout() != null) && (original.getNoActivityTimeout()) ){
				try {
					original.close(true);
					this.sendResponse(response, original);
				} catch (CAPException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return;
			}

			FastList<CAPMessage> capMessages = original.getCAPMessages();

			for (FastList.Node<CAPMessage> n = capMessages.head(), end = capMessages.tail(); (n = n.getNext()) != end;) {

				CAPMessage capMessage = n.getValue();
				switch (capMessage.getMessageType()) {
				case initialDP_Request:
					InitialDPRequestImpl initialDPRequest = (InitialDPRequestImpl) capMessage;
					this.handleIdp(original, initialDPRequest);
					break;
				case applyChargingReport_Request:
					ApplyChargingReportRequestImpl applyChargingReportRequest = (ApplyChargingReportRequestImpl) capMessage;
					logger.info(String.format("Received applyChargingReportRequest=%s", applyChargingReportRequest));
					break;
				case eventReportBCSM_Request:
					EventReportBCSMRequestImpl eventReportBCSMRequest = (EventReportBCSMRequestImpl) capMessage;
					logger.info(String.format("Received eventReportBCSMRequest=%s", eventReportBCSMRequest));
					break;
				default:
					logger.info(String.format("unrecognized capMessage=%s", capMessage));
					break;

				}
			}

			// send response
			this.sendResponse(response, original);
		} catch (XMLStreamException e) {
			logger.error("Error while processing received XML", e);
		}

	}

	private void sendResponse(HttpServletResponse response, XmlCAPDialog original) {
		byte[] data;
		try {
			data = factory.serialize(original);
			response.getOutputStream().write(data);
			response.flushBuffer();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void handleIdp(XmlCAPDialog original, InitialDPRequestImpl initialDPRequest) {
		//try {
			logger.info(String.format("Received initialDPRequest=%s", initialDPRequest));

			// Lets send back CON and end Dialog
			original.getCAPMessages().clear();

			ContinueRequest cue = new ContinueRequestImpl();
			original.addCAPMessage(cue);

			//original.close(false);
//		} catch (CAPException e) {
//			e.printStackTrace();
//		}
	}
}
