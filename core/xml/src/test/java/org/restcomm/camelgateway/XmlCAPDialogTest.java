/**
 * 
 */
package org.restcomm.camelgateway;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.util.ArrayList;

import javolution.xml.stream.XMLStreamException;

import org.mobicents.protocols.ss7.cap.api.CAPApplicationContext;
import org.mobicents.protocols.ss7.cap.api.CAPException;
import org.mobicents.protocols.ss7.cap.api.dialog.CAPUserAbortReason;
import org.mobicents.protocols.ss7.cap.api.errors.CAPErrorCode;
import org.mobicents.protocols.ss7.cap.api.errors.CAPErrorMessage;
import org.mobicents.protocols.ss7.cap.api.errors.CAPErrorMessageFactory;
import org.mobicents.protocols.ss7.cap.api.errors.CAPErrorMessageSystemFailure;
import org.mobicents.protocols.ss7.cap.api.errors.UnavailableNetworkResource;
import org.mobicents.protocols.ss7.cap.api.isup.CalledPartyNumberCap;
import org.mobicents.protocols.ss7.cap.api.primitives.BCSMEvent;
import org.mobicents.protocols.ss7.cap.api.primitives.EventTypeBCSM;
import org.mobicents.protocols.ss7.cap.api.primitives.MonitorMode;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.primitive.DpSpecificCriteria;
import org.mobicents.protocols.ss7.cap.api.service.circuitSwitchedCall.primitive.MidCallControlInfo;
import org.mobicents.protocols.ss7.cap.errors.CAPErrorMessageFactoryImpl;
import org.mobicents.protocols.ss7.cap.isup.CalledPartyNumberCapImpl;
import org.mobicents.protocols.ss7.cap.isup.CauseCapImpl;
import org.mobicents.protocols.ss7.cap.isup.OriginalCalledNumberCapImpl;
import org.mobicents.protocols.ss7.cap.isup.RedirectingPartyIDCapImpl;
import org.mobicents.protocols.ss7.cap.primitives.BCSMEventImpl;
import org.mobicents.protocols.ss7.cap.primitives.ReceivingSideIDImpl;
import org.mobicents.protocols.ss7.cap.primitives.SendingSideIDImpl;
import org.mobicents.protocols.ss7.cap.service.circuitSwitchedCall.ApplyChargingReportRequestImpl;
import org.mobicents.protocols.ss7.cap.service.circuitSwitchedCall.ApplyChargingRequestImpl;
import org.mobicents.protocols.ss7.cap.service.circuitSwitchedCall.ConnectRequestImpl;
import org.mobicents.protocols.ss7.cap.service.circuitSwitchedCall.ReleaseCallRequestImpl;
import org.mobicents.protocols.ss7.cap.service.circuitSwitchedCall.RequestReportBCSMEventRequestImpl;
import org.mobicents.protocols.ss7.cap.service.circuitSwitchedCall.primitive.CAMELAChBillingChargingCharacteristicsImpl;
import org.mobicents.protocols.ss7.cap.service.circuitSwitchedCall.primitive.DestinationRoutingAddressImpl;
import org.mobicents.protocols.ss7.cap.service.circuitSwitchedCall.primitive.DpSpecificCriteriaImpl;
import org.mobicents.protocols.ss7.cap.service.circuitSwitchedCall.primitive.MidCallControlInfoImpl;
import org.mobicents.protocols.ss7.cap.service.circuitSwitchedCall.primitive.TimeDurationChargingResultImpl;
import org.mobicents.protocols.ss7.cap.service.circuitSwitchedCall.primitive.TimeInformationImpl;
import org.mobicents.protocols.ss7.inap.api.primitives.LegType;
import org.mobicents.protocols.ss7.inap.isup.CallingPartysCategoryInapImpl;
import org.mobicents.protocols.ss7.inap.isup.RedirectionInformationInapImpl;
import org.mobicents.protocols.ss7.inap.primitives.LegIDImpl;
import org.mobicents.protocols.ss7.indicator.NatureOfAddress;
import org.mobicents.protocols.ss7.indicator.NumberingPlan;
import org.mobicents.protocols.ss7.indicator.RoutingIndicator;
import org.mobicents.protocols.ss7.isup.impl.message.parameter.CalledPartyNumberImpl;
import org.mobicents.protocols.ss7.sccp.impl.parameter.BCDEvenEncodingScheme;
import org.mobicents.protocols.ss7.sccp.impl.parameter.GlobalTitle0100Impl;
import org.mobicents.protocols.ss7.sccp.impl.parameter.SccpAddressImpl;
import org.mobicents.protocols.ss7.sccp.parameter.GlobalTitle;
import org.mobicents.protocols.ss7.sccp.parameter.SccpAddress;
import org.mobicents.protocols.ss7.tcap.api.MessageType;
import org.mobicents.protocols.ss7.tcap.asn.ProblemImpl;
import org.mobicents.protocols.ss7.tcap.asn.comp.InvokeProblemType;
import org.mobicents.protocols.ss7.tcap.asn.comp.PAbortCauseType;
import org.mobicents.protocols.ss7.tcap.asn.comp.Problem;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Amit Bhayani
 * 
 */
