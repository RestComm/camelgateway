/**
 * 
 */
package org.restcomm.camelgateway;

import javax.naming.OperationNotSupportedException;

import javolution.util.FastList;
import javolution.xml.XMLFormat;
import javolution.xml.XMLSerializable;
import javolution.xml.stream.XMLStreamException;

import org.mobicents.protocols.ss7.cap.api.CAPApplicationContext;
import org.mobicents.protocols.ss7.cap.api.CAPException;
import org.mobicents.protocols.ss7.cap.api.CAPMessage;
import org.mobicents.protocols.ss7.cap.api.CAPMessageType;
import org.mobicents.protocols.ss7.cap.api.CAPServiceBase;
import org.mobicents.protocols.ss7.cap.api.dialog.CAPDialogState;
import org.mobicents.protocols.ss7.cap.api.dialog.CAPGprsReferenceNumber;
import org.mobicents.protocols.ss7.cap.api.dialog.CAPUserAbortReason;
import org.mobicents.protocols.ss7.cap.api.errors.CAPErrorMessage;
import org.mobicents.protocols.ss7.cap.service.circuitSwitchedCall.ApplyChargingReportRequestImpl;
import org.mobicents.protocols.ss7.cap.service.circuitSwitchedCall.ApplyChargingRequestImpl;
import org.mobicents.protocols.ss7.cap.service.circuitSwitchedCall.CancelRequestImpl;
import org.mobicents.protocols.ss7.cap.service.circuitSwitchedCall.ConnectRequestImpl;
import org.mobicents.protocols.ss7.cap.service.circuitSwitchedCall.ConnectToResourceRequestImpl;
import org.mobicents.protocols.ss7.cap.service.circuitSwitchedCall.ContinueRequestImpl;
import org.mobicents.protocols.ss7.cap.service.circuitSwitchedCall.EventReportBCSMRequestImpl;
import org.mobicents.protocols.ss7.cap.service.circuitSwitchedCall.FurnishChargingInformationRequestImpl;
import org.mobicents.protocols.ss7.cap.service.circuitSwitchedCall.InitialDPRequestImpl;
import org.mobicents.protocols.ss7.cap.service.circuitSwitchedCall.PromptAndCollectUserInformationRequestImpl;
import org.mobicents.protocols.ss7.cap.service.circuitSwitchedCall.PromptAndCollectUserInformationResponseImpl;
import org.mobicents.protocols.ss7.cap.service.circuitSwitchedCall.ReleaseCallRequestImpl;
import org.mobicents.protocols.ss7.cap.service.circuitSwitchedCall.RequestReportBCSMEventRequestImpl;
import org.mobicents.protocols.ss7.sccp.impl.parameter.SccpAddressImpl;
import org.mobicents.protocols.ss7.sccp.parameter.SccpAddress;
import org.mobicents.protocols.ss7.tcap.api.MessageType;
import org.mobicents.protocols.ss7.tcap.asn.comp.Invoke;
import org.mobicents.protocols.ss7.tcap.asn.comp.PAbortCauseType;
import org.mobicents.protocols.ss7.tcap.asn.comp.Problem;
import org.mobicents.protocols.ss7.tcap.asn.comp.ReturnResultLast;

/**
 * @author Amit Bhayani
 * @author sergey vetyutnev
 *
 */
public class XmlCAPDialog implements org.mobicents.protocols.ss7.cap.api.CAPDialog, XMLSerializable {

	private static final String NETWORK_ID = "networkId";
    private static final String DIALOG_TYPE = "type";
	
	private static final String CAP_APPLN_CONTEXT = "appCntx";

	private static final String SCCP_ORG_ADD = "origAddress";
	private static final String SCCP_DST_ADD = "destAddress";

    private static final String CAP_USER_ABORT_REASON = "capUserAbortReason";
    private static final String P_ABORT_CAUSE_TYPE = "pAbortCauseType";

