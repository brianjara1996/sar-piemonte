package it.eng.cct.dem.sar.mw.piemonte.db.service.trace;

import org.apache.xmlbeans.XmlObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.cct.piemonte.eprescription.types.TrasmissioneDocument;

public class InvioTelematicoSanitaTrasmissione extends InvioTelematicoSanita{

	private TrasmissioneDocument mwInputBean;
	private TrasmissioneDocument mwOutputBean;
	private static final Logger logger = LoggerFactory.getLogger(InvioTelematicoSanitaTrasmissione.class);

	private static final String SERVICE_NAME = "InvioTelematicoSanitaTrasmissione";

	public InvioTelematicoSanitaTrasmissione(XmlObject mwInputBean, XmlObject mwOutputBean, XmlObject mwToServiceBean, XmlObject serviceToMwBean, Boolean traceInternalMsg) {
		super(mwInputBean, mwOutputBean, mwToServiceBean, serviceToMwBean, traceInternalMsg);
		super.setService_name(SERVICE_NAME);
		this.mwInputBean = (TrasmissioneDocument) mwInputBean;
		this.mwOutputBean = (TrasmissioneDocument) mwOutputBean;
	}

	@Override
	public byte[] getXmlTxtIn() {
		return this.mwInputBean.xmlText().getBytes();
	}

	@Override
	public byte[] getXmlTxtOut() {
		return this.mwOutputBean.xmlText().getBytes();
	}

}
