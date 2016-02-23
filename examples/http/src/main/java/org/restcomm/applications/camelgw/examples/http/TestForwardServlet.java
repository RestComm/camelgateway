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
import org.mobicents.protocols.ss7.cap.api.CAPMessage;
import org.mobicents.protocols.ss7.cap.service.circuitSwitchedCall.ApplyChargingReportRequestImpl;
import org.mobicents.protocols.ss7.cap.service.circuitSwitchedCall.ContinueRequestImpl;
import org.mobicents.protocols.ss7.cap.service.circuitSwitchedCall.EventReportBCSMRequestImpl;
import org.mobicents.protocols.ss7.cap.service.circuitSwitchedCall.InitialDPRequestImpl;
import org.mobicents.protocols.ss7.indicator.RoutingIndicator;
import org.mobicents.protocols.ss7.sccp.impl.parameter.SccpAddressImpl;
import org.mobicents.protocols.ss7.sccp.parameter.SccpAddress;

public class TestForwardServlet extends HttpServlet {

	private static final Logger logger = Logger.getLogger(TestServlet.class);

	private EventsSerializeFactory factory = null;

	SccpAddress camelGwAddress = new SccpAddressImpl(RoutingIndicator.ROUTING_BASED_ON_DPC_AND_SSN, null, 1, 146);
	SccpAddress thirdPartyAddress = new SccpAddressImpl(RoutingIndicator.ROUTING_BASED_ON_DPC_AND_SSN, null, 3, 146);
	SccpAddress mnoAddress = new SccpAddressImpl(RoutingIndicator.ROUTING_BASED_ON_DPC_AND_SSN, null, 2, 146);

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

		XmlCAPDialog copy = new XmlCAPDialog(CAPApplicationContext.CapV1_gsmSSF_to_gsmSCF, camelGwAddress,
				thirdPartyAddress, 12l, 13l);

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

			FastList<CAPMessage> capMessages = original.getCAPMessages();

			for (FastList.Node<CAPMessage> n = capMessages.head(), end = capMessages.tail(); (n = n.getNext()) != end;) {

				CAPMessage capMessage = n.getValue();
				switch (capMessage.getMessageType()) {
//				case initialDP_Request:
//					InitialDPRequestImpl initialDPRequest = (InitialDPRequestImpl) capMessage;
//					this.handleIdp(original, initialDPRequest);
//					break;
//				case continue_Request:
//					ContinueRequestImpl continueRequest = (ContinueRequestImpl) capMessage;
//					this.handleContinueRequest(original, continueRequest);
//					break;
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

//	private void handleIdp(XmlCAPDialog original, InitialDPRequestImpl initialDPRequest) {
//		logger.info(String.format("Received initialDPRequest=%s", initialDPRequest));
//
//		original.setRedirectRequest(true);
//		original.setRemoteAddress(this.thirdPartyAddress);
//		original.setLocalAddress(this.camelGwAddress);
//	}
//
//	private void handleContinueRequest(XmlCAPDialog original, ContinueRequestImpl continueRequest) {
//		logger.info(String.format("Received ContinueRequest=%s", continueRequest));
//
//		original.setRedirectRequest(false);
//		original.setRemoteAddress(this.mnoAddress);
//		original.setLocalAddress(this.camelGwAddress);
//	}

}
