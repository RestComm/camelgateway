/*
 * TeleStax, Open Source Cloud Communications  
 * Copyright 2012, Telestax Inc and individual contributors
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
package org.restcomm.camelgateway.slee;

import java.io.ByteArrayInputStream;
import java.util.Iterator;
import java.util.Map;

import javax.slee.ActivityContextInterface;
import javax.slee.Sbb;
import javax.slee.facilities.TimerEvent;
import javax.slee.facilities.TimerID;
import javax.slee.facilities.TimerOptions;

import javolution.util.FastList;
import net.java.client.slee.resource.http.HttpClientActivity;

import org.apache.http.Header;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHeader;
import org.restcomm.camelgateway.CamelPropertiesManagement;
import org.restcomm.camelgateway.EventsSerializeFactory;
import org.restcomm.camelgateway.NetworkRoutingRuleManagement;
import org.restcomm.camelgateway.XmlCAPDialog;
import org.restcomm.camelgateway.rules.NetworkRoutingRule;
import org.mobicents.protocols.ss7.cap.api.CAPDialog;
import org.mobicents.protocols.ss7.cap.api.CAPException;
import org.mobicents.protocols.ss7.cap.api.dialog.CAPDialogState;
import org.mobicents.protocols.ss7.cap.api.dialog.CAPUserAbortReason;
import org.mobicents.slee.resource.cap.events.CAPEvent;

/**
 * 
 * @author Amit Bhayani
 * @author sergey vetyutnev
 * 
 */
public abstract class CamelGatewaySbb extends CamelBaseSbb implements Sbb /*, CamelGateway */ {

	private static final String REDIRECTER = "REDIRECTER";

	private static final String CONTENT_TYPE = "text";
	private static final String CONTENT_SUB_TYPE = "xml";

	private static final String ACCEPTED_CONTENT_TYPE = CONTENT_TYPE + "/" + CONTENT_SUB_TYPE;

	private static final String CONTENT_ENCODING = "utf-8";

	private static final CamelPropertiesManagement camlePropertiesManagement = CamelPropertiesManagement.getInstance();
	private static final NetworkRoutingRuleManagement networkRoutingRuleManagement = NetworkRoutingRuleManagement
			.getInstance();
	

	/**
	 * SbbLocalObject method invocation
	 * 
	 */

//	public void processContinueRequestFrom3rdParty(XmlCAPDialog xmlCAPDialog3rdParty) throws Exception {
//		try {
//			byte[] data = this.getEventsSerializeFactory().serialize(xmlCAPDialog3rdParty);
//			this.sendXmlPayload(data);
//		} catch (Exception e) {
//			logger.severe("Exception while processing ContinueRequest received from 3rd party", e);
//			// TODO Abort DIalog?
//		}
//	}
//
//	public void processConnectRequestFrom3rdParty(XmlCAPDialog xmlCAPDialog3rdParty) throws Exception {
//		try {
//			byte[] data = this.getEventsSerializeFactory().serialize(xmlCAPDialog3rdParty);
//			this.sendXmlPayload(data);
//		} catch (Exception e) {
//			logger.severe("Exception while processing ConnectRequest received from 3rd party", e);
//			// TODO Abort DIalog?
//		}
//	}
//
//	public void processApplyChargingRequestFrom3rdParty(ApplyChargingRequest applyChargingRequest) throws Exception {
//
//		// Sending ACR directly to MNO
//		CAPDialogCircuitSwitchedCall capDialogCircuitSwitchedCall = (CAPDialogCircuitSwitchedCall) this.getCAPDialog();
//		capDialogCircuitSwitchedCall.addApplyChargingRequest(
//				applyChargingRequest.getAChBillingChargingCharacteristics(), applyChargingRequest.getPartyToCharge(),
//				applyChargingRequest.getExtensions(), applyChargingRequest.getAChChargingAddress());
//
//		capDialogCircuitSwitchedCall.send();
//
//	}
//
//	public void processReleaseCallRequestFrom3rdParty(ReleaseCallRequest releaseCallRequest) throws Exception {
//		// Sending RC directly to MNO
//		CAPDialogCircuitSwitchedCall capDialogCircuitSwitchedCall = (CAPDialogCircuitSwitchedCall) this.getCAPDialog();
//		capDialogCircuitSwitchedCall.addReleaseCallRequest(releaseCallRequest.getCause());
//		capDialogCircuitSwitchedCall.close(false);
//
//		this.endHttpClientActivity();
//
//	}
//
//	public void processDialogUserAbortFrom3rdParty(DialogUserAbort dialogUserAbort) throws Exception {
//		CAPDialog capDialog = this.getCAPDialog();
//		capDialog.abort(dialogUserAbort.getUserReason());
//
//		this.endHttpClientActivity();
//	}
//
//	public void processDialogProviderAbortFrom3rdParty(DialogProviderAbort dialogProviderAbort) throws Exception {
//		CAPDialog capDialog = this.getCAPDialog();
//		capDialog.abort(CAPUserAbortReason.no_reason_given);
//
//		this.endHttpClientActivity();
//	}