    private static final String PRE_ARRANGED_END = "prearrangedEnd";
    private static final String SEND_EMPTY_CONTINUE = "sendEmptyContinue";
	
	private static final String NO_ACTIVITY_TIMEOUT = "noActivityTimeOut";

	private static final String RETURN_MSG_ON_ERR = "returnMessageOnError";
	
//	private static final String REDIRECT_REQUEST = "redirectRequest";

	private static final String CAP_MSGS_SIZE = "capMessagesSize";

	private static final String LOCAL_ID = "localId";
	private static final String REMOTE_ID = "remoteId";

    private static final String INVOKE_WITHOUT_ANSWERS_ID = "invokeWithoutAnswerIds";
    private static final String ASSIGNED_INVOKE_IDS = "assignedInvokeIds";

    private static final String ERROR_COMPONENTS = "errComponents";
    private static final String REJECT_COMPONENTS = "rejectComponents";

	private static final String COMMA_SEPARATOR = ",";

	// Application Context of this Dialog
	protected CAPApplicationContext appCntx;

	protected SccpAddress origAddress;
	protected SccpAddress destAddress;

	private MessageType messageType = MessageType.Unknown;

	private CAPUserAbortReason capUserAbortReason = null;
	private PAbortCauseType pAbortCauseType = null;
    private Boolean prearrangedEnd = null;
    private Boolean sendEmptyContinue = null;

	private Long localId;
	private Long remoteId;

	private int networkId;

	private boolean returnMessageOnError = false;
	
	private Boolean noActivityTimeout = null;
	
//	private boolean redirectRequest = false;

	private FastList<Long> processInvokeWithoutAnswerIds = new FastList<Long>();
    private FastList<CAPMessage> capMessages = new FastList<CAPMessage>();
    private FastList<Long> assignedInvokeIds = new FastList<Long>();

    private ErrorComponentCap errorComponents = new ErrorComponentCap();
    private RejectComponentCap rejectComponents = new RejectComponentCap();

	private CAPDialogState state = CAPDialogState.Idle;

	public XmlCAPDialog() {
		super();
	}

