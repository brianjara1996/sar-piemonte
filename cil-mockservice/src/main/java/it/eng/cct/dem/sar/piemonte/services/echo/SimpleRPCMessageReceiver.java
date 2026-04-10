package it.eng.cct.dem.sar.piemonte.services.echo;

import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.rpc.receivers.RPCMessageReceiver;

public class SimpleRPCMessageReceiver extends RPCMessageReceiver {

	@Override
	public void invokeBusinessLogic(MessageContext inMessage, MessageContext outMessage) throws AxisFault {
		super.invokeBusinessLogic(inMessage, outMessage);
	}

	@Override
	public void receive(MessageContext messageCtx) throws AxisFault {
		messageCtx.setProperty(AddressingConstants.ADDR_VALIDATE_ACTION, "false");
		super.receive(messageCtx);
	}

	@Override
	public SOAPFactory getSOAPFactory(MessageContext msgContext) throws AxisFault {
		return super.getSOAPFactory(msgContext);
	}

}
