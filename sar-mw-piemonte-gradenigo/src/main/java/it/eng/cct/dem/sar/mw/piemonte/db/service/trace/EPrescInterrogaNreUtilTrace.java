package it.eng.cct.dem.sar.mw.piemonte.db.service.trace;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.xmlbeans.XmlObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.cct.dem.sar.mw.piemonte.db.model.SpTransaction;
import it.eng.cct.dem.sar.mw.piemonte.db.model.SpTransactionError;
import it.eng.cct.dem.sar.mw.piemonte.db.service.SpTransactionErrorService;
import it.eng.cct.dem.sar.mw.piemonte.db.service.SpTransactionService;
import it.eng.cct.dem.sar.mw.piemonte.exception.DatabaseException;
import it.eng.cct.dem.sar.mw.spring.ApplicationContextProvider;
import it.eng.cct.dem.sar.mw.utils.StringEncodingUtils;
import it.eng.cct.piemonte.eprescription.types.InterrogaDocument;
import it.finanze.sanita.dem.xsd.interroganreutilricevuta.InterrogaNreUtilRicevutaDocument;
import it.finanze.sanita.dem.xsd.interroganreutilrichiesta.InterrogaNreUtilRichiestaDocument;
import it.finanze.sanita.dem.xsd.tipodati.ErroreRicettaType;

public class EPrescInterrogaNreUtilTrace {

	private InterrogaDocument mwInputBean;
	private InterrogaDocument mwOutputBean;
	private SpTransactionService spTransactionService;
	private SpTransactionErrorService spTransactionErrorService;
	private InterrogaNreUtilRichiestaDocument mwToServiceBean;
	private InterrogaNreUtilRicevutaDocument serviceToMwBean;
	private Boolean traceInternalMsg;
	private static final Logger logger = LoggerFactory.getLogger(EPrescInterrogaNreUtilTrace.class);

	private static final String SERVICE_NAME = "InterrogaNreUtil";

	public EPrescInterrogaNreUtilTrace(XmlObject mwInputBean, XmlObject mwOutputBean, XmlObject mwToServiceBean, XmlObject serviceToMwBean, Boolean traceInternalMsg) {
		super();
		this.mwInputBean = (InterrogaDocument) mwInputBean;
		this.mwOutputBean = (InterrogaDocument) mwOutputBean;
		this.mwToServiceBean = (InterrogaNreUtilRichiestaDocument) mwToServiceBean;
		this.serviceToMwBean = (InterrogaNreUtilRicevutaDocument) serviceToMwBean;
		this.spTransactionErrorService = (SpTransactionErrorService) ApplicationContextProvider.getApplicationContext().getBean("spTransactionErrorService");
		this.spTransactionService = (SpTransactionService) ApplicationContextProvider.getApplicationContext().getBean("spTransactionService");
		this.traceInternalMsg = traceInternalMsg;
	}

	public void trace() {
		SpTransaction spTransactions = new SpTransaction();

		spTransactions.setTipo(SERVICE_NAME);

		spTransactions.setCfMedico(this.mwToServiceBean.getInterrogaNreUtilRichiesta().getCfMedico());
		spTransactions.setCfPaziente(StringEncodingUtils.sha256(this.mwToServiceBean.getInterrogaNreUtilRichiesta().getCfAssistito()));
		spTransactions.setNre(this.mwToServiceBean.getInterrogaNreUtilRichiesta().getNre());
		spTransactions.setMsgMwSrv(this.mwToServiceBean.xmlText().getBytes());
		spTransactions.setMsgSrvMw(this.serviceToMwBean.xmlText().getBytes());
		if(traceInternalMsg){
			spTransactions.setMsgMwIn(this.mwInputBean.xmlText().getBytes());
			spTransactions.setMsgMwOut(this.mwOutputBean.xmlText().getBytes());
		}
		spTransactions.setDtInsert(new Date());

		spTransactions.setEsito(this.serviceToMwBean.getInterrogaNreUtilRicevuta().getCodEsitoInterrogaNreUtilizzati());

		if (this.serviceToMwBean.getInterrogaNreUtilRicevuta().getElencoErroriRicette() != null && this.serviceToMwBean.getInterrogaNreUtilRicevuta().getElencoErroriRicette().sizeOfErroreRicettaArray() > 0) {
			int sizeError = this.serviceToMwBean.getInterrogaNreUtilRicevuta().getElencoErroriRicette().sizeOfErroreRicettaArray();
			for (int i = 0; i < sizeError; i++) {
				ErroreRicettaType err = this.serviceToMwBean.getInterrogaNreUtilRicevuta().getElencoErroriRicette().getErroreRicettaArray(i);
				if (StringUtils.isEmpty(err.getCodEsito()) || !err.getCodEsito().equalsIgnoreCase("0000")) {
					SpTransactionError spTransactionsError = new SpTransactionError();
					spTransactionsError.setOperation(SERVICE_NAME);
					spTransactionsError.setDtInsert(new Date());
					spTransactionsError.setOrder(new Long(i));
					spTransactionsError.setCodError(err.getCodEsito());
					spTransactionsError.setDescrError(err.getEsito());
					try {
						spTransactionErrorService.save(spTransactionsError);
					} catch (DatabaseException e) {
						logger.error("Si è verificato un errore in EPrescInterrogaNreUtilTrace.trace: " + e.getMessage());
					}
				}
			}
		}
		try {
			spTransactionService.save(spTransactions);
		} catch (DatabaseException e) {
			logger.error("Si è verificato un errore in EPrescInterrogaNreUtilTrace.trace: " + e.getMessage());
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
}