public class XmlCAPDialogTest {

	/**
	 * 
	 */
	public XmlCAPDialogTest() {
		// TODO Auto-generated constructor stub
	}

	private EventsSerializeFactory factory = null;

	@BeforeClass
	public void setUpClass() throws Exception {
		factory = new EventsSerializeFactory();
	}

	@AfterClass
	public void tearDownClass() throws Exception {
	}

	@BeforeMethod
	public void setUp() {
	}

	@AfterMethod
	public void tearDown() {
	}

	public byte[] getDataFailureCause() {
		return new byte[] { -124, -112 };
	}

	@Test
	public void testXmlCAPDialogNull() throws XMLStreamException, CAPException {
		XmlCAPDialog original = new XmlCAPDialog(null, null, null, null, null);
		byte[] serializedEvent = this.factory.serialize(original);
		System.out.println(new String(serializedEvent));

		XmlCAPDialog copy = this.factory.deserialize(serializedEvent);

		assertNotNull(copy);
	}

	@Test
	public void testXmlCAPDialogNotNull() throws XMLStreamException, CAPException {
		SccpAddress orgAddress = new SccpAddressImpl(RoutingIndicator.ROUTING_BASED_ON_DPC_AND_SSN, null, 1, 8);
		SccpAddress dstAddress = new SccpAddressImpl(RoutingIndicator.ROUTING_BASED_ON_DPC_AND_SSN, null, 2, 8);

		XmlCAPDialog original = new XmlCAPDialog(CAPApplicationContext.CapV1_gsmSSF_to_gsmSCF, orgAddress, dstAddress,
				12l, 13l);

		byte[] serializedEvent = this.factory.serialize(original);
		System.out.println(new String(serializedEvent));

		XmlCAPDialog copy = this.factory.deserialize(serializedEvent);

		assertNotNull(copy);

		assertEquals(copy.getApplicationContext(), original.getApplicationContext());
		assertEquals(copy.getLocalDialogId(), original.getLocalDialogId());
		assertEquals(copy.getRemoteDialogId(), original.getRemoteDialogId());
		assertEquals(copy.getLocalAddress(), original.getLocalAddress());
		assertEquals(copy.getRemoteAddress(), original.getRemoteAddress());
		assertEquals(copy.getReturnMessageOnError(), original.getReturnMessageOnError());
	}

	@Test
	public void testXmlCAPDialogUserAbortReason() throws XMLStreamException, CAPException {
		SccpAddress orgAddress = new SccpAddressImpl(RoutingIndicator.ROUTING_BASED_ON_DPC_AND_SSN, null, 1, 8);
		SccpAddress dstAddress = new SccpAddressImpl(RoutingIndicator.ROUTING_BASED_ON_DPC_AND_SSN, null, 2, 8);

		XmlCAPDialog original = new XmlCAPDialog(CAPApplicationContext.CapV1_gsmSSF_to_gsmSCF, orgAddress, dstAddress,
				12l, 13l);

		original.abort(CAPUserAbortReason.no_reason_given);
		original.setPAbortCauseType(PAbortCauseType.NoCommonDialoguePortion);

		byte[] serializedEvent = this.factory.serialize(original);
		System.out.println(new String(serializedEvent));

		XmlCAPDialog copy = this.factory.deserialize(serializedEvent);

		assertNotNull(copy);

		assertEquals(copy.getApplicationContext(), original.getApplicationContext());
		assertEquals(copy.getLocalDialogId(), original.getLocalDialogId());
		assertEquals(copy.getRemoteDialogId(), original.getRemoteDialogId());
		assertEquals(copy.getLocalAddress(), original.getLocalAddress());
		assertEquals(copy.getRemoteAddress(), original.getRemoteAddress());

        assertEquals(copy.getCapUserAbortReason(), original.getCapUserAbortReason());
        assertEquals(copy.getPAbortCauseType(), original.getPAbortCauseType());
	}

