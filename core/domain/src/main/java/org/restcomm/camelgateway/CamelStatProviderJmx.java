/*
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

import java.util.Date;
import java.util.concurrent.TimeUnit;

import javolution.util.FastMap;

import org.apache.log4j.Logger;
import org.restcomm.applications.camel.bootstrap.Version;
import org.mobicents.protocols.ss7.oam.common.jmx.MBeanHost;
import org.mobicents.protocols.ss7.oam.common.jmx.MBeanLayer;
import org.mobicents.protocols.ss7.oam.common.jmx.MBeanType;
import org.mobicents.protocols.ss7.oam.common.statistics.CounterDefSetImpl;
import org.mobicents.protocols.ss7.oam.common.statistics.CounterDefImpl;
import org.mobicents.protocols.ss7.oam.common.statistics.SourceValueCounterImpl;
import org.mobicents.protocols.ss7.oam.common.statistics.SourceValueObjectImpl;
import org.mobicents.protocols.ss7.oam.common.statistics.SourceValueSetImpl;
import org.mobicents.protocols.ss7.oam.common.statistics.api.CounterDef;
import org.mobicents.protocols.ss7.oam.common.statistics.api.CounterDefSet;
import org.mobicents.protocols.ss7.oam.common.statistics.api.CounterMediator;
import org.mobicents.protocols.ss7.oam.common.statistics.api.CounterType;
import org.mobicents.protocols.ss7.oam.common.statistics.api.SourceValueSet;
import org.restcomm.commons.statistics.reporter.RestcommStatsReporter;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;


/**
*
* @author sergey vetyutnev
*
*/
public class CamelStatProviderJmx implements CamelStatProviderJmxMBean, CounterMediator {

    protected final Logger logger;

    private final MBeanHost ss7Management;
    private final CamelStatAggregator camelStatAggregator = CamelStatAggregator.getInstance();

    private FastMap<String, CounterDefSet> lstCounters = new FastMap<String, CounterDefSet>();

    protected static final String DEFAULT_STATISTICS_SERVER = "https://statistics.restcomm.com/rest/";

    private RestcommStatsReporter statsReporter = RestcommStatsReporter.getRestcommStatsReporter();
    private MetricRegistry metrics = RestcommStatsReporter.getMetricRegistry();
    private Counter counterDialogs = metrics.counter("calls");
    private Counter counterSeconds = metrics.counter("seconds");

    public CamelStatProviderJmx(MBeanHost ss7Management) {
        this.ss7Management = ss7Management;

        this.logger = Logger.getLogger(CamelStatProviderJmx.class.getCanonicalName() + "-" + getName());
    }

    /**
     * methods - bean life-cycle
     */

    public void start() throws Exception {
        logger.info("CamelStatProviderJmx Starting ...");

        setupCounterList();

        // TODO: take an original version from JSS7 for Ss7Layer !!!!
        this.ss7Management.registerMBean(Ss7Layer.CAMEL_GW, CamelManagementType.MANAGEMENT, this.getName(), this);

        String statisticsServer = Version.instance.getStatisticsServer();
        if (statisticsServer == null || !statisticsServer.contains("http")) {
            statisticsServer = DEFAULT_STATISTICS_SERVER;
        }
        // define remote server address (optionally)
        statsReporter.setRemoteServer(statisticsServer);

        String projectName = System.getProperty("RestcommProjectName", Version.instance.getShortName());
        String projectType = System.getProperty("RestcommProjectType", Version.instance.getProjectType());
        String projectVersion = System.getProperty("RestcommProjectVersion", Version.instance.getProjectVersion());
        logger.info("Restcomm Stats starting: " + projectName + " " + projectType + " " + projectVersion + " "
                + statisticsServer);
        statsReporter.setProjectName(projectName);
        statsReporter.setProjectType(projectType);
        statsReporter.setVersion(projectVersion);
        statsReporter.start(86400, TimeUnit.SECONDS);

        camelStatAggregator.setCounterDialogs(counterDialogs);
        camelStatAggregator.setCounterSeconds(counterSeconds);

        logger.info("CamelStatProviderJmx Started ...");
    }