	/**
	 * 
	 */
	public XmlCAPDialog(CAPApplicationContext appCntx, SccpAddress origAddress, SccpAddress destAddress, Long localId,
			Long remoteId) {
		this.appCntx = appCntx;
		this.origAddress = origAddress;
		this.destAddress = destAddress;
		this.localId = localId;
		this.remoteId = remoteId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.protocols.ss7.cap.api.CAPDialog#abort(org.mobicents.protocols
	 * .ss7.cap.api.dialog.CAPUserAbortReason)
	 */
	@Override
	public void abort(CAPUserAbortReason capUserAbortReason) throws CAPException {
		this.capUserAbortReason = capUserAbortReason;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.protocols.ss7.cap.api.CAPDialog#cancelInvocation(java.lang
	 * .Long)
	 */
	@Override
	public boolean cancelInvocation(Long invokeId) throws CAPException {
		throw new CAPException(new OperationNotSupportedException());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.protocols.ss7.cap.api.CAPDialog#close(boolean)
	 */
	@Override
	public void close(boolean prearrangedEnd) throws CAPException {
		this.prearrangedEnd = prearrangedEnd;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.protocols.ss7.cap.api.CAPDialog#closeDelayed(boolean)
	 */
	@Override
	public void closeDelayed(boolean prearrangedEnd) throws CAPException {
		throw new CAPException(new OperationNotSupportedException());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.protocols.ss7.cap.api.CAPDialog#getApplicationContext()
	 */
	@Override
	public CAPApplicationContext getApplicationContext() {
		return this.appCntx;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.protocols.ss7.cap.api.CAPDialog#getGprsReferenceNumber()
	 */
	@Override
	public CAPGprsReferenceNumber getGprsReferenceNumber() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.protocols.ss7.cap.api.CAPDialog#getLocalAddress()
	 */
	@Override
	public SccpAddress getLocalAddress() {
		return this.origAddress;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.protocols.ss7.cap.api.CAPDialog#getLocalDialogId()
	 */
	@Override
	public Long getLocalDialogId() {
		return this.localId;
	}
	
	@Override
	public int getNetworkId() {
		return networkId;
	}

	@Override
	public void setNetworkId(int networkId) {
		this.networkId = networkId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.protocols.ss7.cap.api.CAPDialog#getMaxUserDataLength()
	 */
	@Override
	public int getMaxUserDataLength() {
		// TODO Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.protocols.ss7.cap.api.CAPDialog#getMessageUserDataLengthOnClose
	 * (boolean)
	 */
	@Override
	public int getMessageUserDataLengthOnClose(boolean prearrangedEnd) throws CAPException {
		// TODO Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.protocols.ss7.cap.api.CAPDialog#getMessageUserDataLengthOnSend
	 * ()
	 */
	@Override
	public int getMessageUserDataLengthOnSend() throws CAPException {
		// TODO Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.protocols.ss7.cap.api.CAPDialog#getReceivedGprsReferenceNumber
	 * ()
	 */
	@Override
	public CAPGprsReferenceNumber getReceivedGprsReferenceNumber() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.protocols.ss7.cap.api.CAPDialog#getRemoteAddress()
	 */
	@Override
	public SccpAddress getRemoteAddress() {
		return this.destAddress;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.protocols.ss7.cap.api.CAPDialog#getRemoteDialogId()
	 */
	@Override
	public Long getRemoteDialogId() {
		return this.remoteId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.protocols.ss7.cap.api.CAPDialog#getReturnMessageOnError()
	 */
	@Override
	public boolean getReturnMessageOnError() {
		return this.returnMessageOnError;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.protocols.ss7.cap.api.CAPDialog#getService()
	 */
	@Override
	public CAPServiceBase getService() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.protocols.ss7.cap.api.CAPDialog#getState()
	 */
	@Override
	public CAPDialogState getState() {
		return this.state;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.protocols.ss7.cap.api.CAPDialog#getTCAPMessageType()
	 */
	@Override
	public MessageType getTCAPMessageType() {
        return this.messageType;
	}

    public void setTCAPMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.protocols.ss7.cap.api.CAPDialog#getUserObject()
	 */
	@Override
	public Object getUserObject() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.protocols.ss7.cap.api.CAPDialog#keepAlive()
	 */
	@Override
	public void keepAlive() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.protocols.ss7.cap.api.CAPDialog#processInvokeWithoutAnswer
	 * (java.lang.Long)
	 */
	@Override
    public void processInvokeWithoutAnswer(Long invokeId) {
        this.processInvokeWithoutAnswerIds.add(invokeId);
    }

	public void addAssignedInvokeIds(Long invokeId) {
        this.assignedInvokeIds.add(invokeId);
    }

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.protocols.ss7.cap.api.CAPDialog#release()
	 */
	@Override
	public void release() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.protocols.ss7.cap.api.CAPDialog#resetInvokeTimer(java.lang
	 * .Long)
	 */
	@Override
	public void resetInvokeTimer(Long arg0) throws CAPException {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.protocols.ss7.cap.api.CAPDialog#send()
	 */
	@Override
	public void send() throws CAPException {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.protocols.ss7.cap.api.CAPDialog#sendDelayed()
	 */
	@Override
	public void sendDelayed() throws CAPException {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.protocols.ss7.cap.api.CAPDialog#sendErrorComponent(java
	 * .lang.Long, org.mobicents.protocols.ss7.cap.api.errors.CAPErrorMessage)
	 */
	@Override
	public void sendErrorComponent(Long invokeId, CAPErrorMessage capErrorMessage) throws CAPException {
		this.errorComponents.put(invokeId, capErrorMessage);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.protocols.ss7.cap.api.CAPDialog#sendInvokeComponent(org
	 * .mobicents.protocols.ss7.tcap.asn.comp.Invoke)
	 */
	@Override
	public void sendInvokeComponent(Invoke invoke) throws CAPException {
		throw new CAPException(new OperationNotSupportedException());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.protocols.ss7.cap.api.CAPDialog#sendRejectComponent(java
	 * .lang.Long, org.mobicents.protocols.ss7.tcap.asn.comp.Problem)
	 */
	@Override
	public void sendRejectComponent(Long invokeId, Problem problem) throws CAPException {
        this.rejectComponents.put(invokeId, problem);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.protocols.ss7.cap.api.CAPDialog#sendReturnResultLastComponent
	 * (org.mobicents.protocols.ss7.tcap.asn.comp.ReturnResultLast)
	 */
	@Override
	public void sendReturnResultLastComponent(ReturnResultLast arg0) throws CAPException {
		throw new CAPException(new OperationNotSupportedException());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.protocols.ss7.cap.api.CAPDialog#setGprsReferenceNumber(
	 * org.mobicents.protocols.ss7.cap.api.dialog.CAPGprsReferenceNumber)
	 */
	@Override
	public void setGprsReferenceNumber(CAPGprsReferenceNumber arg0) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.protocols.ss7.cap.api.CAPDialog#setLocalAddress(org.mobicents
	 * .protocols.ss7.sccp.parameter.SccpAddress)
	 */
	@Override
	public void setLocalAddress(SccpAddress origAddress) {
		this.origAddress = origAddress;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.protocols.ss7.cap.api.CAPDialog#setRemoteAddress(org.mobicents
	 * .protocols.ss7.sccp.parameter.SccpAddress)
	 */
	@Override
	public void setRemoteAddress(SccpAddress destAddress) {
		this.destAddress = destAddress;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.protocols.ss7.cap.api.CAPDialog#setReturnMessageOnError
	 * (boolean)
	 */
	@Override
	public void setReturnMessageOnError(boolean returnMessageOnError) {
		this.returnMessageOnError = returnMessageOnError;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.protocols.ss7.cap.api.CAPDialog#setUserObject(java.lang
	 * .Object)
	 */
	@Override
	public void setUserObject(Object arg0) {
		// TODO Auto-generated method stub

	}

//	/**
//	 * 
//	 * @return
//	 */
//	public boolean isRedirectRequest() {
//		return redirectRequest;
//	}
//
//	/**
//	 * 
//	 * @param redirectRequest
//	 */
//	public void setRedirectRequest(boolean redirectRequest) {
//		this.redirectRequest = redirectRequest;
//	}

	public Boolean getNoActivityTimeout() {
		return noActivityTimeout;
	}

	public void setNoActivityTimeout(Boolean noActivityTimeout) {
		this.noActivityTimeout = noActivityTimeout;
	}

	/**
	 * Non CAPDialog methods
	 */

	public void addCAPMessage(CAPMessage capMessage) {
		this.capMessages.add(capMessage);
	}

	public boolean removeCAPMessage(CAPMessage capMessage) {
		return this.capMessages.remove(capMessage);
	}

	public FastList<CAPMessage> getCAPMessages() {
		return this.capMessages;
	}

    public FastList<Long> getProcessInvokeWithoutAnswerIds() {
        return this.processInvokeWithoutAnswerIds;
    }

    public FastList<Long> getAssignedInvokeIds() {
        return this.assignedInvokeIds;
    }

    public ErrorComponentCap getErrorComponents() {
        return errorComponents;
    }

    public RejectComponentCap getRejectComponents() {
        return rejectComponents;
    }

	public void reset() {
		this.capMessages.clear();
        this.processInvokeWithoutAnswerIds.clear();
        this.assignedInvokeIds.clear();
        this.errorComponents.clear();
        this.rejectComponents.clear();
        
        this.noActivityTimeout = null;
	}

    public CAPUserAbortReason getCapUserAbortReason() {
        return capUserAbortReason;
    }

    public PAbortCauseType getPAbortCauseType() {
        return pAbortCauseType;
    }

    public void setPAbortCauseType(PAbortCauseType val) {
        pAbortCauseType = val;
    }

    public Boolean getPrearrangedEnd() {
        return prearrangedEnd;
    }

    public Boolean getSendEmptyContinue() {
        return this.sendEmptyContinue;
    }

    public void setSendEmptyContinue(Boolean val) {
        this.sendEmptyContinue = val;
    }


	@Override
	public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("XmlCAPDialog [");
        if (appCntx != null) {
            sb.append("appCntx=");
            sb.append(appCntx);
        }
        if (origAddress != null) {
            sb.append(", origAddress=");
            sb.append(origAddress);
        }
        if (destAddress != null) {
            sb.append(", destAddress=");
            sb.append(destAddress);
        }
        if (messageType != null) {
            sb.append(", messageType=");
            sb.append(messageType);
        }
        if (pAbortCauseType != null) {
            sb.append(", pAbortCauseType=");
            sb.append(pAbortCauseType);
        }
        if (capUserAbortReason != null) {
            sb.append(", capUserAbortReason=");
            sb.append(capUserAbortReason);
        }
        if (prearrangedEnd != null) {
            sb.append(", prearrangedEnd=");
            sb.append(prearrangedEnd);
        }
        if (noActivityTimeout != null) {
            sb.append(", noActivityTimeout=");
            sb.append(noActivityTimeout);
        }        
        if (localId != null) {
            sb.append(", localId=");
            sb.append(localId);
        }
        if (remoteId != null) {
            sb.append(", remoteId=");
            sb.append(remoteId);
        }
        sb.append(", networkId=");
        sb.append(networkId);
        if (returnMessageOnError) {
            sb.append(", returnMessageOnError=");
            sb.append(returnMessageOnError);
        }
//        if (redirectRequest) {
//            sb.append(", redirectRequest=");
//            sb.append(redirectRequest);
//        }
        if (processInvokeWithoutAnswerIds != null && processInvokeWithoutAnswerIds.size() > 0) {
            sb.append(", processInvokeWithoutAnswerIds=");
            sb.append(processInvokeWithoutAnswerIds);
        }
        if (assignedInvokeIds != null && assignedInvokeIds.size() > 0) {
            sb.append(", assignedInvokeIds=");
            sb.append(assignedInvokeIds);
        }
        if (capMessages != null && capMessages.size() > 0) {
            sb.append(", capMessages=");
            sb.append(capMessages);
        }
        if (errorComponents != null && errorComponents.size() > 0) {
            sb.append(", errorComponents=");
            sb.append(errorComponents);
        }
        if (rejectComponents != null && rejectComponents.size() > 0) {
            sb.append(", rejectComponents=");
            sb.append(rejectComponents);
        }
        if (state != null) {
            sb.append(", state=");
            sb.append(state);
        }

        sb.append("]");
	    return sb.toString();
	}



    protected static final XMLFormat<XmlCAPDialog> USSR_XML = new XMLFormat<XmlCAPDialog>(XmlCAPDialog.class) {
        public void write(XmlCAPDialog dialog, OutputElement xml) throws XMLStreamException {
            xml.setAttribute(DIALOG_TYPE, dialog.messageType.name());
            if (dialog.appCntx != null) {
                xml.setAttribute(CAP_APPLN_CONTEXT, dialog.appCntx.name());
            }
            xml.setAttribute(NETWORK_ID, dialog.networkId);
            xml.setAttribute(LOCAL_ID, dialog.localId);
            xml.setAttribute(REMOTE_ID, dialog.remoteId);

            int size = dialog.processInvokeWithoutAnswerIds.size();
            if (size > 0) {
                StringBuffer sb = new StringBuffer();
                for (int count = 0; count < size; count++) {
                    sb.append(dialog.processInvokeWithoutAnswerIds.get(count));
                    if (count != (size - 1)) {
                        sb.append(COMMA_SEPARATOR);
                    }
                }

                xml.setAttribute(INVOKE_WITHOUT_ANSWERS_ID, sb.toString());
            }

            size = dialog.assignedInvokeIds.size();
            if (size > 0) {
                StringBuffer sb = new StringBuffer();
                for (int count = 0; count < size; count++) {
                    sb.append(dialog.assignedInvokeIds.get(count));
                    if (count != (size - 1)) {
                        sb.append(COMMA_SEPARATOR);
                    }
                }

                xml.setAttribute(ASSIGNED_INVOKE_IDS, sb.toString());
            }

            int capMessagsSize = dialog.capMessages.size();

            xml.setAttribute(CAP_MSGS_SIZE, capMessagsSize);

            if (dialog.capUserAbortReason != null) {
                xml.setAttribute(CAP_USER_ABORT_REASON, dialog.capUserAbortReason.name());
            }
            if (dialog.pAbortCauseType != null) {
                xml.setAttribute(P_ABORT_CAUSE_TYPE, dialog.pAbortCauseType.name());
            }

            xml.setAttribute(PRE_ARRANGED_END, dialog.prearrangedEnd);
            xml.setAttribute(SEND_EMPTY_CONTINUE, dialog.sendEmptyContinue);
            
            xml.setAttribute(NO_ACTIVITY_TIMEOUT, dialog.noActivityTimeout);

            xml.setAttribute(RETURN_MSG_ON_ERR, dialog.returnMessageOnError);

//            xml.setAttribute(REDIRECT_REQUEST, dialog.redirectRequest);

            xml.add((SccpAddressImpl) dialog.origAddress, SCCP_ORG_ADD, SccpAddressImpl.class);
            xml.add((SccpAddressImpl) dialog.destAddress, SCCP_DST_ADD, SccpAddressImpl.class);

            if (dialog.errorComponents.size() > 0)
                xml.add(dialog.errorComponents, ERROR_COMPONENTS, ErrorComponentCap.class);

            if (dialog.rejectComponents.size() > 0)
                xml.add(dialog.rejectComponents, REJECT_COMPONENTS, RejectComponentCap.class);

            if (dialog.capMessages.size() > 0) {
                for (FastList.Node<CAPMessage> n = dialog.capMessages.head(), end = dialog.capMessages.tail(); (n = n.getNext()) != end;) {

                    CAPMessage capMessage = n.getValue();

                    switch (capMessage.getMessageType()) {
                        case initialDP_Request:
                            xml.add((InitialDPRequestImpl) capMessage, CAPMessageType.initialDP_Request.name(),
                                    InitialDPRequestImpl.class);
                            break;
                        case applyCharging_Request:
                            xml.add((ApplyChargingRequestImpl) capMessage, CAPMessageType.applyCharging_Request.name(),
                                    ApplyChargingRequestImpl.class);
                            break;
                        case applyChargingReport_Request:
                            xml.add((ApplyChargingReportRequestImpl) capMessage,
                                    CAPMessageType.applyChargingReport_Request.name(), ApplyChargingReportRequestImpl.class);
                            break;
                        case cancel_Request:
                            xml.add((CancelRequestImpl) capMessage, CAPMessageType.cancel_Request.name(),
                                    CancelRequestImpl.class);
                            break;
                        case connect_Request:
                            xml.add((ConnectRequestImpl) capMessage, CAPMessageType.connect_Request.name(),
                                    ConnectRequestImpl.class);
                            break;
                        case continue_Request:
                            xml.add((ContinueRequestImpl) capMessage, CAPMessageType.continue_Request.name(),
                                    ContinueRequestImpl.class);
                            break;
                        case eventReportBCSM_Request:
                            xml.add((EventReportBCSMRequestImpl) capMessage, CAPMessageType.eventReportBCSM_Request.name(),
                                    EventReportBCSMRequestImpl.class);
                            break;
                        case releaseCall_Request:
                            xml.add((ReleaseCallRequestImpl) capMessage, CAPMessageType.releaseCall_Request.name(),
                                    ReleaseCallRequestImpl.class);
                            break;
                        case requestReportBCSMEvent_Request:
                            xml.add((RequestReportBCSMEventRequestImpl) capMessage,
                                    CAPMessageType.requestReportBCSMEvent_Request.name(),
                                    RequestReportBCSMEventRequestImpl.class);
                            break;

                        case connectToResource_Request:
                            xml.add((ConnectToResourceRequestImpl) capMessage,
                                    CAPMessageType.connectToResource_Request.name(),
                                    ConnectToResourceRequestImpl.class);
                            break;
                        case furnishChargingInformation_Request:
                            xml.add((FurnishChargingInformationRequestImpl) capMessage,
                                    CAPMessageType.furnishChargingInformation_Request.name(),
                                    FurnishChargingInformationRequestImpl.class);
                            break;
                        case promptAndCollectUserInformation_Request:
                            xml.add((PromptAndCollectUserInformationRequestImpl) capMessage,
                                    CAPMessageType.promptAndCollectUserInformation_Request.name(),
                                    PromptAndCollectUserInformationRequestImpl.class);
                            break;
                        case promptAndCollectUserInformation_Response:
                            xml.add((PromptAndCollectUserInformationResponseImpl) capMessage,
                                    CAPMessageType.promptAndCollectUserInformation_Response.name(),
                                    PromptAndCollectUserInformationResponseImpl.class);
                            break;

                        default:
                            break;
                    }
                }
            }
		}

		public void read(InputElement xml, XmlCAPDialog dialog) throws XMLStreamException {
            MessageType mt = MessageType.valueOf(xml.getAttribute(DIALOG_TYPE, MessageType.Unknown.name()));
            if (mt != MessageType.Unknown)
                dialog.messageType = mt;
            else
                dialog.messageType = null;

            String appCtxStr = xml.getAttribute(CAP_APPLN_CONTEXT, null);

			if (appCtxStr != null) {
				dialog.appCntx = CAPApplicationContext.valueOf(CAPApplicationContext.class, appCtxStr);
			}
			dialog.networkId = xml.getAttribute(NETWORK_ID, 0);
			dialog.localId = xml.getAttribute(LOCAL_ID, 0l);
			dialog.remoteId = xml.getAttribute(REMOTE_ID, 0l);

            String sb = xml.getAttribute(INVOKE_WITHOUT_ANSWERS_ID, null);
            if (sb != null) {
                String[] longStrsArr = sb.split(COMMA_SEPARATOR);
                for (int count = 0; count < longStrsArr.length; count++) {
                    dialog.processInvokeWithoutAnswer(Long.parseLong(longStrsArr[count]));
                }
            }
            sb = xml.getAttribute(ASSIGNED_INVOKE_IDS, null);
            if (sb != null) {
                String[] longStrsArr = sb.split(COMMA_SEPARATOR);
                for (int count = 0; count < longStrsArr.length; count++) {
                    dialog.addAssignedInvokeIds(Long.parseLong(longStrsArr[count]));
                }
            }

			int capMssgsSize = xml.getAttribute(CAP_MSGS_SIZE, 0);

            String capUsrAbrtReaStr = xml.getAttribute(CAP_USER_ABORT_REASON, null);
            if (capUsrAbrtReaStr != null) {
                dialog.capUserAbortReason = CAPUserAbortReason.valueOf(CAPUserAbortReason.class, capUsrAbrtReaStr);
            }
            String pAbortCauseTypeStr = xml.getAttribute(P_ABORT_CAUSE_TYPE, null);
            if (pAbortCauseTypeStr != null) {
                dialog.pAbortCauseType = CAPUserAbortReason.valueOf(PAbortCauseType.class, pAbortCauseTypeStr);
            }

            String preArrEndStr = xml.getAttribute(PRE_ARRANGED_END, null);
            if (preArrEndStr != null) {
                dialog.prearrangedEnd = Boolean.parseBoolean(preArrEndStr);
            }

            String sendEmptyContinueStr = xml.getAttribute(SEND_EMPTY_CONTINUE, null);
            if (sendEmptyContinueStr != null) {
                dialog.sendEmptyContinue = Boolean.parseBoolean(sendEmptyContinueStr);
            }

			String noActivityTimeoutStr = xml.getAttribute(NO_ACTIVITY_TIMEOUT, null);
			if(noActivityTimeoutStr != null){
				dialog.noActivityTimeout = Boolean.parseBoolean(noActivityTimeoutStr);
			}

			dialog.returnMessageOnError = xml.getAttribute(RETURN_MSG_ON_ERR, false);

//			dialog.redirectRequest = xml.getAttribute(REDIRECT_REQUEST, false);

			dialog.origAddress = xml.get(SCCP_ORG_ADD, SccpAddressImpl.class);
			dialog.destAddress = xml.get(SCCP_DST_ADD, SccpAddressImpl.class);

            ErrorComponentCap eComp = xml.get(ERROR_COMPONENTS, ErrorComponentCap.class);
            if (eComp != null)
                dialog.errorComponents = eComp;

            RejectComponentCap rComp = xml.get(REJECT_COMPONENTS, RejectComponentCap.class);
            if (rComp != null)
                dialog.rejectComponents = rComp;

			for (int count = 0; count < capMssgsSize; count++) {

				CAPMessage capMessage = xml.get(CAPMessageType.initialDP_Request.name(), InitialDPRequestImpl.class);

				if (capMessage == null) {
					capMessage = xml.get(CAPMessageType.applyCharging_Request.name(), ApplyChargingRequestImpl.class);
				}

				if (capMessage == null) {
					capMessage = xml.get(CAPMessageType.applyChargingReport_Request.name(),
							ApplyChargingReportRequestImpl.class);
				}

				if (capMessage == null) {
					capMessage = xml.get(CAPMessageType.cancel_Request.name(), CancelRequestImpl.class);
				}

				if (capMessage == null) {
					capMessage = xml.get(CAPMessageType.connect_Request.name(), ConnectRequestImpl.class);
				}

				if (capMessage == null) {
					capMessage = xml.get(CAPMessageType.continue_Request.name(), ContinueRequestImpl.class);
				}

				if (capMessage == null) {
					capMessage = xml.get(CAPMessageType.eventReportBCSM_Request.name(),
							EventReportBCSMRequestImpl.class);
				}

				if (capMessage == null) {
					capMessage = xml.get(CAPMessageType.releaseCall_Request.name(), ReleaseCallRequestImpl.class);
				}

                if (capMessage == null) {
                    capMessage = xml.get(CAPMessageType.requestReportBCSMEvent_Request.name(),
                            RequestReportBCSMEventRequestImpl.class);
                }

                if (capMessage == null) {
                    capMessage = xml.get(CAPMessageType.connectToResource_Request.name(), ConnectToResourceRequestImpl.class);
                }
                if (capMessage == null) {
                    capMessage = xml.get(CAPMessageType.furnishChargingInformation_Request.name(), FurnishChargingInformationRequestImpl.class);
                }
                if (capMessage == null) {
                    capMessage = xml.get(CAPMessageType.promptAndCollectUserInformation_Request.name(), PromptAndCollectUserInformationRequestImpl.class);
                }
                if (capMessage == null) {
                    capMessage = xml.get(CAPMessageType.promptAndCollectUserInformation_Response.name(), PromptAndCollectUserInformationResponseImpl.class);
                }

				dialog.addCAPMessage(capMessage);
			}
		}
	};

}