    @Test
    public void testXmlCAPDialogErrorComponents() throws XMLStreamException, CAPException {
        SccpAddress orgAddress = new SccpAddressImpl(RoutingIndicator.ROUTING_BASED_ON_DPC_AND_SSN, null, 1, 8);
        SccpAddress dstAddress = new SccpAddressImpl(RoutingIndicator.ROUTING_BASED_ON_DPC_AND_SSN, null, 2, 8);

        XmlCAPDialog original = new XmlCAPDialog(CAPApplicationContext.CapV1_gsmSSF_to_gsmSCF, orgAddress, dstAddress, 12l, 13l);

        CAPErrorMessageFactory capErrorMessageFactory = new CAPErrorMessageFactoryImpl();

        CAPErrorMessage capErrorMessage = capErrorMessageFactory
                .createCAPErrorMessageSystemFailure(UnavailableNetworkResource.endUserFailure);
        CAPErrorMessage capErrorMessage2 = capErrorMessageFactory
                .createCAPErrorMessageParameterless((long) CAPErrorCode.improperCallerResponse);
        original.sendErrorComponent(1l, capErrorMessage);
        original.sendErrorComponent(2l, capErrorMessage2);
        Problem problem = new ProblemImpl();
        problem.setInvokeProblemType(InvokeProblemType.MistypedParameter);
        original.sendRejectComponent(3L, problem);

        byte[] serializedEvent = this.factory.serialize(original);
        String s = new String(serializedEvent);
        System.out.println(s);

        XmlCAPDialog copy = this.factory.deserialize(serializedEvent);

        assertNotNull(copy);

        assertEquals(copy.getApplicationContext(), original.getApplicationContext());
        assertEquals(copy.getLocalDialogId(), original.getLocalDialogId());
        assertEquals(copy.getRemoteDialogId(), original.getRemoteDialogId());
        assertEquals(copy.getLocalAddress(), original.getLocalAddress());
        assertEquals(copy.getRemoteAddress(), original.getRemoteAddress());

        assertEquals(copy.getErrorComponents().size(), original.getErrorComponents().size());
        CAPErrorMessageSystemFailure erro = (CAPErrorMessageSystemFailure) original.getErrorComponents().getErrorComponents()
                .get(1l);
        CAPErrorMessageSystemFailure errd = (CAPErrorMessageSystemFailure) copy.getErrorComponents().getErrorComponents()
                .get(1l);
        assertEquals(erro.getUnavailableNetworkResource(), errd.getUnavailableNetworkResource());

        Problem problemo = original.getRejectComponents().getRejectComponents().get(3L);
        Problem problemd = copy.getRejectComponents().getRejectComponents().get(3L);
        assertEquals(copy.getRejectComponents().size(), original.getRejectComponents().size());
        assertEquals(problemo.getInvokeProblemType(), problemd.getInvokeProblemType());
    }

	@Test
	public void testXmlCAPDialogPreArrangedTest() throws XMLStreamException, CAPException {
		SccpAddress orgAddress = new SccpAddressImpl(RoutingIndicator.ROUTING_BASED_ON_DPC_AND_SSN, null, 1, 8);
		SccpAddress dstAddress = new SccpAddressImpl(RoutingIndicator.ROUTING_BASED_ON_DPC_AND_SSN, null, 2, 8);

		XmlCAPDialog original = new XmlCAPDialog(CAPApplicationContext.CapV1_gsmSSF_to_gsmSCF, orgAddress, dstAddress,
				12l, 13l);

		original.close(true);

		byte[] serializedEvent = this.factory.serialize(original);
		System.out.println(new String(serializedEvent));

		XmlCAPDialog copy = this.factory.deserialize(serializedEvent);

		assertNotNull(copy);

		assertEquals(copy.getApplicationContext(), original.getApplicationContext());
		assertEquals(copy.getLocalDialogId(), original.getLocalDialogId());
		assertEquals(copy.getRemoteDialogId(), original.getRemoteDialogId());
		assertEquals(copy.getLocalAddress(), original.getLocalAddress());
		assertEquals(copy.getRemoteAddress(), original.getRemoteAddress());

		assertEquals(copy.getPrearrangedEnd(), original.getPrearrangedEnd());
	}