    public void stop() {
        logger.info("CamelStatProviderJmx Stopping ...");

        statsReporter.stop();

        logger.info("CamelStatProviderJmx Stopped ...");
    }

    public String getName() {
        return "CAMEL";
    }

    private void setupCounterList() {
        FastMap<String, CounterDefSet> lst = new FastMap<String, CounterDefSet>();

        CounterDefSetImpl cds = new CounterDefSetImpl(this.getCounterMediatorName() + "-Main");
        lst.put(cds.getName(), cds);

//        CounterDef cd = new CounterDefImpl(CounterType.Minimal, "MinDialogsInProcess", "A min count of dialogs that are in progress during a period");
//        cds.addCounterDef(cd);
//        cd = new CounterDefImpl(CounterType.Maximal, "MaxDialogsInProcess", "A max count of dialogs that are in progress during a period");
//        cds.addCounterDef(cd);

        CounterDef cd = new CounterDefImpl(CounterType.Summary, "DialogsAllEstablished", "Dialogs successfully established all");
        cds.addCounterDef(cd);
        cd = new CounterDefImpl(CounterType.Summary_Cumulative, "DialogsAllEstablishedCumulative", "Dialogs successfully established all cumulative");
        cds.addCounterDef(cd);

        cd = new CounterDefImpl(CounterType.Summary, "SecondsAll", "Duration all successfull dialogs in seconds");
        cds.addCounterDef(cd);
        cd = new CounterDefImpl(CounterType.Summary_Cumulative, "SecondsAllCumulative", "Duration all successfull dialogs in seconds cumulative");
        cds.addCounterDef(cd);

//        cd = new CounterDefImpl(CounterType.Summary, "DialogsAllFailed", "Dialogs failed at establishing or established phases all");
//        cds.addCounterDef(cd);
//        cd = new CounterDefImpl(CounterType.Summary, "DialogsPullEstablished", "Dialogs successfully established - pull case");
//        cds.addCounterDef(cd);
//        cd = new CounterDefImpl(CounterType.Summary, "DialogsPullFailed", "Dialogs failed at establishing or established phases - pull case");
//        cds.addCounterDef(cd);
//        cd = new CounterDefImpl(CounterType.Summary, "DialogsPushEstablished", "Dialogs successfully established - push case");
//        cds.addCounterDef(cd);
//        cd = new CounterDefImpl(CounterType.Summary, "DialogsPushFailed", "Dialogs failed at establishing or established phases - push case");
//        cds.addCounterDef(cd);
//        cd = new CounterDefImpl(CounterType.Summary, "DialogsHttpEstablished", "Dialogs successfully established - Http case");
//        cds.addCounterDef(cd);
//        cd = new CounterDefImpl(CounterType.Summary, "DialogsHttpFailed", "Dialogs failed at establishing or established phases - Http case");
//        cds.addCounterDef(cd);
//        cd = new CounterDefImpl(CounterType.Summary, "DialogsSipEstablished", "Dialogs successfully established - Sip case");
//        cds.addCounterDef(cd);
//        cd = new CounterDefImpl(CounterType.Summary, "DialogsSipFailed", "Dialogs failed at establishing or established phases - Sip case");
//        cds.addCounterDef(cd);
//        cd = new CounterDefImpl(CounterType.Summary_Cumulative, "DialogsAllEstablishedCumulative", "Dialogs successfully established all cumulative");
//        cds.addCounterDef(cd);
//        cd = new CounterDefImpl(CounterType.Summary_Cumulative, "DialogsAllFailedCumulative", "Dialogs failed at establishing or established phases all cumulative");
//        cds.addCounterDef(cd);
//        cd = new CounterDefImpl(CounterType.Average, "DialogsAllEstablishedPerSec", "Dialogs successfully established all per second");
//        cds.addCounterDef(cd);
//        cd = new CounterDefImpl(CounterType.Average, "DialogsAllFailedPerSec", "Dialogs failed at establishing or established phases all per second");
//        cds.addCounterDef(cd);

        cd = new CounterDefImpl(CounterType.Summary, "MessagesRecieved", "SS7 payload Messages received by CAMEL GW");
        cds.addCounterDef(cd);
        cd = new CounterDefImpl(CounterType.Summary, "MessagesSent", "SS7 payload Messages sent by CAMEL GW");
        cds.addCounterDef(cd);
        cd = new CounterDefImpl(CounterType.Summary, "MessagesAll", "SS7 payload Messages received and sent by CAMEL");
        cds.addCounterDef(cd);
        cd = new CounterDefImpl(CounterType.Summary_Cumulative, "MessagesAllCumulative", "SS7 payload Messages received and sent by CAMEL GW cumulative");
        cds.addCounterDef(cd);

//        cd = new CounterDefImpl(CounterType.Summary, "ProcessUssdRequestOperations", "ProcessUssdRequest operations count");
//        cds.addCounterDef(cd);
//        cd = new CounterDefImpl(CounterType.Summary_Cumulative, "ProcessUssdRequestOperationsCumulative", "ProcessUssdRequest operations count cumulative");
//        cds.addCounterDef(cd);
//        cd = new CounterDefImpl(CounterType.Summary, "UssdRequestOperations", "UssdRequest operations count");
//        cds.addCounterDef(cd);
//        cd = new CounterDefImpl(CounterType.Summary_Cumulative, "UssdRequestOperationsCumulative", "UssdRequest operations count cumulative");
//        cds.addCounterDef(cd);
//        cd = new CounterDefImpl(CounterType.Summary, "UssdNotifyOperations", "UssdNotify operations count");
//        cds.addCounterDef(cd);
//        cd = new CounterDefImpl(CounterType.Summary_Cumulative, "UssdNotifyOperationsCumulative", "UssdNotify operations count cumulative");
//        cds.addCounterDef(cd);
//        cd = new CounterDefImpl(CounterType.Summary, "UssdPullNoRoutingRule", "Ussd pull requests with no configured routing rule count");
//        cds.addCounterDef(cd);
//        cd = new CounterDefImpl(CounterType.Summary_Cumulative, "UssdPullNoRoutingRuleCumulative", "Ussd pull requests with no configured routing rule count cumulative");
//        cds.addCounterDef(cd);
//
//        cds.addCounterDef(cd);
//        cd = new CounterDefImpl(CounterType.Summary, "MapErrorAbsentSubscribers", "AbsentSubscribers MAP errors count");
//        cds.addCounterDef(cd);
//        cd = new CounterDefImpl(CounterType.Summary_Cumulative, "MapErrorAbsentSubscribersCumulative", "AbsentSubscribers MAP errors count cumulative");
//        cds.addCounterDef(cd);
//        cd = new CounterDefImpl(CounterType.Summary, "MapErrorCallBarred", "CallBarred MAP errors count");
//        cds.addCounterDef(cd);
//        cd = new CounterDefImpl(CounterType.Summary_Cumulative, "MapErrorCallBarredCumulative", "CallBarred MAP errors count cumulative");
//        cds.addCounterDef(cd);
//        cd = new CounterDefImpl(CounterType.Summary, "MapErrorTeleserviceNotProvisioned", "TeleserviceNotProvisioned MAP errors count");
//        cds.addCounterDef(cd);
//        cd = new CounterDefImpl(CounterType.Summary_Cumulative, "MapErrorTeleserviceNotProvisionedCumulative", "TeleserviceNotProvisioned MAP errors count cumulative");
//        cds.addCounterDef(cd);
//        cd = new CounterDefImpl(CounterType.Summary, "MapErrorUnknownSubscriber", "UnknownSubscriber MAP errors count");
//        cds.addCounterDef(cd);
//        cd = new CounterDefImpl(CounterType.Summary_Cumulative, "MapErrorUnknownSubscriberCumulative", "UnknownSubscriber MAP errors count cumulative");
//        cds.addCounterDef(cd);
//        cd = new CounterDefImpl(CounterType.Summary, "MapErrorUssdBusy", "UssdBusy MAP errors count");
//        cds.addCounterDef(cd);
//        cd = new CounterDefImpl(CounterType.Summary_Cumulative, "MapErrorUssdBusyCumulative", "UssdBusy MAP errors count cumulative");
//        cds.addCounterDef(cd);
//        cd = new CounterDefImpl(CounterType.Summary, "MapErrorComponentOther", "ComponentOther MAP errors count");
//        cds.addCounterDef(cd);
//        cd = new CounterDefImpl(CounterType.Summary_Cumulative, "MapErrorComponentOtherCumulative", "ComponentOther MAP errors count cumulative");
//        cds.addCounterDef(cd);
//        cd = new CounterDefImpl(CounterType.Summary, "MapDialogTimeouts", "MAP DialogTimeouts count");
//        cds.addCounterDef(cd);
//        cd = new CounterDefImpl(CounterType.Summary_Cumulative, "MapDialogTimeoutsCumulative", "MAP DialogTimeouts count cumulative");
//        cds.addCounterDef(cd);
//        cd = new CounterDefImpl(CounterType.Summary, "MapInvokeTimeouts", "MAP InvokeTimeouts count");
//        cds.addCounterDef(cd);
//        cd = new CounterDefImpl(CounterType.Summary_Cumulative, "MapInvokeTimeoutsCumulative", "MAP InvokeTimeouts count cumulative");
//        cds.addCounterDef(cd);
//        cd = new CounterDefImpl(CounterType.Summary, "AppTimeouts", "Application Timeouts count");
//        cds.addCounterDef(cd);
//        cd = new CounterDefImpl(CounterType.Summary_Cumulative, "AppTimeoutsCumulative", "Application Timeouts count cumulative");
//        cds.addCounterDef(cd);
//
//        cd = new CounterDefImpl(CounterType.ComplexValue, "RequestsPerUssdCode", "USSD PULL requests count per USSD code");
//        cds.addCounterDef(cd);

        lstCounters = lst;
    }