	/**
	 * Events Handlers
	 */

    public void onDIALOG_DELIMITER(org.mobicents.slee.resource.cap.events.DialogDelimiter event, ActivityContextInterface aci) {
        XmlCAPDialog dialog = this.getXmlCAPDialog();
        dialog.setTCAPMessageType(event.getCAPDialog().getTCAPMessageType());

        this.resetTimer(aci);
        
        try {
            byte[] data = this.getEventsSerializeFactory().serialize(dialog);
            this.sendXmlPayload(data);
        } catch (Exception e) {
            logger.severe(String.format("Exception while sending onDIALOG_DELIMITER to an application", event), e);
            // TODO Abort DIalog?
        }
        dialog.reset();
        this.setXmlCAPDialog(dialog);
    }

	public void onDIALOG_REQUEST(org.mobicents.slee.resource.cap.events.DialogRequest event,
			ActivityContextInterface aci/* , EventContext eventContext */) {

		CAPDialog capDialog = event.getCAPDialog();
		XmlCAPDialog dialog = new XmlCAPDialog(capDialog.getApplicationContext(), capDialog.getLocalAddress(),
				capDialog.getRemoteAddress(), capDialog.getLocalDialogId(), capDialog.getRemoteDialogId());
		int networkId = capDialog.getNetworkId();
		dialog.setNetworkId(networkId);
		this.setXmlCAPDialog(dialog);

		NetworkRoutingRule networkRoutingRule = networkRoutingRuleManagement.getNetworkRoutingRule(networkId);

		if (networkRoutingRule == null) {
			String route = camlePropertiesManagement.getRoute();
			if (logger.isFineEnabled()) {
				logger.fine("No NetworkRoutingRule configured for network-id " + networkId + " Using the default one "
						+ route);
			}
			networkRoutingRule = new NetworkRoutingRule();
			networkRoutingRule.setRuleUrl(route);
		}

		this.setNetworkRoutingRule(networkRoutingRule);

		if (networkRoutingRule.getRuleUrl() == null) {
			logger.warning("No routing rule defined for networkId " + networkId
					+ " Disconnecting from ACI, no new messages will be processed");
			aci.detach(this.sbbContext.getSbbLocalObject());

		}

        camelStatAggregator.updateDialogsAllEstablished();
        this.setStartDialogTime(System.currentTimeMillis());
	}

	public void onDIALOG_ACCEPT(org.mobicents.slee.resource.cap.events.DialogAccept event, ActivityContextInterface aci) {
	}

