/**
 * 
 */
package org.restcomm.camelgateway;

import java.util.Map;

import javolution.util.FastMap;
import javolution.util.FastMap.Entry;
import javolution.xml.XMLFormat;
import javolution.xml.stream.XMLStreamException;

import org.mobicents.protocols.ss7.cap.api.errors.CAPErrorMessage;
import org.mobicents.protocols.ss7.cap.api.errors.CAPErrorMessageCancelFailed;
import org.mobicents.protocols.ss7.cap.api.errors.CAPErrorMessageParameterless;
import org.mobicents.protocols.ss7.cap.api.errors.CAPErrorMessageRequestedInfoError;
import org.mobicents.protocols.ss7.cap.api.errors.CAPErrorMessageSystemFailure;
import org.mobicents.protocols.ss7.cap.api.errors.CAPErrorMessageTaskRefused;

/**
 * @author Amit Bhayani
 * @author sergey vetyutnev
 *
 */
public class ErrorComponentCap {

    public static final String CAP_ERROR_MESSAGE_CANCEL_FAILED = CAPErrorMessageCancelFailed.class.getSimpleName();
    public static final String CAP_ERROR_MESSAGE_PARAMETERLESS = CAPErrorMessageParameterless.class.getSimpleName();
    public static final String CAP_ERROR_MESSAGE_REQUESTED_INFO_ERROR = CAPErrorMessageRequestedInfoError.class.getSimpleName();
    public static final String CAP_ERROR_MESSAGE_SYSTEM_FAILURE = CAPErrorMessageSystemFailure.class.getSimpleName();
    public static final String CAP_ERROR_MESSAGE_TASK_REFUSED = CAPErrorMessageTaskRefused.class.getSimpleName();

    private static final String INVOKE_ID = "invokeId";
    private static final String ERROR_COMPONENT = "errorComponent";

    private FastMap<Long, CAPErrorMessage> data = new FastMap<Long, CAPErrorMessage>();

    public void put(Long invokeId, CAPErrorMessage capErrorMessage) {
        this.data.put(invokeId, capErrorMessage);
    }

    public void clear() {
        this.data.clear();
    }

    public int size() {
        return this.data.size();
    }

    public FastMap<Long, CAPErrorMessage> getErrorComponents() {
        return data;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ErrorComponentCap=[");

        int i1 = 0;
        for (Entry<Long, CAPErrorMessage> n = data.head(), end = data.tail(); (n = n.getNext()) != end;) {
            Long id = n.getKey();
            CAPErrorMessage capErrorMessage = n.getValue();

            if (i1 == 0)
                i1 = 1;
            else
                sb.append(", ");
            sb.append("invokeId=");
            sb.append(id);
            sb.append(", capErrorMessage=");
            sb.append(capErrorMessage);
        }

        sb.append("]");
        return sb.toString();
    }

    /**
     * XML Serialization/Deserialization
     */
    protected static final XMLFormat<ErrorComponentCap> ERROR_COMPONENT_CAP_XML = new XMLFormat<ErrorComponentCap>(
            ErrorComponentCap.class) {

        @Override
        public void read(javolution.xml.XMLFormat.InputElement xml, ErrorComponentCap data) throws XMLStreamException {
            data.data.clear();

            while (xml.hasNext()) {
                Long id = xml.get(INVOKE_ID, Long.class);
                Object pe = xml.get(ERROR_COMPONENT);
                CAPErrorMessage capErrorMessage = (CAPErrorMessage) pe;
                data.data.put(id, capErrorMessage);
            }
        }

        @Override
        public void write(ErrorComponentCap data, javolution.xml.XMLFormat.OutputElement xml) throws XMLStreamException {
            if (data.data.size() > 0) {
                for (Entry<Long, CAPErrorMessage> n = data.data.head(), end = data.data.tail(); (n = n.getNext()) != end;) {
                    Long id = n.getKey();
                    CAPErrorMessage capErrorMessage = n.getValue();

                    xml.add(id, INVOKE_ID, Long.class);
                    xml.add(capErrorMessage, ERROR_COMPONENT);
                }
            }
        }
    };

}
