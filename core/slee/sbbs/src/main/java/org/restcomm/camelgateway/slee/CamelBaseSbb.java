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

import java.io.IOException;

import javax.slee.ActivityContextInterface;
import javax.slee.CreateException;
import javax.slee.RolledBackContext;
import javax.slee.SbbContext;
import javax.slee.facilities.TimerFacility;
import javax.slee.facilities.Tracer;
import javax.slee.resource.ResourceAdaptorTypeID;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.java.client.slee.resource.http.HttpClientActivityContextInterfaceFactory;
import net.java.client.slee.resource.http.HttpClientResourceAdaptorSbbInterface;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.restcomm.camelgateway.CamelStatAggregator;
import org.restcomm.camelgateway.EventsSerializeFactory;
import org.restcomm.camelgateway.XmlCAPDialog;
import org.mobicents.protocols.asn.AsnOutputStream;
import org.mobicents.protocols.ss7.cap.api.CAPDialog;
import org.mobicents.protocols.ss7.cap.api.CAPException;
import org.mobicents.protocols.ss7.cap.api.CAPMessage;
import org.mobicents.protocols.ss7.cap.api.CAPParameterFactory;
import org.mobicents.protocols.ss7.cap.api.CAPProvider;
import org.mobicents.protocols.ss7.cap.api.dialog.CAPUserAbortReason;
import org.mobicents.protocols.ss7.cap.api.errors.CAPErrorMessage;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.CAPDialogCircuitSwitchedCall;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.ConnectRequest;
import org.mobicents.protocols.ss7.cap.primitives.CAPAsnPrimitive;
import org.mobicents.protocols.ss7.cap.service.circuitSwitchedCall.ApplyChargingReportRequestImpl;
import org.mobicents.protocols.ss7.cap.service.circuitSwitchedCall.ApplyChargingRequestImpl;
import org.mobicents.protocols.ss7.cap.service.circuitSwitchedCall.CancelRequestImpl;
import org.mobicents.protocols.ss7.cap.service.circuitSwitchedCall.ConnectToResourceRequestImpl;
import org.mobicents.protocols.ss7.cap.service.circuitSwitchedCall.ContinueRequestImpl;
import org.mobicents.protocols.ss7.cap.service.circuitSwitchedCall.EventReportBCSMRequestImpl;
import org.mobicents.protocols.ss7.cap.service.circuitSwitchedCall.FurnishChargingInformationRequestImpl;
import org.mobicents.protocols.ss7.cap.service.circuitSwitchedCall.InitialDPRequestImpl;
import org.mobicents.protocols.ss7.cap.service.circuitSwitchedCall.PromptAndCollectUserInformationRequestImpl;
import org.mobicents.protocols.ss7.cap.service.circuitSwitchedCall.ReleaseCallRequestImpl;
import org.mobicents.protocols.ss7.cap.service.circuitSwitchedCall.RequestReportBCSMEventRequestImpl;
import org.mobicents.slee.SbbContextExt;
import org.mobicents.slee.resource.cap.CAPContextInterfaceFactory;

/**
 * @author Amit Bhayani
 * @author sergey vetyutnev
 * 
 */
public class CamelBaseSbb {

	/**
	 * 
	 */
	public CamelBaseSbb() {
		// TODO Auto-generated constructor stub
	}

	protected EventsSerializeFactory eventsSerializeFactory = null;

    // -------------------------------------------------------------
    // Statistics STUFF
    // -------------------------------------------------------------
    protected CamelStatAggregator camelStatAggregator;

	// -------------------------------------------------------------
	// SLEE STUFF
	// -------------------------------------------------------------
	protected SbbContextExt sbbContext;

	protected Tracer logger;
	
	protected TimerFacility timerFacility = null;

	// -------------------------------------------------------------
	// CAP RA STUFF
	// -------------------------------------------------------------
	protected static final ResourceAdaptorTypeID capRATypeID = new ResourceAdaptorTypeID("CAPResourceAdaptorType",
			"org.mobicents", "2.0");
	protected static final String capRaLink = "CAPRA";
	protected CAPContextInterfaceFactory capAcif;
	protected CAPProvider capProvider;
	protected CAPParameterFactory capParameterFactory;