	public void onDIALOG_USERABORT(org.mobicents.slee.resource.cap.events.DialogUserAbort event,
			ActivityContextInterface aci) {
		logger.warning(String.format("onDIALOG_USERABORT for Dialog=%s", event.getCAPDialog()));

//		if (this.getRedirect()) {
//			// If its redirect, send to 3rd party directly
//			try {
//				CamelRedirectSbbLocalObject camelRedirectSbbLocalObject = this.getCamelRedirectSbbLocalObject();
//				camelRedirectSbbLocalObject.processDialogUserAbortFromMno(event);
//			} catch (Exception e) {
//				logger.severe("Error while calling processDialogUserAbortFromMno on Child Sbb", e);
//				// TODO Handle the error condition
//            }
//        } else {

        XmlCAPDialog dialog = this.getXmlCAPDialog();
        dialog.reset();
        try {
            dialog.abort(event.getUserReason());
        } catch (CAPException e1) {
            // This can not occur
            e1.printStackTrace();
        }
        dialog.setTCAPMessageType(event.getCAPDialog().getTCAPMessageType());

        try {
            byte[] data = this.getEventsSerializeFactory().serialize(dialog);
            this.sendXmlPayload(data);
        } catch (Exception e) {
            logger.severe(String.format("Exception while sending onDIALOG_USERABORT to an application", event), e);
            this.endHttpClientActivity();
        }

//        }

        this.setCapDialogClosed(true);

//        this.endHttpClientActivity();
	}

	public void onDIALOG_PROVIDERABORT(org.mobicents.slee.resource.cap.events.DialogProviderAbort event,
			ActivityContextInterface aci) {
		logger.warning(String.format("onDIALOG_PROVIDERABORT for Dialog=%s", event.getCAPDialog()));

//		if (this.getRedirect()) {
//			// If its redirect, send to 3rd party directly
//			try {
//				CamelRedirectSbbLocalObject camelRedirectSbbLocalObject = this.getCamelRedirectSbbLocalObject();
//				camelRedirectSbbLocalObject.processDialogProviderAbortFromMno(event);
//			} catch (Exception e) {
//				logger.severe("Error while calling processDialogProviderAbortFromMno on Child Sbb", e);
//				// TODO Handle the error condition
//			}
//        } else {
        XmlCAPDialog dialog = this.getXmlCAPDialog();
        dialog.reset();
        dialog.setPAbortCauseType(event.getPAbortCauseType());
        dialog.setTCAPMessageType(event.getCAPDialog().getTCAPMessageType());

        try {
            byte[] data = this.getEventsSerializeFactory().serialize(dialog);
            this.sendXmlPayload(data);
        } catch (Exception e) {
            logger.severe(String.format("Exception while sending onDIALOG_PROVIDERABORT to an application", event), e);
            this.endHttpClientActivity();
        }
//		}

        this.setCapDialogClosed(true);

//		this.endHttpClientActivity();
	}

	public void onDIALOG_CLOSE(org.mobicents.slee.resource.cap.events.DialogClose event, ActivityContextInterface aci) {
        XmlCAPDialog dialog = this.getXmlCAPDialog();
        dialog.setTCAPMessageType(event.getCAPDialog().getTCAPMessageType());

        try {
            byte[] data = this.getEventsSerializeFactory().serialize(dialog);
            this.sendXmlPayload(data);
        } catch (Exception e) {
            logger.severe(String.format("Exception while sending onDIALOG_CLOSE to an application", event), e);
            this.endHttpClientActivity();
            return;
        }
        dialog.reset();
        this.setXmlCAPDialog(dialog);

        this.setCapDialogClosed(true);

        this.updateDialogTime();

//        this.endHttpClientActivity();
	}

	public void onDIALOG_NOTICE(org.mobicents.slee.resource.cap.events.DialogNotice event, ActivityContextInterface aci) {
		logger.warning(String.format("onDIALOG_NOTICE for Dialog=%s", event.getCAPDialog()));
	}

	public void onDIALOG_TIMEOUT(org.mobicents.slee.resource.cap.events.DialogTimeout event,
			ActivityContextInterface aci) {
        if (logger.isFineEnabled()) {
            logger.fine(String.format("onDIALOG_TIMEOUT for Dialog=%s Calling keepAlive", event.getCAPDialog()));
        }

		event.getCAPDialog().keepAlive();
	}

