package it.eng.cct.dem.sar.mw.piemonte.db.service.trace;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.xmlbeans.XmlObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.cct.dem.sar.mw.piemonte.db.model.SpTransaction;
import it.eng.cct.dem.sar.mw.piemonte.db.model.SpTransactionError;
import it.eng.cct.dem.sar.mw.piemonte.db.service.SpTransactionErrorService;
import it.eng.cct.dem.sar.mw.piemonte.db.service.SpTransactionService;
import it.eng.cct.dem.sar.mw.piemonte.exception.DatabaseException;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TEPrescription;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TEPrescriptions;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.impl.EPrescriptionsDocumentImpl;
import it.eng.cct.dem.sar.mw.spring.ApplicationContextProvider;
import it.finanze.sanita.dem.dpcm.wsdl.invioTelematicoSanita.service.InputBeanDocument;
import it.finanze.sanita.dem.dpcm.wsdl.invioTelematicoSanita.service.RicevutaDocument;

public abstract class InvioTelematicoSanita {

	private SpTransactionService spTransactionService;
	private SpTransactionErrorService spTransactionErrorService;
	private InputBeanDocument mwToServiceBean;
	private RicevutaDocument serviceToMwBean;
	private EPrescriptionsDocumentImpl serviceToMwErrBean;
	private Boolean traceInternalMsg;
	private static final Logger logger = LoggerFactory.getLogger(InvioTelematicoSanita.class);

	private String service_name = "InvioTelematicoSanitaAnnullamento";

	public InvioTelematicoSanita(XmlObject mwInputBean, XmlObject mwOutputBean, XmlObject mwToServiceBean, XmlObject serviceToMwBean,Boolean traceInternalMsg) {
		super();
		this.mwToServiceBean = (InputBeanDocument) mwToServiceBean;
		if (serviceToMwBean instanceof EPrescriptionsDocumentImpl)
			this.serviceToMwErrBean = (EPrescriptionsDocumentImpl) serviceToMwBean;
		if (serviceToMwBean instanceof RicevutaDocument)
			this.serviceToMwBean = (RicevutaDocument) serviceToMwBean;
		this.spTransactionErrorService = (SpTransactionErrorService) ApplicationContextProvider.getApplicationContext().getBean("spTransactionErrorService");
		this.spTransactionService = (SpTransactionService) ApplicationContextProvider.getApplicationContext().getBean("spTransactionService");
		this.traceInternalMsg = traceInternalMsg;
	}

	public void trace() {
		SpTransaction spTransactions = new SpTransaction();
		List<SpTransactionError> transErrorList = new ArrayList<SpTransactionError>();

		spTransactions.setTipo(this.service_name);
		
		if(traceInternalMsg){
			spTransactions.setMsgMwIn(this.getXmlTxtIn());
		}
		spTransactions.setMsgMwSrv(this.getXmlTxtOut());

		spTransactions.setDtInsert(new Date());

		if (this.serviceToMwBean != null) {
			spTransactions.setMsgSrvMw(this.serviceToMwBean.xmlText().getBytes());
			spTransactions.setEsito(this.serviceToMwBean.getRicevuta().getCodiceEsito());
			if (StringUtils.isNotEmpty(this.serviceToMwBean.getRicevuta().getProtocolloSAC()))
				spTransactions.setProtocolloSac(this.serviceToMwBean.getRicevuta().getProtocolloSAC());
		} else if (this.serviceToMwErrBean != null) {
			spTransactions.setMsgSrvMw(this.serviceToMwErrBean.xmlText().getBytes());
			TEPrescriptions x = this.serviceToMwErrBean.getEPrescriptions();
			if (x.getEPrescriptionArray() != null && x.getEPrescriptionArray().length > 0) {
				for (int i = 0; i < x.getEPrescriptionArray().length; i++) {
					TEPrescription e = x.getEPrescriptionArray()[i];
					SpTransactionError te = new SpTransactionError();
					te.setCodError(e.getEsito());
					te.setOrder(new Long(i));
					te.setOperation(this.service_name);
					te.setDtInsert(new Date());
					transErrorList.add(te);
				}
			}
		}
		try {
			spTransactionService.save(spTransactions);
			if (transErrorList.size() > 0) {
				for (SpTransactionError spTransactionError : transErrorList) {
					spTransactionError.setIdTrans(spTransactions.getId());
					spTransactionErrorService.save(spTransactionError);
				}
			}
		} catch (DatabaseException e) {
			logger.error("Si è verificato un errore in EPrescInvioTelematicoPrescrittoTrace.trace: " + e.getMessage());
		}
	}

	public SpTransactionService getSpTransactionService() {
		return spTransactionService;
	}

	public void setSpTransactionService(SpTransactionService spTransactionService) {
		this.spTransactionService = spTransactionService;
	}

	public SpTransactionErrorService getSpTransactionErrorService() {
		return spTransactionErrorService;
	}

	public void setSpTransactionErrorService(SpTransactionErrorService spTransactionErrorService) {
		this.spTransactionErrorService = spTransactionErrorService;
	}
	
	public abstract byte[] getXmlTxtIn();
	
	public abstract byte[] getXmlTxtOut();

	public String getService_name() {
		return service_name;
	}

	public void setService_name(String service_name) {
		this.service_name = service_name;
	}
}