    @Override
    public CounterDefSet getCounterDefSet(String counterDefSetName) {
        return lstCounters.get(counterDefSetName);
    }

    @Override
    public String[] getCounterDefSetList() {
        String[] res = new String[lstCounters.size()];
        lstCounters.keySet().toArray(res);
        return res;
    }

    @Override
    public String getCounterMediatorName() {
        return "CAMEL GW-" + this.getName();
    }

    @Override
    public SourceValueSet getSourceValueSet(String counterDefSetName, String campaignName, int durationInSeconds) {

        if (durationInSeconds >= 60)
            logger.info("getSourceValueSet() - starting - campaignName=" + campaignName);
        else
            logger.debug("getSourceValueSet() - starting - campaignName=" + campaignName);

        long curTimeSeconds = new Date().getTime() / 1000;

        SourceValueSetImpl svs;
        try {
            String[] csl = this.getCounterDefSetList();
            if (!csl[0].equals(counterDefSetName))
                return null;

            svs = new SourceValueSetImpl(camelStatAggregator.getSessionId());

            CounterDefSet cds = getCounterDefSet(counterDefSetName);
            for (CounterDef cd : cds.getCounterDefs()) {
                SourceValueCounterImpl scs = new SourceValueCounterImpl(cd);

                SourceValueObjectImpl svo = null;
//                if (cd.getCounterName().equals("MinDialogsInProcess")) {
//                    Long res = camelStatAggregator.getMinDialogsInProcess(campaignName);
//                    if (res != null)
//                        svo = new SourceValueObjectImpl(this.getName(), res);
//                } else if (cd.getCounterName().equals("MaxDialogsInProcess")) {
//                    Long res = camelStatAggregator.getMaxDialogsInProcess(campaignName);
//                    if (res != null)
//                        svo = new SourceValueObjectImpl(this.getName(), res);
//
//                } else 

                if (cd.getCounterName().equals("DialogsAllEstablished")) {
                    svo = new SourceValueObjectImpl(this.getName(), camelStatAggregator.getDialogsAllEstablished());
                } else if (cd.getCounterName().equals("DialogsAllEstablishedCumulative")) {
                    svo = new SourceValueObjectImpl(this.getName(), camelStatAggregator.getDialogsAllEstablishedCumulative());
                } else if (cd.getCounterName().equals("SecondsAll")) {
                    svo = new SourceValueObjectImpl(this.getName(), camelStatAggregator.getSecondsAll());
                } else if (cd.getCounterName().equals("SecondsAllCumulative")) {
                    svo = new SourceValueObjectImpl(this.getName(), camelStatAggregator.getSecondsAllCumulative());

//                } else if (cd.getCounterName().equals("DialogsAllFailed")) {
//                    svo = new SourceValueObjectImpl(this.getName(), camelStatAggregator.getDialogsAllFailed());
//                } else if (cd.getCounterName().equals("DialogsPullEstablished")) {
//                    svo = new SourceValueObjectImpl(this.getName(), camelStatAggregator.getDialogsPullEstablished());
//                } else if (cd.getCounterName().equals("DialogsPullFailed")) {
//                    svo = new SourceValueObjectImpl(this.getName(), camelStatAggregator.getDialogsPullFailed());
//                } else if (cd.getCounterName().equals("DialogsPushEstablished")) {
//                    svo = new SourceValueObjectImpl(this.getName(), camelStatAggregator.getDialogsPushEstablished());
//                } else if (cd.getCounterName().equals("DialogsPushFailed")) {
//                    svo = new SourceValueObjectImpl(this.getName(), camelStatAggregator.getDialogsPushFailed());
//                } else if (cd.getCounterName().equals("DialogsHttpEstablished")) {
//                    svo = new SourceValueObjectImpl(this.getName(), camelStatAggregator.getDialogsHttpEstablished());
//                } else if (cd.getCounterName().equals("DialogsHttpFailed")) {
//                    svo = new SourceValueObjectImpl(this.getName(), camelStatAggregator.getDialogsHttpFailed());
//                } else if (cd.getCounterName().equals("DialogsSipEstablished")) {
//                    svo = new SourceValueObjectImpl(this.getName(), camelStatAggregator.getDialogsSipEstablished());
//                } else if (cd.getCounterName().equals("DialogsSipFailed")) {
//                    svo = new SourceValueObjectImpl(this.getName(), camelStatAggregator.getDialogsSipFailed());

                } else if (cd.getCounterName().equals("MessagesRecieved")) {
                    svo = new SourceValueObjectImpl(this.getName(), camelStatAggregator.getMessagesRecieved());
                } else if (cd.getCounterName().equals("MessagesSent")) {
                    svo = new SourceValueObjectImpl(this.getName(), camelStatAggregator.getMessagesSent());
                } else if (cd.getCounterName().equals("MessagesAll")) {
                    svo = new SourceValueObjectImpl(this.getName(), camelStatAggregator.getMessagesAll());
                } else if (cd.getCounterName().equals("MessagesAllCumulative")) {
                    svo = new SourceValueObjectImpl(this.getName(), camelStatAggregator.getMessagesAllCumulative());

//                } else if (cd.getCounterName().equals("DialogsAllFailedCumulative")) {
//                    svo = new SourceValueObjectImpl(this.getName(), camelStatAggregator.getDialogsAllFailedCumulative());

                } else if (cd.getCounterName().equals("DialogsAllEstablishedPerSec")) {
                    long cnt = camelStatAggregator.getDialogsAllEstablished();
                    svo = new SourceValueObjectImpl(this.getName(), 0);
                    svo.setValueA(cnt);
                    svo.setValueB(curTimeSeconds);
//                } else if (cd.getCounterName().equals("DialogsAllFailedPerSec")) {
//                    long cnt = camelStatAggregator.getDialogsAllFailed();
//                    svo = new SourceValueObjectImpl(this.getName(), 0);
//                    svo.setValueA(cnt);
//                    svo.setValueB(curTimeSeconds);


//                } else if (cd.getCounterName().equals("ProcessUssdRequestOperations")) {
//                    svo = new SourceValueObjectImpl(this.getName(), camelStatAggregator.getProcessUssdRequestOperations());
//                } else if (cd.getCounterName().equals("ProcessUssdRequestOperationsCumulative")) {
//                    svo = new SourceValueObjectImpl(this.getName(), camelStatAggregator.getProcessUssdRequestOperations());
//                } else if (cd.getCounterName().equals("UssdRequestOperations")) {
//                    svo = new SourceValueObjectImpl(this.getName(), camelStatAggregator.getUssdRequestOperations());
//                } else if (cd.getCounterName().equals("UssdRequestOperationsCumulative")) {
//                    svo = new SourceValueObjectImpl(this.getName(), camelStatAggregator.getUssdRequestOperations());
//                } else if (cd.getCounterName().equals("UssdNotifyOperations")) {
//                    svo = new SourceValueObjectImpl(this.getName(), camelStatAggregator.getUssdNotifyOperations());
//                } else if (cd.getCounterName().equals("UssdNotifyOperationsCumulative")) {
//                    svo = new SourceValueObjectImpl(this.getName(), camelStatAggregator.getUssdNotifyOperations());
//                } else if (cd.getCounterName().equals("UssdPullNoRoutingRule")) {
//                    svo = new SourceValueObjectImpl(this.getName(), camelStatAggregator.getUssdPullNoRoutingRule());
//                } else if (cd.getCounterName().equals("UssdPullNoRoutingRuleCumulative")) {
//                    svo = new SourceValueObjectImpl(this.getName(), camelStatAggregator.getUssdPullNoRoutingRule());
//
//                } else if (cd.getCounterName().equals("MapErrorAbsentSubscribers")) {
//                    svo = new SourceValueObjectImpl(this.getName(), camelStatAggregator.getMapErrorAbsentSubscribers());
//                } else if (cd.getCounterName().equals("MapErrorAbsentSubscribersCumulative")) {
//                    svo = new SourceValueObjectImpl(this.getName(), camelStatAggregator.getMapErrorAbsentSubscribers());
//                } else if (cd.getCounterName().equals("MapErrorCallBarred")) {
//                    svo = new SourceValueObjectImpl(this.getName(), camelStatAggregator.getMapErrorCallBarred());
//                } else if (cd.getCounterName().equals("MapErrorCallBarredCumulative")) {
//                    svo = new SourceValueObjectImpl(this.getName(), camelStatAggregator.getMapErrorCallBarred());
//                } else if (cd.getCounterName().equals("MapErrorTeleserviceNotProvisioned")) {
//                    svo = new SourceValueObjectImpl(this.getName(), camelStatAggregator.getMapErrorTeleserviceNotProvisioned());
//                } else if (cd.getCounterName().equals("MapErrorTeleserviceNotProvisionedCumulative")) {
//                    svo = new SourceValueObjectImpl(this.getName(), camelStatAggregator.getMapErrorTeleserviceNotProvisioned());
//                } else if (cd.getCounterName().equals("MapErrorUnknownSubscriber")) {
//                    svo = new SourceValueObjectImpl(this.getName(), camelStatAggregator.getMapErrorUnknownSubscriber());
//                } else if (cd.getCounterName().equals("MapErrorUnknownSubscriberCumulative")) {
//                    svo = new SourceValueObjectImpl(this.getName(), camelStatAggregator.getMapErrorUnknownSubscriber());
//                } else if (cd.getCounterName().equals("MapErrorUssdBusy")) {
//                    svo = new SourceValueObjectImpl(this.getName(), camelStatAggregator.getMapErrorUssdBusy());
//                } else if (cd.getCounterName().equals("MapErrorUssdBusyCumulative")) {
//                    svo = new SourceValueObjectImpl(this.getName(), camelStatAggregator.getMapErrorUssdBusy());
//                } else if (cd.getCounterName().equals("MapErrorComponentOther")) {
//                    svo = new SourceValueObjectImpl(this.getName(), camelStatAggregator.getMapErrorComponentOther());
//                } else if (cd.getCounterName().equals("MapErrorComponentOtherCumulative")) {
//                    svo = new SourceValueObjectImpl(this.getName(), camelStatAggregator.getMapErrorComponentOther());
//                } else if (cd.getCounterName().equals("MapDialogTimeouts")) {
//                    svo = new SourceValueObjectImpl(this.getName(), camelStatAggregator.getMapDialogTimeouts());
//                } else if (cd.getCounterName().equals("MapDialogTimeoutsCumulative")) {
//                    svo = new SourceValueObjectImpl(this.getName(), camelStatAggregator.getMapDialogTimeouts());
//                } else if (cd.getCounterName().equals("MapInvokeTimeouts")) {
//                    svo = new SourceValueObjectImpl(this.getName(), camelStatAggregator.getMapInvokeTimeouts());
//                } else if (cd.getCounterName().equals("MapInvokeTimeoutsCumulative")) {
//                    svo = new SourceValueObjectImpl(this.getName(), camelStatAggregator.getMapInvokeTimeouts());
//                } else if (cd.getCounterName().equals("AppTimeouts")) {
//                    svo = new SourceValueObjectImpl(this.getName(), camelStatAggregator.getAppTimeouts());
//                } else if (cd.getCounterName().equals("AppTimeoutsCumulative")) {
//                    svo = new SourceValueObjectImpl(this.getName(), camelStatAggregator.getAppTimeouts());
//
//                } else if (cd.getCounterName().equals("RequestsPerUssdCode")) {
//                    svo = createComplexValue(camelStatAggregator.getRequestsPerUssdCode(campaignName));

                }

                if (svo != null)
                    scs.addObject(svo);

                svs.addCounter(scs);
            }
        } catch (Throwable e) {
            logger.info("Exception when getSourceValueSet() - campaignName=" + campaignName + " - " + e.getMessage(), e);
            return null;
        }

        if (durationInSeconds >= 60)
            logger.info("getSourceValueSet() - return value - campaignName=" + campaignName);
        else
            logger.debug("getSourceValueSet() - return value - campaignName=" + campaignName);

        return svs;
    }

//    private SourceValueObjectImpl createComplexValue(Map<String, LongValue> vv) {
//        SourceValueObjectImpl svo = null;
//        if (vv != null) {
//            svo = new SourceValueObjectImpl(this.getName(), 0);
//            ComplexValue[] vvv = new ComplexValue[vv.size()];
//            int i1 = 0;
//            for (String s : vv.keySet()) {
//                LongValue lv = vv.get(s);
//                vvv[i1++] = new ComplexValueImpl(s, lv.getValue());
//            }
//            svo.setComplexValue(vvv);
//        }
//        return svo;
//    }






    // TODO: take an original version from JSS7 !!!!
    public enum Ss7Layer implements MBeanLayer {
        CAMEL_GW("CAMEL_GW");

        private final String name;

        public static final String NAME_CAMEL_GW = "CAMEL_GW";

        private Ss7Layer(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

        public static MBeanLayer getInstance(String name) {
            if (NAME_CAMEL_GW.equals(name)) {
                return CAMEL_GW;
            }

            return null;
        }
    }

    public enum CamelManagementType implements MBeanType {
        MANAGEMENT("Management");

        private final String name;

        public static final String NAME_MANAGEMENT = "Management";

        private CamelManagementType(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

        public static CamelManagementType getInstance(String name) {
            if (NAME_MANAGEMENT.equals(name)) {
                return MANAGEMENT;
            }

            return null;
        }
    }

}