	public void onDIALOG_RELEASE(org.mobicents.slee.resource.cap.events.DialogRelease event,
			ActivityContextInterface aci) {
		//just to be safe
		this.cancelTimer();
	}

	public void onINITIAL_DP_REQUEST(
			org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.InitialDPRequest event,
			ActivityContextInterface aci) {
		XmlCAPDialog dialog = this.getXmlCAPDialog();
		dialog.addCAPMessage(((CAPEvent) event).getWrappedEvent());
		this.setXmlCAPDialog(dialog);

        camelStatAggregator.updateMessagesRecieved();
        camelStatAggregator.updateMessagesAll();
	}

	public void onACTIVITY_TEST_REQUEST(
			org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.ActivityTestRequest event,
			ActivityContextInterface aci) {
	}

	public void onACTIVITY_TEST_RESPONSE(
			org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.ActivityTestResponse event,
			ActivityContextInterface aci) {
	}

	public void onAPPLY_CHARGING_REPORT_REQUEST(
			org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.ApplyChargingReportRequest event,
			ActivityContextInterface aci) {

//		if (this.getRedirect()) {
//			// If its redirect, send to 3rd party directly
//			try {
//				CamelRedirectSbbLocalObject camelRedirectSbbLocalObject = this.getCamelRedirectSbbLocalObject();
//				camelRedirectSbbLocalObject.processApplyChargingReportRequestFromMno(event);
//			} catch (Exception e) {
//				logger.severe("Error while calling processApplyChargingReportRequestFromMno on Child Sbb", e);
//				// TODO Handle the error condition
//			}
//
//		} else {
        XmlCAPDialog dialog = this.getXmlCAPDialog();
        dialog.addCAPMessage(((CAPEvent) event).getWrappedEvent());
        this.setXmlCAPDialog(dialog);

        camelStatAggregator.updateMessagesRecieved();
        camelStatAggregator.updateMessagesAll();

        //		}
	}

	public void onAPPLY_CHARGING_REQUEST(
			org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.ApplyChargingRequest event,
			ActivityContextInterface aci) {
	}

	public void onASSIST_REQUEST_INSTRUCTIONS_REQUEST(
			org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.AssistRequestInstructionsRequest event,
			ActivityContextInterface aci) {
	}

	public void onCALL_INFORMATION_REPORT_REQUEST(
			org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.CallInformationReportRequest event,
			ActivityContextInterface aci) {
	}

	public void onCALL_INFORMATION_REQUEST_REQUEST(
			org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.CallInformationRequestRequest event,
			ActivityContextInterface aci/* , EventContext eventContext */) {
	}

	public void onCANCEL_REQUEST(org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.CancelRequest event,
			ActivityContextInterface aci/* , EventContext eventContext */) {
	}

	public void onCONNECT_REQUEST(org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.ConnectRequest event,
			ActivityContextInterface aci/* , EventContext eventContext */) {
	}

	public void onCONNECT_TO_RESOURCE_REQUEST(
			org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.ConnectToResourceRequest event,
			ActivityContextInterface aci/* , EventContext eventContext */) {
	}

	public void onCONTINUE_REQUEST(
			org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.ContinueRequest event,
			ActivityContextInterface aci) {
	}

	public void onDISCONNECT_FORWARD_CONNECTION_REQUEST(
			org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.DisconnectForwardConnectionRequest event,
			ActivityContextInterface aci/* , EventContext eventContext */) {
	}

	public void onESTABLISH_TEMPORARY_CONNECTION_REQUEST(
			org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.EstablishTemporaryConnectionRequest event,
			ActivityContextInterface aci/* , EventContext eventContext */) {
	}