	@Test
	public void testApplyChargingRequest() throws XMLStreamException, CAPException {

		CAMELAChBillingChargingCharacteristicsImpl aChBillingChargingCharacteristics = new CAMELAChBillingChargingCharacteristicsImpl(
				36000, false, null, null, null, false);
		SendingSideIDImpl partyToCharge = new SendingSideIDImpl(LegType.leg1);
		ApplyChargingRequestImpl orgCapMessage = new ApplyChargingRequestImpl(aChBillingChargingCharacteristics,
				partyToCharge, null, null);

		XmlCAPDialog original = new XmlCAPDialog(CAPApplicationContext.CapV1_gsmSSF_to_gsmSCF, null, null, 12l, 13l);
        original.processInvokeWithoutAnswer(1l);
        original.processInvokeWithoutAnswer(3l);
        original.addAssignedInvokeIds(5L);
        original.addAssignedInvokeIds(6L);
		original.addCAPMessage(orgCapMessage);
        original.setTCAPMessageType(MessageType.End);

		byte[] serializedEvent = this.factory.serialize(original);
		System.out.println(new String(serializedEvent));

		XmlCAPDialog copy = this.factory.deserialize(serializedEvent);

		assertNotNull(copy);

		assertEquals(copy.getApplicationContext(), original.getApplicationContext());
		assertEquals(copy.getLocalDialogId(), original.getLocalDialogId());
        assertEquals(copy.getProcessInvokeWithoutAnswerIds().size(), original.getProcessInvokeWithoutAnswerIds().size());
        assertEquals(copy.getAssignedInvokeIds().size(), original.getAssignedInvokeIds().size());
        assertEquals(copy.getCAPMessages().size(), original.getCAPMessages().size());
        assertEquals(copy.getTCAPMessageType(), original.getTCAPMessageType());

        long piwao1 = original.getProcessInvokeWithoutAnswerIds().get(0);
        long piwao2 = original.getProcessInvokeWithoutAnswerIds().get(1);
        long piwac1 = copy.getProcessInvokeWithoutAnswerIds().get(0);
        long piwac2 = copy.getProcessInvokeWithoutAnswerIds().get(1);
        assertEquals(piwao1, piwac1);
        assertEquals(piwao2, piwac2);

        long aiio1 = original.getAssignedInvokeIds().get(0);
        long aiio2 = original.getAssignedInvokeIds().get(1);
        long aiic1 = copy.getAssignedInvokeIds().get(0);
        long aiic2 = copy.getAssignedInvokeIds().get(1);
        assertEquals(aiio1, aiic1);
        assertEquals(aiio2, aiic2);

		ApplyChargingRequestImpl cpyCapMessage = (ApplyChargingRequestImpl) copy.getCAPMessages().get(0);

		assertEquals(cpyCapMessage.getAChBillingChargingCharacteristics().getMaxCallPeriodDuration(), orgCapMessage
				.getAChBillingChargingCharacteristics().getMaxCallPeriodDuration());
		assertEquals(cpyCapMessage.getAChBillingChargingCharacteristics().getReleaseIfdurationExceeded(), orgCapMessage
				.getAChBillingChargingCharacteristics().getReleaseIfdurationExceeded());
		assertEquals(cpyCapMessage.getPartyToCharge().getSendingSideID(), orgCapMessage.getPartyToCharge()
				.getSendingSideID());

	}

	@Test
	public void testApplyChargingReportRequestTest() throws XMLStreamException, CAPException {
		ReceivingSideIDImpl partyToCharge = new ReceivingSideIDImpl(LegType.leg1);
		TimeInformationImpl timeInformation = new TimeInformationImpl(26);
		TimeDurationChargingResultImpl timeDurationChargingResult = new TimeDurationChargingResultImpl(partyToCharge,
				timeInformation, false, false, null, null);

		ApplyChargingReportRequestImpl orgCapMessage = new ApplyChargingReportRequestImpl(timeDurationChargingResult);
		orgCapMessage.setInvokeId(24);

		XmlCAPDialog original = new XmlCAPDialog(CAPApplicationContext.CapV1_gsmSSF_to_gsmSCF, null, null, 12l, 13l);

		original.processInvokeWithoutAnswer(1l);
		original.addCAPMessage(orgCapMessage);

		byte[] serializedEvent = this.factory.serialize(original);
		System.out.println(new String(serializedEvent));

		XmlCAPDialog copy = this.factory.deserialize(serializedEvent);

		assertNotNull(copy);

		assertEquals(copy.getApplicationContext(), original.getApplicationContext());
		assertEquals(copy.getLocalDialogId(), original.getLocalDialogId());
		assertEquals(copy.getProcessInvokeWithoutAnswerIds().size(), original.getProcessInvokeWithoutAnswerIds().size());
		assertEquals(copy.getCAPMessages().size(), original.getCAPMessages().size());
	}