	// -------------------------------------------------------------
	// HTTP Client RA STUFF
	// -------------------------------------------------------------
	protected static final ResourceAdaptorTypeID httpClientRATypeID = new ResourceAdaptorTypeID(
			"HttpClientResourceAdaptorType", "org.mobicents", "4.0");
	protected static final String httpClientRaLink = "HttpClientResourceAdaptor";

	protected HttpClientActivityContextInterfaceFactory httpClientActivityContextInterfaceFactory;
	protected HttpClientResourceAdaptorSbbInterface httpClientProvider;

	protected EventsSerializeFactory getEventsSerializeFactory() {
		if (this.eventsSerializeFactory == null) {
			this.eventsSerializeFactory = new EventsSerializeFactory();
		}
		return this.eventsSerializeFactory;
	}

	// -------------------------------------------------------------
	// SLEE minimal STUFF
	// -------------------------------------------------------------
	public void setSbbContext(SbbContext sbbContext) {
		this.sbbContext = (SbbContextExt) sbbContext;
		this.logger = sbbContext.getTracer("CAMEL-GW");

		this.eventsSerializeFactory = new EventsSerializeFactory();

		// CAP RA
		this.capAcif = (CAPContextInterfaceFactory) this.sbbContext.getActivityContextInterfaceFactory(capRATypeID);
		this.capProvider = (CAPProvider) this.sbbContext.getResourceAdaptorInterface(capRATypeID, capRaLink);
		this.capParameterFactory = this.capProvider.getCAPParameterFactory();

		// HTTP-Client RA
		this.httpClientActivityContextInterfaceFactory = (HttpClientActivityContextInterfaceFactory) this.sbbContext
				.getActivityContextInterfaceFactory(httpClientRATypeID);
		this.httpClientProvider = (HttpClientResourceAdaptorSbbInterface) this.sbbContext.getResourceAdaptorInterface(
				httpClientRATypeID, httpClientRaLink);

		camelStatAggregator = CamelStatAggregator.getInstance();

		this.timerFacility = this.sbbContext.getTimerFacility();
	}

	public void unsetSbbContext() {
		// clean RAs
		this.capAcif = null;
		this.capProvider = null;
		this.capParameterFactory = null;

		this.httpClientActivityContextInterfaceFactory = null;
		this.httpClientProvider = null;

		// clean SLEE
		this.sbbContext = null;
		this.logger = null;
	}

	public void sbbCreate() throws CreateException {
	}

	public void sbbPostCreate() throws CreateException {
	}

	public void sbbActivate() {
	}

	public void sbbPassivate() {
	}

	public void sbbLoad() {
	}

	public void sbbStore() {
	}

	public void sbbRemove() {
	}

	public void sbbExceptionThrown(Exception exception, Object object, ActivityContextInterface activityContextInterface) {
	}

	public void sbbRolledBack(RolledBackContext rolledBackContext) {
	}

	protected void processXmlCAPDialog(XmlCAPDialog xmlCAPDialog, CAPDialog capDialog, boolean isScf, FastList<Long> sentInvokeIds)
			throws CAPException {

	    // marking of incoming Invokes for which there will not be responses / errors
		FastList<Long> processInvokeWithoutAnswerIds = xmlCAPDialog.getProcessInvokeWithoutAnswerIds();
		for (FastList.Node<Long> n = processInvokeWithoutAnswerIds.head(), end = processInvokeWithoutAnswerIds.tail(); (n = n
				.getNext()) != end;) {
			capDialog.processInvokeWithoutAnswer(n.getValue());
		}

        int addedMsgs = 0;
		Boolean prearrangedEnd = xmlCAPDialog.getPrearrangedEnd();

        // sending of errors
		FastMap<Long, CAPErrorMessage> errorMessages = xmlCAPDialog.getErrorComponents().getErrorComponents();
        for (FastMap.Entry<Long, CAPErrorMessage> n = errorMessages.head(), end = errorMessages.tail(); (n = n.getNext()) != end;) {
            Long invokeId = n.getKey();
            CAPErrorMessage capError = n.getValue();
            capDialog.sendErrorComponent(invokeId, capError);
            addedMsgs++;
        }

        // sending of Invokes / RRL
        FastList<CAPMessage> capMessages = xmlCAPDialog.getCAPMessages();
        for (FastList.Node<CAPMessage> n = capMessages.head(), end = capMessages.tail(); (n = n.getNext()) != end;) {
            camelStatAggregator.updateMessagesSent();
            camelStatAggregator.updateMessagesAll();

            CAPMessage capMessage = n.getValue();
            if (addedMsgs > 0) {
                // we need to test if we have enough free space to send a component in the same massage
                int encodedSize;
                if (prearrangedEnd != null) {
                    encodedSize = capDialog.getMessageUserDataLengthOnClose(prearrangedEnd);
                } else {
                    encodedSize = capDialog.getMessageUserDataLengthOnSend();
                }

                CAPAsnPrimitive asnPrimitive = (CAPAsnPrimitive) capMessage;
                AsnOutputStream asnOs = new AsnOutputStream();
                int nextMessageSize = 10; // 10 = max component encoding header size
                try {
                    asnPrimitive.encodeAll(asnOs);
                    nextMessageSize += asnOs.size(); // 10 = max component encoding header size
                } catch (CAPException e) {
                    // ignore it: this means that a message does not have a parameter body
                }

                if (encodedSize + nextMessageSize + 5 > capDialog.getMaxUserDataLength()) {
                    capDialog.send();
                    addedMsgs = 0;
                }
            }

            ProcessComponentResult ps = this.processCAPMessageFromApplication(capMessage, capDialog, isScf);
            if (ps.componentAdded)
                addedMsgs++;
            if (ps.invokeId != null && sentInvokeIds != null) {
                sentInvokeIds.add(ps.invokeId);
            }
		}// for loop
	}