	public void onEVENT_REPORT_BCSM_REQUEST(
			org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.EventReportBCSMRequest event,
			ActivityContextInterface aci/* , EventContext eventContext */) {
//		if (this.getRedirect()) {
//			// If its redirect, send to 3rd party directly
//			try {
//				CamelRedirectSbbLocalObject camelRedirectSbbLocalObject = this.getCamelRedirectSbbLocalObject();
//				camelRedirectSbbLocalObject.processEventReportBCSMRequestFromMno(event);
//			} catch (Exception e) {
//				logger.severe("Error while calling processEventReportBCSMRequestFromMno on Child Sbb", e);
//				// TODO Handle the error condition
//			}
//
//		} else {
        XmlCAPDialog dialog = this.getXmlCAPDialog();
        dialog.addCAPMessage(((CAPEvent) event).getWrappedEvent());
        this.setXmlCAPDialog(dialog);

        camelStatAggregator.updateMessagesRecieved();
        camelStatAggregator.updateMessagesAll();
//		}
	}

	public void onFURNISH_CHARGING_INFORMATION_REQUEST(
			org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.FurnishChargingInformationRequest event,
			ActivityContextInterface aci/* , EventContext eventContext */) {
	}

	public void onPLAY_ANNOUNCEMENT_REQUEST(
			org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.PlayAnnouncementRequest event,
			ActivityContextInterface aci/* , EventContext eventContext */) {
	}

	public void onRELEASE_CALL_REQUEST(
			org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.ReleaseCallRequest event,
			ActivityContextInterface aci/* , EventContext eventContext */) {
	}

	public void onPROMPT_AND_COLLECT_USER_INFORMATION_REQUEST(
			org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.PromptAndCollectUserInformationRequest event,
			ActivityContextInterface aci/* , EventContext eventContext */) {
	}

    public void onPROMPT_AND_COLLECT_USER_INFORMATION_RESPONSE(
            org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.PromptAndCollectUserInformationResponse event,
            ActivityContextInterface aci/* , EventContext eventContext */) {
        XmlCAPDialog dialog = this.getXmlCAPDialog();
        dialog.addCAPMessage(((CAPEvent) event).getWrappedEvent());
        this.setXmlCAPDialog(dialog);

        camelStatAggregator.updateMessagesRecieved();
        camelStatAggregator.updateMessagesAll();
    }

	public void onREQUEST_REPORT_BCSM_EVENT_REQUEST(
			org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.RequestReportBCSMEventRequest event,
			ActivityContextInterface aci/* , EventContext eventContext */) {
	}

	public void onRESET_TIMER_REQUEST(
			org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.ResetTimerRequest event,
			ActivityContextInterface aci/* , EventContext eventContext */) {
	}

	public void onSEND_CHARGING_INFORMATION_REQUEST(
			org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.SendChargingInformationRequest event,
			ActivityContextInterface aci/* , EventContext eventContext */) {
	}

	public void onSPECIALIZED_RESOURCE_REPORT_REQUEST(
			org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.SpecializedResourceReportRequest event,
			ActivityContextInterface aci/* , EventContext eventContext */) {
	}

	public void onINVOKE_TIMEOUT(org.mobicents.slee.resource.cap.events.InvokeTimeout event,
			ActivityContextInterface aci/* , EventContext eventContext */) {
	}

	public void onERROR_COMPONENT(org.mobicents.slee.resource.cap.events.ErrorComponent event,
			ActivityContextInterface aci/* , EventContext eventContext */) {
        XmlCAPDialog dialog = this.getXmlCAPDialog();
        try {
            dialog.sendErrorComponent(event.getInvokeId(), event.getCAPErrorMessage());
        } catch (CAPException e) {
            // This never occur
            e.printStackTrace();
        }
        this.setXmlCAPDialog(dialog);
	}

	public void onREJECT_COMPONENT(org.mobicents.slee.resource.cap.events.RejectComponent event,
			ActivityContextInterface aci/* , EventContext eventContext */) {
        XmlCAPDialog dialog = this.getXmlCAPDialog();
        try {
            dialog.sendRejectComponent(event.getInvokeId(), event.getProblem());
        } catch (CAPException e) {
            // This never occur
            e.printStackTrace();
        }
        this.setXmlCAPDialog(dialog);
	}

