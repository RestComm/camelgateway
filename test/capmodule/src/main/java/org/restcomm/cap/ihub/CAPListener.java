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
package org.restcomm.cap.ihub;

import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;
import org.mobicents.protocols.ss7.cap.api.CAPDialog;
import org.mobicents.protocols.ss7.cap.api.CAPDialogListener;
import org.mobicents.protocols.ss7.cap.api.CAPException;
import org.mobicents.protocols.ss7.cap.api.CAPMessage;
import org.mobicents.protocols.ss7.cap.api.dialog.CAPGeneralAbortReason;
import org.mobicents.protocols.ss7.cap.api.dialog.CAPGprsReferenceNumber;
import org.mobicents.protocols.ss7.cap.api.dialog.CAPNoticeProblemDiagnostic;
import org.mobicents.protocols.ss7.cap.api.dialog.CAPUserAbortReason;
import org.mobicents.protocols.ss7.cap.api.errors.CAPErrorMessage;
import org.mobicents.protocols.ss7.cap.api.errors.CAPErrorMessageFactory;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.ActivityTestRequest;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.ActivityTestResponse;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.ApplyChargingReportRequest;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.ApplyChargingRequest;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.AssistRequestInstructionsRequest;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.CAPDialogCircuitSwitchedCall;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.CAPServiceCircuitSwitchedCallListener;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.CallGapRequest;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.CallInformationReportRequest;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.CallInformationRequestRequest;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.CancelRequest;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.CollectInformationRequest;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.ConnectRequest;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.ConnectToResourceRequest;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.ContinueRequest;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.ContinueWithArgumentRequest;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.DisconnectForwardConnectionRequest;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.DisconnectForwardConnectionWithArgumentRequest;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.DisconnectLegRequest;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.DisconnectLegResponse;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.EstablishTemporaryConnectionRequest;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.EventReportBCSMRequest;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.FurnishChargingInformationRequest;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.InitialDPRequest;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.InitiateCallAttemptRequest;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.InitiateCallAttemptResponse;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.MoveLegRequest;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.MoveLegResponse;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.PlayAnnouncementRequest;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.PromptAndCollectUserInformationRequest;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.PromptAndCollectUserInformationResponse;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.ReleaseCallRequest;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.RequestReportBCSMEventRequest;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.ResetTimerRequest;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.SendChargingInformationRequest;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.SpecializedResourceReportRequest;
import org.mobicents.protocols.ss7.tcap.asn.comp.PAbortCauseType;
import org.mobicents.protocols.ss7.tcap.asn.comp.Problem;

/**
 * 
 * 
 * @author Amit Bhayani
 * 
 */
public class CAPListener implements CAPDialogListener, CAPServiceCircuitSwitchedCallListener {

	private static final Logger logger = Logger.getLogger(CAPListener.class);

	private CAPSimulator iHubManagement = null;

	private final AtomicLong capMessagesReceivedCounter = new AtomicLong(0);
	private long currentMapMessageCount = 0;

	private final CAPErrorMessageFactory mAPErrorMessageFactory;

	protected CAPListener(CAPSimulator iHubManagement) {
		this.iHubManagement = iHubManagement;
		this.mAPErrorMessageFactory = this.iHubManagement.getCapProvider().getCAPErrorMessageFactory();
	}

	/**
	 * Dialog Listener
	 */

	@Override
	public void onDialogAccept(CAPDialog arg0, CAPGprsReferenceNumber arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDialogClose(CAPDialog arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDialogDelimiter(CAPDialog arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDialogNotice(CAPDialog arg0, CAPNoticeProblemDiagnostic arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDialogRelease(CAPDialog capDialog) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDialogRequest(CAPDialog capDialog, CAPGprsReferenceNumber capGprsReferenceNumber) {
		// TODO Auto-generated method stub
		this.currentMapMessageCount = this.capMessagesReceivedCounter.incrementAndGet();
	}

	@Override
	public void onDialogTimeout(CAPDialog arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDialogUserAbort(CAPDialog capDialog, CAPGeneralAbortReason generalReason,
			CAPUserAbortReason userReason) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDialogProviderAbort(CAPDialog capDialog, PAbortCauseType abortCause) {

	}

	/**
	 * Component Listener
	 */

	@Override
	public void onErrorComponent(CAPDialog capDialog, Long invokeId, CAPErrorMessage capErrorMessage) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onInvokeTimeout(CAPDialog arg0, Long arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCAPMessage(CAPMessage capMessage) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRejectComponent(CAPDialog capDialog, Long invokeId, Problem problem, boolean isLocalOriginated) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onActivityTestRequest(ActivityTestRequest arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onActivityTestResponse(ActivityTestResponse arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onApplyChargingReportRequest(ApplyChargingReportRequest arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onApplyChargingRequest(ApplyChargingRequest arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAssistRequestInstructionsRequest(AssistRequestInstructionsRequest arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCallInformationReportRequest(CallInformationReportRequest arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCallInformationRequestRequest(CallInformationRequestRequest arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCancelRequest(CancelRequest arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConnectRequest(ConnectRequest arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConnectToResourceRequest(ConnectToResourceRequest arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onContinueRequest(ContinueRequest arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onContinueWithArgumentRequest(ContinueWithArgumentRequest arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDisconnectForwardConnectionRequest(DisconnectForwardConnectionRequest arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDisconnectForwardConnectionWithArgumentRequest(DisconnectForwardConnectionWithArgumentRequest arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDisconnectLegRequest(DisconnectLegRequest arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDisconnectLegResponse(DisconnectLegResponse arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onEstablishTemporaryConnectionRequest(EstablishTemporaryConnectionRequest arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onEventReportBCSMRequest(EventReportBCSMRequest arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFurnishChargingInformationRequest(FurnishChargingInformationRequest arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onInitialDPRequest(InitialDPRequest arg0) {
		if (logger.isDebugEnabled()) {
			logger.debug("Received InitialDPRequest " + arg0);
		}
		try {

			CAPDialogCircuitSwitchedCall dialog = arg0.getCAPDialog();

			dialog.addContinueRequest();

			dialog.close(false);
		} catch (CAPException e) {
			logger.error("Error while trying to send CON", e);
		}

		// Send CON

	}

	@Override
	public void onInitiateCallAttemptRequest(InitiateCallAttemptRequest arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onInitiateCallAttemptResponse(InitiateCallAttemptResponse arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMoveLegRequest(MoveLegRequest arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMoveLegResponse(MoveLegResponse arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPlayAnnouncementRequest(PlayAnnouncementRequest arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPromptAndCollectUserInformationRequest(PromptAndCollectUserInformationRequest arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPromptAndCollectUserInformationResponse(PromptAndCollectUserInformationResponse arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onReleaseCallRequest(ReleaseCallRequest arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRequestReportBCSMEventRequest(RequestReportBCSMEventRequest arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onResetTimerRequest(ResetTimerRequest arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSendChargingInformationRequest(SendChargingInformationRequest arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSpecializedResourceReportRequest(SpecializedResourceReportRequest arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCollectInformationRequest(CollectInformationRequest arg0) {
		// TODO Auto-generated method stub
		
	}

    @Override
    public void onCallGapRequest(CallGapRequest arg0) {
        // TODO Auto-generated method stub
        
    }

	/**
	 * CAPServiceCircuitSwitchedCallListener
	 */

}