	@Test
	public void testRequestReportBCSMEventRequest() throws XMLStreamException, CAPException {
		ArrayList<BCSMEvent> bcsmEventList = new ArrayList<BCSMEvent>();
		LegIDImpl legID = new LegIDImpl(true, LegType.leg2);
		BCSMEventImpl be = new BCSMEventImpl(EventTypeBCSM.routeSelectFailure, MonitorMode.interrupted, legID, null,
				false);
		bcsmEventList.add(be);
		legID = new LegIDImpl(true, LegType.leg2);
		be = new BCSMEventImpl(EventTypeBCSM.oCalledPartyBusy, MonitorMode.interrupted, legID, null, false);
		bcsmEventList.add(be);
		legID = new LegIDImpl(true, LegType.leg2);
		be = new BCSMEventImpl(EventTypeBCSM.oNoAnswer, MonitorMode.interrupted, legID, null, false);
		bcsmEventList.add(be);
		legID = new LegIDImpl(true, LegType.leg2);
		be = new BCSMEventImpl(EventTypeBCSM.oAnswer, MonitorMode.notifyAndContinue, legID, null, false);
		bcsmEventList.add(be);
		legID = new LegIDImpl(true, LegType.leg1);
		be = new BCSMEventImpl(EventTypeBCSM.oDisconnect, MonitorMode.interrupted, legID, null, false);
		bcsmEventList.add(be);
		legID = new LegIDImpl(true, LegType.leg2);
		be = new BCSMEventImpl(EventTypeBCSM.oDisconnect, MonitorMode.interrupted, legID, null, false);
		bcsmEventList.add(be);
		legID = new LegIDImpl(true, LegType.leg1);
		be = new BCSMEventImpl(EventTypeBCSM.oAbandon, MonitorMode.notifyAndContinue, legID, null, false);
		
		MidCallControlInfo midCallControlInfo = new MidCallControlInfoImpl(4, 6, "#", "0", "*", 100);
		DpSpecificCriteria dpSpecificCriteria = new DpSpecificCriteriaImpl(midCallControlInfo);
		be = new BCSMEventImpl(EventTypeBCSM.tMidCall, MonitorMode.notifyAndContinue, legID, dpSpecificCriteria, false);
        
		bcsmEventList.add(be);
		// EventTypeBCSM eventTypeBCSM, MonitorMode monitorMode, LegID legID,
		// DpSpecificCriteria dpSpecificCriteria, boolean
		// automaticRearm

		RequestReportBCSMEventRequestImpl elem = new RequestReportBCSMEventRequestImpl(bcsmEventList, null);

		XmlCAPDialog original = new XmlCAPDialog(CAPApplicationContext.CapV1_gsmSSF_to_gsmSCF, null, null, 12l, 13l);

		original.processInvokeWithoutAnswer(1l);
		original.addCAPMessage(elem);

		byte[] serializedEvent = this.factory.serialize(original);
		System.out.println(new String(serializedEvent));

		XmlCAPDialog copy = this.factory.deserialize(serializedEvent);

		assertNotNull(copy);

		assertEquals(copy.getApplicationContext(), original.getApplicationContext());
		assertEquals(copy.getLocalDialogId(), original.getLocalDialogId());
		assertEquals(copy.getProcessInvokeWithoutAnswerIds().size(), original.getProcessInvokeWithoutAnswerIds().size());
		assertEquals(copy.getCAPMessages().size(), original.getCAPMessages().size());
	}

	@Test
	public void testConnect() throws XMLStreamException, CAPException {
		SccpAddress orgAddress = new SccpAddressImpl(RoutingIndicator.ROUTING_BASED_ON_DPC_AND_SSN, null, 1, 8);
		SccpAddress dstAddress = new SccpAddressImpl(RoutingIndicator.ROUTING_BASED_ON_DPC_AND_SSN, null, 2, 8);

		XmlCAPDialog original = new XmlCAPDialog(CAPApplicationContext.CapV2_gsmSSF_to_gsmSCF, orgAddress, dstAddress,
				12l, 13l);

		ArrayList<CalledPartyNumberCap> calledPartyNumbers = new ArrayList<CalledPartyNumberCap>();
		CalledPartyNumberImpl cpn = new CalledPartyNumberImpl(2, "972201", 1, 2);
		CalledPartyNumberCapImpl calledPartyNumber = new CalledPartyNumberCapImpl(cpn);
		calledPartyNumbers.add(calledPartyNumber);
		DestinationRoutingAddressImpl destinationRoutingAddress = new DestinationRoutingAddressImpl(calledPartyNumbers);

		OriginalCalledNumberCapImpl originalCalledPartyID = new OriginalCalledNumberCapImpl(getOriginalCalledPartyID());
		CallingPartysCategoryInapImpl callingPartysCategory = new CallingPartysCategoryInapImpl(
				getCallingPartysCategory());
		RedirectingPartyIDCapImpl redirectingPartyID = new RedirectingPartyIDCapImpl(getRedirectingPartyID());
		RedirectionInformationInapImpl redirectionInformation = new RedirectionInformationInapImpl(
				getRedirectionInformation());

		ConnectRequestImpl originalConnectRequestImpl = new ConnectRequestImpl(destinationRoutingAddress, null,
				originalCalledPartyID, null, null, callingPartysCategory, redirectingPartyID, redirectionInformation,
				null, null, null, null, null, false, false, false, null, false);
		originalConnectRequestImpl.setInvokeId(24);

		original.addCAPMessage(originalConnectRequestImpl);

		byte[] serializedEvent = this.factory.serialize(original);
		System.out.println(new String(serializedEvent));

		XmlCAPDialog copy = this.factory.deserialize(serializedEvent);

		assertNotNull(copy);

		assertEquals(copy.getApplicationContext(), original.getApplicationContext());
		assertEquals(copy.getLocalDialogId(), original.getLocalDialogId());
		assertEquals(copy.getRemoteDialogId(), original.getRemoteDialogId());
		assertEquals(copy.getLocalAddress(), original.getLocalAddress());
		assertEquals(copy.getRemoteAddress(), original.getRemoteAddress());
		assertEquals(copy.getReturnMessageOnError(), original.getReturnMessageOnError());
	}