	public void onResponseEvent(net.java.client.slee.resource.http.event.ResponseEvent event,
			ActivityContextInterface aci) {
        HttpResponse response = event.getHttpResponse();
        HttpClientActivity httpClientActivity = ((HttpClientActivity) aci.getActivity());

        CAPDialog capDialog = this.getCAPDialog();

        if (this.getCapDialogClosed()) {
            // capDialog is already closed - we do not process an error but
            this.endHttpClientActivity(httpClientActivity);
            return;
        }

        if (response == null) {
            // Error condition
            logger.severe("Exception received for HTTP request sent. See traces above");

            this.abort(capDialog);
            this.endHttpClientActivity(httpClientActivity);

            return;
        }

        StatusLine statusLine = response.getStatusLine();

        int statusCode = statusLine.getStatusCode();

        switch (statusCode) {
            case 200:
                try {
                    byte[] xmlContent = null;
                    if (response.getEntity() != null) {
                        xmlContent = getResultData(response.getEntity());

                        if (logger.isFineEnabled()) {
                            logger.fine("Received answer content: \n" + new String(xmlContent));
                        }

                        if (xmlContent == null || xmlContent.length <= 0) {
                            if (logger.isInfoEnabled()) {
                                logger.info("Received empty payload from http server");
                            }
                            return;
                        }

                        EventsSerializeFactory factory = this.getEventsSerializeFactory();
                        XmlCAPDialog xmlCAPDialog = factory.deserialize(xmlContent);

                        if (xmlCAPDialog == null) {
                            logger.severe("Received Success Response but couldn't deserialize to Dialog. Dialog is null");
                            this.abort(capDialog);
                            this.endHttpClientActivity(httpClientActivity);
                            return;
                        }

                        CAPUserAbortReason capUserAbortReason = xmlCAPDialog.getCapUserAbortReason();
                        if (capUserAbortReason != null) {
                            capDialog.abort(capUserAbortReason);
                            this.endHttpClientActivity(httpClientActivity);
                            return;
                        }

//                        if (xmlCAPDialog.isRedirectRequest()) {
//                            // Redirect to 3rd party SCF
//
//                            this.setRedirect(true);
//                            this.getCamelRedirectSbbLocalObject().forwardIdp(xmlCAPDialog);
//                            return;
//                        }

                        Boolean prearrangedEnd = xmlCAPDialog.getPrearrangedEnd();
                        FastList<Long> sentInvokeIds = new FastList<Long>();

                        this.processXmlCAPDialog(xmlCAPDialog, capDialog, true, sentInvokeIds);

                        boolean httpPayloadIsSent = false;
                        if (camlePropertiesManagement.getUpdateAssignedInvokeIds() && (sentInvokeIds.size() > 0)) {
                            // TODO: sending back new HTTP request with sentInvokeIds - do we need this ?
                            // or make this option configurable
                            XmlCAPDialog dialog = this.getXmlCAPDialog();
                            dialog.reset();
                            for (FastList.Node<Long> n = sentInvokeIds.head(), end = sentInvokeIds.tail(); (n = n.getNext()) != end;) {
                                dialog.addAssignedInvokeIds(n.getValue());
                            }
                            try {
                                byte[] data = this.getEventsSerializeFactory().serialize(dialog);
                                this.sendXmlPayload(data);
                                httpPayloadIsSent = true;
                            } catch (Exception e) {
                                logger.severe(String.format("Exception while sending sentInvokeIds to an application = %s", event), e);
                                this.abort(capDialog);
                                this.endHttpClientActivity(httpClientActivity);
                                return;
                            }
                        }

                        if (capDialog.getState() == CAPDialogState.InitialReceived || prearrangedEnd != null
                                || (xmlCAPDialog.getSendEmptyContinue() != null && xmlCAPDialog.getSendEmptyContinue())
                                || xmlCAPDialog.getErrorComponents().size() > 0 || xmlCAPDialog.getCAPMessages().size() > 0) {
                            if (prearrangedEnd != null) {
                                capDialog.close(prearrangedEnd);
                                this.updateDialogTime();
                                if (httpPayloadIsSent)
                                    this.setCapDialogClosed(true);
                                else
                                    this.endHttpClientActivity(httpClientActivity);
                            } else {
                                capDialog.send();
                            }
                        }

                    } else {
                        logger.severe("Received Success Response but without response body");
                        this.abort(capDialog);
                        this.endHttpClientActivity(httpClientActivity);
                        return;
                    }

                } catch (Exception e) {
                    logger.severe("Error while processing 2xx", e);
                    this.abort(capDialog);
                    this.endHttpClientActivity(httpClientActivity);
                }
                break;

            default:
                logger.severe(String.format("Received non 2xx Response=%s", response));
                this.abort(capDialog);
                this.endHttpClientActivity(httpClientActivity);
                break;
        }
    }
	