    protected ProcessComponentResult processCAPMessageFromApplication(CAPMessage capMessage, CAPDialog capDialog, boolean isScf)
            throws CAPException {
        ProcessComponentResult res = new ProcessComponentResult();

		switch (capMessage.getMessageType()) {
		case initialDP_Request:
			if (!isScf) {
				// If its acting as SSF we accept IDP
				InitialDPRequestImpl initialDPRequestImpl = (InitialDPRequestImpl) capMessage;

				res.componentAdded = true;
				res.invokeId = ((CAPDialogCircuitSwitchedCall) capDialog).addInitialDPRequest(
						initialDPRequestImpl.getServiceKey(), initialDPRequestImpl.getCalledPartyNumber(),
						initialDPRequestImpl.getCallingPartyNumber(), initialDPRequestImpl.getCallingPartysCategory(),
						initialDPRequestImpl.getCGEncountered(), initialDPRequestImpl.getIPSSPCapabilities(),
						initialDPRequestImpl.getLocationNumber(), initialDPRequestImpl.getOriginalCalledPartyID(),
						initialDPRequestImpl.getExtensions(), initialDPRequestImpl.getHighLayerCompatibility(),
						initialDPRequestImpl.getAdditionalCallingPartyNumber(),
						initialDPRequestImpl.getBearerCapability(), initialDPRequestImpl.getEventTypeBCSM(),
						initialDPRequestImpl.getRedirectingPartyID(), initialDPRequestImpl.getRedirectionInformation(),
						initialDPRequestImpl.getCause(), initialDPRequestImpl.getServiceInteractionIndicatorsTwo(),
						initialDPRequestImpl.getCarrier(), initialDPRequestImpl.getCugIndex(),
						initialDPRequestImpl.getCugInterlock(), initialDPRequestImpl.getCugOutgoingAccess(),
						initialDPRequestImpl.getIMSI(), initialDPRequestImpl.getSubscriberState(),
						initialDPRequestImpl.getLocationInformation(), initialDPRequestImpl.getExtBasicServiceCode(),
						initialDPRequestImpl.getCallReferenceNumber(), initialDPRequestImpl.getMscAddress(),
						initialDPRequestImpl.getCalledPartyBCDNumber(), initialDPRequestImpl.getTimeAndTimezone(),
						initialDPRequestImpl.getCallForwardingSSPending(),
						initialDPRequestImpl.getInitialDPArgExtension());
			} else {
				logger.warning("CAMEL Gw is acting as SCF and received InitialDPRequest from an application. Dropping request");
			}

			break;

		case applyCharging_Request:
			if (isScf) {
				ApplyChargingRequestImpl applyChargingRequest = (ApplyChargingRequestImpl) capMessage;
                res.componentAdded = true;
				res.invokeId = ((CAPDialogCircuitSwitchedCall) capDialog).addApplyChargingRequest(
						applyChargingRequest.getAChBillingChargingCharacteristics(),
						applyChargingRequest.getPartyToCharge(), applyChargingRequest.getExtensions(),
						applyChargingRequest.getAChChargingAddress());
			} else {
				logger.warning("CAMEL Gw is acting as SSF and received ApplyChargingRequest from an application. Dropping request");
			}

			break;
		case applyChargingReport_Request:
			if (!isScf) {
				ApplyChargingReportRequestImpl applyChargingReportRequestImpl = (ApplyChargingReportRequestImpl) capMessage;
                res.componentAdded = true;
				res.invokeId = ((CAPDialogCircuitSwitchedCall) capDialog)
						.addApplyChargingReportRequest(applyChargingReportRequestImpl.getTimeDurationChargingResult());

			} else {
				logger.warning("CAMEL Gw is acting as SCF and received ApplyChargingReportRequest from an application. Dropping request");
			}
			break;

		case cancel_Request:
			if (isScf) {
				CancelRequestImpl cancelRequest = (CancelRequestImpl) capMessage;
				if (cancelRequest.getAllRequests()) {
	                res.componentAdded = true;
				    res.invokeId = ((CAPDialogCircuitSwitchedCall) capDialog).addCancelRequest_AllRequests();
				} else if (cancelRequest.getCallSegmentToCancel() != null) {
	                res.componentAdded = true;
				    res.invokeId = ((CAPDialogCircuitSwitchedCall) capDialog)
							.addCancelRequest_CallSegmentToCancel(cancelRequest.getCallSegmentToCancel());
				} else if (cancelRequest.getInvokeID() != null) {
	                res.componentAdded = true;
				    res.invokeId = ((CAPDialogCircuitSwitchedCall) capDialog).addCancelRequest_InvokeId(cancelRequest
							.getInvokeID());
				}
			} else {
				logger.warning("CAMEL Gw is acting as SSF and received CancelRequest from an application. Dropping request");
			}

			break;

		case connect_Request:
			if (isScf) {
				ConnectRequest connectRequest = (ConnectRequest) capMessage;
                res.componentAdded = true;
				res.invokeId = ((CAPDialogCircuitSwitchedCall) capDialog).addConnectRequest(
						connectRequest.getDestinationRoutingAddress(), connectRequest.getAlertingPattern(),
						connectRequest.getOriginalCalledPartyID(), connectRequest.getExtensions(),
						connectRequest.getCarrier(), connectRequest.getCallingPartysCategory(),
						connectRequest.getRedirectingPartyID(), connectRequest.getRedirectionInformation(),
						connectRequest.getGenericNumbers(), connectRequest.getServiceInteractionIndicatorsTwo(),
						connectRequest.getChargeNumber(), connectRequest.getLegToBeConnected(),
						connectRequest.getCUGInterlock(), connectRequest.getCugOutgoingAccess(),
						connectRequest.getSuppressionOfAnnouncement(), connectRequest.getOCSIApplicable(),
						connectRequest.getNAOliInfo(), connectRequest.getBorInterrogationRequested());
			} else {
				logger.warning("CAMEL Gw is acting as SSF and received ConnectRequest from an application. Dropping request");
			}
			break;
		case continue_Request:
			if (isScf) {
				ContinueRequestImpl continueRequest = (ContinueRequestImpl) capMessage;
                res.componentAdded = true;
				res.invokeId = ((CAPDialogCircuitSwitchedCall) capDialog).addContinueRequest();
			} else {
				logger.warning("CAMEL Gw is acting as SSF and received ContinueRequest from an application. Dropping request");
			}
			break;
		case eventReportBCSM_Request:
			if (!isScf) {
				EventReportBCSMRequestImpl eventReportBCSMRequestImpl = (EventReportBCSMRequestImpl) capMessage;
                res.componentAdded = true;
				res.invokeId = ((CAPDialogCircuitSwitchedCall) capDialog).addEventReportBCSMRequest(
						eventReportBCSMRequestImpl.getEventTypeBCSM(),
						eventReportBCSMRequestImpl.getEventSpecificInformationBCSM(),
						eventReportBCSMRequestImpl.getLegID(), eventReportBCSMRequestImpl.getMiscCallInfo(),
						eventReportBCSMRequestImpl.getExtensions());

			} else {
				logger.warning("CAMEL Gw is acting as SCF and received EventReportBCSMRequest from an application. Dropping request");
			}
			break;
		case releaseCall_Request:
			if (isScf) {
				ReleaseCallRequestImpl releaseCallRequest = (ReleaseCallRequestImpl) capMessage;
                res.componentAdded = true;
				res.invokeId = ((CAPDialogCircuitSwitchedCall) capDialog).addReleaseCallRequest(releaseCallRequest.getCause());
			} else {
				logger.warning("CAMEL Gw is acting as SSF and received ReleaseCallRequest from an application. Dropping request");
			}
			break;
        case requestReportBCSMEvent_Request:
            if (isScf) {
                RequestReportBCSMEventRequestImpl requestReportBCSMEventRequest = (RequestReportBCSMEventRequestImpl) capMessage;
                res.componentAdded = true;
                res.invokeId = ((CAPDialogCircuitSwitchedCall) capDialog)
                        .addRequestReportBCSMEventRequest(requestReportBCSMEventRequest.getBCSMEventList(),
                                requestReportBCSMEventRequest.getExtensions());
            } else {
                logger.warning("CAMEL Gw is acting as SSF and received RequestReportBCSMEventRequest. Dropping request");
            }
            break;

        case connectToResource_Request:
                if (isScf) {
                    ConnectToResourceRequestImpl connectToResourceRequest = (ConnectToResourceRequestImpl) capMessage;
                    res.componentAdded = true;
                    res.invokeId = ((CAPDialogCircuitSwitchedCall) capDialog).addConnectToResourceRequest(
                            connectToResourceRequest.getResourceAddress_IPRoutingAddress(),
                            connectToResourceRequest.getResourceAddress_Null(), connectToResourceRequest.getExtensions(),
                            connectToResourceRequest.getServiceInteractionIndicatorsTwo(),
                            connectToResourceRequest.getCallSegmentID());
                } else {
                    logger.warning("CAMEL Gw is acting as SSF and received connectToResource_Request. Dropping request");
                }
            break;
        case furnishChargingInformation_Request:
            if (isScf) {
                    FurnishChargingInformationRequestImpl furnishChargingInformationRequestImplRequest = (FurnishChargingInformationRequestImpl) capMessage;
                    res.componentAdded = true;
                    res.invokeId = ((CAPDialogCircuitSwitchedCall) capDialog)
                            .addFurnishChargingInformationRequest(furnishChargingInformationRequestImplRequest
                                    .getFCIBCCCAMELsequence1());
            } else {
                logger.warning("CAMEL Gw is acting as SSF and received furnishChargingInformation_Request. Dropping request");
            }
        break;
        case promptAndCollectUserInformation_Request:
            if (isScf) {
                PromptAndCollectUserInformationRequestImpl promptAndCollectUserInformationRequest = (PromptAndCollectUserInformationRequestImpl) capMessage;
                    res.componentAdded = true;
                    res.invokeId = ((CAPDialogCircuitSwitchedCall) capDialog).addPromptAndCollectUserInformationRequest(
                            promptAndCollectUserInformationRequest.getCollectedInfo(),
                            promptAndCollectUserInformationRequest.getDisconnectFromIPForbidden(),
                            promptAndCollectUserInformationRequest.getInformationToSend(),
                            promptAndCollectUserInformationRequest.getExtensions(),
                            promptAndCollectUserInformationRequest.getCallSegmentID(),
                            promptAndCollectUserInformationRequest.getRequestAnnouncementStartedNotification());
            } else {
                logger.warning("CAMEL Gw is acting as SSF and received furnishChargingInformation_Request. Dropping request");
            }
        break;
		}// switch

		return res;
	}

	protected CAPDialog getCAPDialog() {
		ActivityContextInterface[] acis = this.sbbContext.getActivities();
		for (ActivityContextInterface aci : acis) {
			Object activity = aci.getActivity();
			if (activity instanceof CAPDialog) {
				return (CAPDialog) activity;
			}
		}

		return null;
	}

	protected void abort(CAPDialog capDialog) {
		if (capDialog != null) {
			try {
				capDialog.abort(CAPUserAbortReason.no_reason_given);
			} catch (CAPException e) {
				logger.severe("Exception while trying to abort CAP Dialog", e);
			}
		}
	}

	protected byte[] getResultData(HttpEntity entity) throws IOException {
		return EntityUtils.toByteArray(entity);
	}

    private class ProcessComponentResult {
        public boolean componentAdded;
        public Long invokeId;
    }
}