	@Test
	public void testRelease() throws XMLStreamException, CAPException {
		
		GlobalTitle orgGt = new GlobalTitle0100Impl("1234",0, BCDEvenEncodingScheme.INSTANCE,NumberingPlan.ISDN_MOBILE, NatureOfAddress.NATIONAL);
		GlobalTitle dstGt = new GlobalTitle0100Impl("5467",0, BCDEvenEncodingScheme.INSTANCE,NumberingPlan.ISDN_MOBILE, NatureOfAddress.NATIONAL);
		SccpAddress orgAddress = new SccpAddressImpl(RoutingIndicator.ROUTING_BASED_ON_GLOBAL_TITLE, orgGt, 1, 8);
		SccpAddress dstAddress = new SccpAddressImpl(RoutingIndicator.ROUTING_BASED_ON_DPC_AND_SSN, dstGt, 2, 8);

		XmlCAPDialog original = new XmlCAPDialog(CAPApplicationContext.CapV2_gsmSSF_to_gsmSCF, orgAddress, dstAddress,
				12l, 13l);

		CauseCapImpl cause = new CauseCapImpl(new byte[] { (byte) 132, (byte) 144 });
		ReleaseCallRequestImpl elem = new ReleaseCallRequestImpl(cause);

		original.addCAPMessage(elem);

		byte[] serializedEvent = this.factory.serialize(original);
		System.out.println(new String(serializedEvent));

		XmlCAPDialog copy = this.factory.deserialize(serializedEvent);

		assertNotNull(copy);

		assertEquals(copy.getApplicationContext(), original.getApplicationContext());
		assertEquals(copy.getLocalDialogId(), original.getLocalDialogId());
		assertEquals(copy.getRemoteDialogId(), original.getRemoteDialogId());
		assertEquals(copy.getLocalAddress(), original.getLocalAddress());
	}

//	@Test
//	public void testDeserializeContinue() throws XMLStreamException, CAPException {
//		String continuereq = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><dialog appCntx=\"CapV2_gsmSSF_to_gsmSCF\" localId=\"8\" remoteId=\"2126851\" capMessagesSize=\"1\" prearrangedEnd=\"false\" returnMessageOnError=\"false\"><origAddress pc=\"11161\" ssn=\"146\"><ai value=\"19\"/><gt type=\"org.mobicents.protocols.ss7.sccp.parameter.GT0100\" tt=\"0\" es=\"1\" np=\"1\" nai=\"4\" digits=\"93781090614\"/></origAddress><destAddress pc=\"0\" ssn=\"146\"><ai value=\"18\"/><gt type=\"org.mobicents.protocols.ss7.sccp.parameter.GT0100\" tt=\"0\" es=\"1\" np=\"1\" nai=\"4\" digits=\"93781090102\"/></destAddress><errComponents/><continue_Request invokeId=\"1\"/></dialog>";
//
//		XmlCAPDialog copy = this.factory.deserialize(continuereq.getBytes());
//	}

//	@Test
//	public void testDeserializeConnect() throws XMLStreamException, CAPException {
//		String continuereq = "<dialog appCntx=\"CapV2_gsmSSF_to_gsmSCF\" capMessagesSize=\"3\" networkId=\"0\" localId=\"1\" redirectRequest=\"false\" remoteId=\"16481\" returnMessageOnError=\"false\"><localAddress pc=\"797\" ssn=\"146\"><ai value=\"19\" /><gt digits=\"34602200008\" es=\"1\" nai=\"4\" np=\"1\" tt=\"0\" type=\"GlobalTitle0100\" /></localAddress><remoteAddress pc=\"0\" ssn=\"146\"><ai value=\"18\" /><gt digits=\"34602200005\" es=\"1\" nai=\"4\" np=\"1\" tt=\"0\" type=\"GlobalTitle0100\" /></remoteAddress><errComponents /><applyCharging_Request invokeId=\"1\"><aChBillingChargingCharacteristics><maxCallPeriodDuration value=\"3000\" /><releaseIfdurationExceeded value=\"false\" /></aChBillingChargingCharacteristics><partyToCharge><sendingSideID value=\"1\" /></partyToCharge></applyCharging_Request><requestReportBCSMEvent_Request invokeId=\"2\"><bcsmEventList><bcsmEvent><eventTypeBCSM value=\"oCalledPartyBusy\" /><monitorMode value=\"interrupted\" /><legID sendingSideID=\"leg2\" /></bcsmEvent><bcsmEvent><eventTypeBCSM value=\"oNoAnswer\" /><monitorMode value=\"interrupted\" /><legID sendingSideID=\"leg2\" /></bcsmEvent><bcsmEvent><eventTypeBCSM value=\"oAnswer\" /><monitorMode value=\"notifyAndContinue\" /><legID sendingSideID=\"leg2\" /></bcsmEvent><bcsmEvent><eventTypeBCSM value=\"oAbandon\" /><monitorMode value=\"notifyAndContinue\" /><legID sendingSideID=\"leg1\" /></bcsmEvent><bcsmEvent><eventTypeBCSM value=\"oDisconnect\" /><monitorMode value=\"interrupted\" /><legID sendingSideID=\"leg1\" /></bcsmEvent><bcsmEvent><eventTypeBCSM value=\"oDisconnect\" /><monitorMode value=\"interrupted\" /><legID sendingSideID=\"leg2\" /></bcsmEvent></bcsmEventList></requestReportBCSMEvent_Request><connect_Request invokeId=\"3\"><destinationRoutingAddress><calledPartyNumberList><calledPartyNumber><isupCalledPartyNumber address=\"cbb3629340433\" internalNetworkNumberIndicator=\"0\" natureOfAddresIndicator=\"3\" numberingPlanIndicator=\"1\" /></calledPartyNumber></calledPartyNumberList></destinationRoutingAddress><originalCalledPartyID><isupOriginalCalledNumber address=\"cbb3629340433\" addressRepresentationRestrictedIndicator=\"1\" natureOfAddresIndicator=\"3\" numberingPlanIndicator=\"1\" /></originalCalledPartyID><callingPartysCategory><isupCallingPartysCategory callingPartyCategory=\"10\" /></callingPartysCategory><redirectingPartyID><isupRedirectingNumber address=\"cbb3629340433\" addressRepresentationRestrictedIndicator=\"1\" natureOfAddresIndicator=\"3\" numberingPlanIndicator=\"1\" /></redirectingPartyID></connect_Request></dialog>";
//		// String continuereq =
//		// "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><dialog appCntx=\"CapV2_gsmSSF_to_gsmSCF\" localId=\"1\" remoteId=\"1504012\" capMessagesSize=\"1\" prearrangedEnd=\"false\"><origAddress pc=\"11161\" ssn=\"146\"><ai value=\"67\"/></origAddress><destAddress pc=\"0\" ssn=\"146\"><ai value=\"67\"/></destAddress><errComponents/><connect_Request invokeId=\"1\"><destinationRoutingAddress><calledPartyNumberList><calledPartyNumber><isupCalledPartyNumber address=\"923335340951\" natureOfAddresIndicator=\"2\" numberingPlanIndicator=\"1\" internalNetworkNumberIndicator=\"0\"/> </calledPartyNumber></calledPartyNumberList></destinationRoutingAddress><originalCalledPartyID><isupOriginalCalledNumber address=\"\" natureOfAddresIndicator=\"\" numberingPlanIndicator=\"\" addressRepresentationRestrictedIndicator=\"\"/></originalCalledPartyID><callingPartysCategory><isupCallingPartysCategory callingPartyCategory=\"10\"/> </callingPartysCategory><redirectingPartyID><isupRedirectingNumber address=\"\" natureOfAddresIndicator=\"\" numberingPlanIndicator=\"\" addressRepresentationRestrictedIndicator=\"\"/></redirectingPartyID><redirectionInformation><isupRedirectionInformation redirectingIndicator=\"3\" originalRedirectionReason=\"0\" redirectionCounter=\"1\" redirectionReason=\"6\"/></redirectionInformation></connect_Request></dialog>";
//		XmlCAPDialog copy = this.factory.deserialize(continuereq.getBytes());
//		
//		System.out.println(copy);
//
//	}
//	
//	@Test
//	public void testDeserialize31736() throws XMLStreamException, CAPException {
//		String continuereq = "<dialog appCntx=\"CapV2_gsmSSF_to_gsmSCF\" capMessagesSize=\"3\" networkId=\"0\" localId=\"19593\" redirectRequest=\"false\" remoteId=\"587203285\" returnMessageOnError=\"false\"><origAddress pc=\"785\" ssn=\"146\"><ai value=\"19\" /><gt digits=\"48790998147\" es=\"1\" nai=\"4\" np=\"1\" tt=\"0\" type=\"GlobalTitle0100\" />	</origAddress>	<destAddress pc=\"0\" ssn=\"146\"><ai value=\"18\" /><gt digits=\"41794945000\" es=\"1\" nai=\"4\" np=\"1\" tt=\"0\" type=\"GlobalTitle0100\" />	</destAddress>	<errComponents />	<applyCharging_Request invokeId=\"1\"><aChBillingChargingCharacteristics><maxCallPeriodDuration value=\"3000\" /><releaseIfdurationExceeded value=\"false\" /></aChBillingChargingCharacteristics><partyToCharge><sendingSideID value=\"1\" /></partyToCharge></applyCharging_Request><requestReportBCSMEvent_Request invokeId=\"2\"><bcsmEventList><bcsmEvent><eventTypeBCSM value=\"oCalledPartyBusy\" /><monitorMode value=\"interrupted\" /><legID sendingSideID=\"leg2\" /></bcsmEvent><bcsmEvent><eventTypeBCSM value=\"oNoAnswer\" /><monitorMode value=\"interrupted\" /><legID sendingSideID=\"leg2\" /></bcsmEvent><bcsmEvent><eventTypeBCSM value=\"oAnswer\" /><monitorMode value=\"notifyAndContinue\" /><legID sendingSideID=\"leg2\" /></bcsmEvent><bcsmEvent><eventTypeBCSM value=\"oAbandon\" /><monitorMode value=\"notifyAndContinue\" /><legID sendingSideID=\"leg1\" /></bcsmEvent><bcsmEvent><eventTypeBCSM value=\"oDisconnect\" /><monitorMode value=\"interrupted\" /><legID sendingSideID=\"leg1\" /></bcsmEvent><bcsmEvent><eventTypeBCSM value=\"oDisconnect\" /><monitorMode value=\"interrupted\" /><legID sendingSideID=\"leg2\" /></bcsmEvent></bcsmEventList></requestReportBCSMEvent_Request><connect_Request invokeId=\"3\"><destinationRoutingAddress><calledPartyNumberList><calledPartyNumber><isupCalledPartyNumber address=\"48732485749\" internalNetworkNumberIndicator=\"0\" natureOfAddresIndicator=\"None\" numberingPlanIndicator=\"1\" /></calledPartyNumber></calledPartyNumberList></destinationRoutingAddress><originalCalledPartyID><isupOriginalCalledNumber address=\"48732485749\" addressRepresentationRestrictedIndicator=\"1\" natureOfAddresIndicator=\"None\" numberingPlanIndicator=\"1\" /></originalCalledPartyID><callingPartysCategory><isupCallingPartysCategory callingPartyCategory=\"10\" /></callingPartysCategory><redirectingPartyID><isupRedirectingNumber address=\"48732485749\" addressRepresentationRestrictedIndicator=\"1\" natureOfAddresIndicator=\"None\" numberingPlanIndicator=\"1\" /></redirectingPartyID></connect_Request></dialog>";
//		// String continuereq =
//		// "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><dialog appCntx=\"CapV2_gsmSSF_to_gsmSCF\" localId=\"1\" remoteId=\"1504012\" capMessagesSize=\"1\" prearrangedEnd=\"false\"><origAddress pc=\"11161\" ssn=\"146\"><ai value=\"67\"/></origAddress><destAddress pc=\"0\" ssn=\"146\"><ai value=\"67\"/></destAddress><errComponents/><connect_Request invokeId=\"1\"><destinationRoutingAddress><calledPartyNumberList><calledPartyNumber><isupCalledPartyNumber address=\"923335340951\" natureOfAddresIndicator=\"2\" numberingPlanIndicator=\"1\" internalNetworkNumberIndicator=\"0\"/> </calledPartyNumber></calledPartyNumberList></destinationRoutingAddress><originalCalledPartyID><isupOriginalCalledNumber address=\"\" natureOfAddresIndicator=\"\" numberingPlanIndicator=\"\" addressRepresentationRestrictedIndicator=\"\"/></originalCalledPartyID><callingPartysCategory><isupCallingPartysCategory callingPartyCategory=\"10\"/> </callingPartysCategory><redirectingPartyID><isupRedirectingNumber address=\"\" natureOfAddresIndicator=\"\" numberingPlanIndicator=\"\" addressRepresentationRestrictedIndicator=\"\"/></redirectingPartyID><redirectionInformation><isupRedirectionInformation redirectingIndicator=\"3\" originalRedirectionReason=\"0\" redirectionCounter=\"1\" redirectionReason=\"6\"/></redirectionInformation></connect_Request></dialog>";
//		XmlCAPDialog copy = this.factory.deserialize(continuereq.getBytes());
//		
//		System.out.println(copy);
//
//	}	

//    @Test
//    public void testDeserialize31736() throws Exception {
//        File file = new File( "E:\\01\\aaa.txt" );
//
//        BufferedReader br = new BufferedReader (
//            new InputStreamReader(
//                new FileInputStream( file ), "UTF-8"
//            )
//        );
//        String line = null;
//        StringBuilder sb = new StringBuilder();
//        while ((line = br.readLine()) != null) {
//            sb.append(line);
//            sb.append("\n");
//        }
//        br.close();
//
//        String continuereq = sb.toString();
//        try {
//            XmlCAPDialog copy = this.factory.deserialize(continuereq.getBytes());
//            System.out.println(copy);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }

	public byte[] getOriginalCalledPartyID() {
		return new byte[] { -125, 20, 7, 1, 9, 0 };
	}

	public byte[] getCallingPartysCategory() {
		return new byte[] { 10 };
	}

	public byte[] getRedirectingPartyID() {
		return new byte[] { -125, 20, 7, 1, 9, 0 };
	}

	public byte[] getRedirectionInformation() {
		return new byte[] { 3, 97 };
	}
}