	public void onTimerEvent(TimerEvent event, ActivityContextInterface aci) {
		XmlCAPDialog dialog = this.getXmlCAPDialog();
		dialog.setNoActivityTimeout(true);
		
		if(this.logger.isFineEnabled()){
			this.logger.fine("onTimerEvent-NoActivityTimeout fired for dialog="+dialog);
		}
		
		try {
	            byte[] data = this.getEventsSerializeFactory().serialize(dialog);
	            this.sendXmlPayload(data);
	        } catch (Exception e) {
	            logger.severe(String.format("Exception while sending onDIALOG_DELIMITER to an application", event), e);
	            // TODO Abort DIalog?
	        }
	        dialog.reset();
	        this.setXmlCAPDialog(dialog);
	        
	        this.resetTimer(aci);
	}

//	public abstract ChildRelationExt getCamelRedirectSbbChildRelation();

	/**
	 * CMP
	 */
	public abstract void setXmlCAPDialog(XmlCAPDialog dialog);

	public abstract XmlCAPDialog getXmlCAPDialog();

//	public abstract void setRedirect(boolean redirect);
//
//	public abstract boolean getRedirect();

	public abstract void setNetworkRoutingRule(NetworkRoutingRule networkRoutingRule);

	public abstract NetworkRoutingRule getNetworkRoutingRule();

    public abstract void setCapDialogClosed(boolean capDialogClosed);

    public abstract boolean getCapDialogClosed();
    
	// 'timerID' CMP field setter
	public abstract void setTimerID(TimerID value);

	// 'timerID' CMP field getter
	public abstract TimerID getTimerID();

    public abstract void setStartDialogTime(long value);

    public abstract long getStartDialogTime();

    /**
	 * Private methods
	 */

	private void sendXmlPayload(byte[] data) throws Exception {
		HttpClientActivity httpClientActivity = this.getHTTPClientActivity();

		if (httpClientActivity == null) {

			httpClientActivity = this.httpClientProvider.createHttpClientActivity(false, null);
			this.httpClientActivityContextInterfaceFactory.getActivityContextInterface(httpClientActivity).attach(
					this.sbbContext.getSbbLocalObject());
		}

		this.httpClientActivityContextInterfaceFactory.getActivityContextInterface(httpClientActivity).attach(
				this.sbbContext.getSbbLocalObject());

		String route = this.getNetworkRoutingRule().getRuleUrl();

		HttpPost uriRequest = createRequest(route, null, ACCEPTED_CONTENT_TYPE, null);

		// NOTE: here we assume that its text/xml utf8 encoded... bum.
		pushContent(uriRequest, ACCEPTED_CONTENT_TYPE, CONTENT_ENCODING, data);

        if (logger.isFineEnabled()) {
            logger.fine("Executing HttpPost=" + uriRequest + "\n" + new String(data));
        }
        httpClientActivity.execute(uriRequest, null);

	}

	private void pushContent(HttpUriRequest request, String contentType, String contentEncoding, byte[] content) {

		// TODO: check other preconditions?
		if (contentType != null && content != null && request instanceof HttpEntityEnclosingRequest) {
			BasicHttpEntity entity = new BasicHttpEntity();
			entity.setContent(new ByteArrayInputStream(content));
			entity.setContentLength(content.length);
			entity.setChunked(false);
			if (contentEncoding != null)
				entity.setContentEncoding(contentEncoding);
			entity.setContentType(contentType);
			HttpEntityEnclosingRequest rr = (HttpEntityEnclosingRequest) request;
			rr.setEntity(entity);
		}

	}

	private HttpPost createRequest(String uri, Map<String, String> queryParameters, String acceptedContent,
			Header[] headers) {

		if (uri == null) {
			throw new NullPointerException("URI mst not be null.");
		}
		String requestURI = uri;
		if (queryParameters != null) {
			requestURI = encode(requestURI, queryParameters);
		}
		HttpPost request = new HttpPost(uri);

		if (acceptedContent != null) {
			BasicHeader acceptedContentHeader = new BasicHeader("Accept", acceptedContent);
			request.addHeader(acceptedContentHeader);
		}
		if (headers != null) {
			for (Header h : headers) {
				request.addHeader(h);
			}
		}
		return request;
	}

	private String encode(String url, Map<String, String> queryParameters) {
		// seriously...
		if (queryParameters == null && queryParameters.size() == 0)
			return url;

		url = url + "?";
		int encCount = 0;
		Iterator<Map.Entry<String, String>> vks = queryParameters.entrySet().iterator();
		while (vks.hasNext()) {

			Map.Entry<String, String> e = vks.next();
			url += e.getKey() + "=" + e.getValue();
			encCount++;
			if (encCount < queryParameters.size()) {
				url += "&";
			}

		}

		return url;
	}

//	private CamelRedirectSbbLocalObject getCamelRedirectSbbLocalObject() {
//		ChildRelationExt childExt = getCamelRedirectSbbChildRelation();
//		CamelRedirectSbbLocalObject child = (CamelRedirectSbbLocalObject) childExt.get(REDIRECTER);
//		if (child == null) {
//			try {
//				child = (CamelRedirectSbbLocalObject) childExt.create(REDIRECTER);
//			} catch (TransactionRequiredLocalException e) {
//				logger.severe("TransactionRequiredLocalException when creating REDIRECTER child", e);
//			} catch (IllegalArgumentException e) {
//				logger.severe("IllegalArgumentException when creating REDIRECTER child", e);
//			} catch (NullPointerException e) {
//				logger.severe("NullPointerException when creating REDIRECTER child", e);
//			} catch (SLEEException e) {
//				logger.severe("SLEEException when creating REDIRECTER child", e);
//			} catch (CreateException e) {
//				logger.severe("CreateException when creating REDIRECTER child", e);
//			}
//		}
//		return child;
//	}

	private HttpClientActivity getHTTPClientActivity() {
		ActivityContextInterface[] acis = this.sbbContext.getActivities();
		for (ActivityContextInterface aci : acis) {
			Object activity = aci.getActivity();
			if (activity instanceof HttpClientActivity) {
				return (HttpClientActivity) activity;
			}
		}
		return null;
	}

	public void endHttpClientActivity() {
        this.endHttpClientActivity(this.getHTTPClientActivity());
    }

	private void endHttpClientActivity(HttpClientActivity httpClientActivity) {
		if (httpClientActivity != null) {
			httpClientActivity.endActivity();
		}
	}
	
	private void resetTimer(ActivityContextInterface ac) {
		cancelTimer();
		
		TimerOptions options = new TimerOptions();
		
		long waitingTime = (camlePropertiesManagement.getNoActivityTimeoutSec() * 1000);

		// Set the timer on ACI
		TimerID timerID = this.timerFacility.setTimer(ac, null, System
				.currentTimeMillis()
				+ waitingTime, options);

		setTimerID(timerID);
	}
	
	private void cancelTimer(){
		TimerID timerId = getTimerID();
		
		if(timerId!=null){
			this.timerFacility.cancelTimer(timerId);
		}
	}

    protected void updateDialogTime() {
        long startTime = this.getStartDialogTime();
        if (startTime != 0) {
            long endTime = System.currentTimeMillis();
            camelStatAggregator.updateSecondsAll((endTime - startTime) / 1000);
        }
    }

}