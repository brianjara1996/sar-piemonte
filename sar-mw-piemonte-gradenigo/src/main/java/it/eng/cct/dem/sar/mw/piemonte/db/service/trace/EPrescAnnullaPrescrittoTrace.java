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
import it.eng.cct.piemonte.eprescription.types.AnnullamentoDocument;
import it.finanze.sanita.dem.xsd.annullaprescrittoricevuta.AnnullaPrescrittoRicevutaDocument;
import it.finanze.sanita.dem.xsd.annullaprescrittorichiesta.AnnullaPrescrittoRichiestaDocument;
import it.finanze.sanita.dem.xsd.tipodati.ErroreRicettaType;

public class EPrescAnnullaPrescrittoTrace {

	private AnnullamentoDocument mwInputBean;
	private AnnullamentoDocument mwOutputBean;
	private SpTransactionService spTransactionService;
	private SpTransactionErrorService spTransactionErrorService;
	private AnnullaPrescrittoRichiestaDocument mwToServiceBean;
	private AnnullaPrescrittoRicevutaDocument serviceToMwBean;
	private Boolean traceInternalMsg;
	private static final Logger logger = LoggerFactory.getLogger(EPrescAnnullaPrescrittoTrace.class);

	private static final String SERVICE_NAME = "demAnnullaPrescritto";

	public EPrescAnnullaPrescrittoTrace(XmlObject mwInputBean, XmlObject mwOutputBean, XmlObject mwToServiceBean, XmlObject serviceToMwBean, Boolean traceInternalMsg) {
		super();
		this.mwInputBean = (AnnullamentoDocument) mwInputBean;
		this.mwOutputBean = (AnnullamentoDocument) mwOutputBean;
		this.mwToServiceBean = (AnnullaPrescrittoRichiestaDocument) mwToServiceBean;
		this.serviceToMwBean = (AnnullaPrescrittoRicevutaDocument) serviceToMwBean;
		this.spTransactionErrorService = (SpTransactionErrorService) ApplicationContextProvider.getApplicationContext().getBean("spTransactionErrorService");
		this.spTransactionService = (SpTransactionService) ApplicationContextProvider.getApplicationContext().getBean("spTransactionService");
		this.traceInternalMsg = traceInternalMsg;
	}

	public void trace() {
		SpTransaction spTransactions = new SpTransaction();

		spTransactions.setTipo(SERVICE_NAME);

		spTransactions.setCfMedico(this.mwToServiceBean.getAnnullaPrescrittoRichiesta().getCfMedico());
		spTransactions.setNre(this.mwToServiceBean.getAnnullaPrescrittoRichiesta().getNre());
		spTransactions.setMsgMwSrv(this.mwToServiceBean.xmlText().getBytes());
		spTransactions.setMsgSrvMw(this.serviceToMwBean.xmlText().getBytes());
		if(traceInternalMsg){
			spTransactions.setMsgMwIn(mwInputBean.xmlText().getBytes());
			spTransactions.setMsgMwOut(mwOutputBean.xmlText().getBytes());
		}
		spTransactions.setDtInsert(new Date());

		spTransactions.setEsito(this.serviceToMwBean.getAnnullaPrescrittoRicevuta().getCodEsitoAnnullamento());

		if (this.serviceToMwBean.getAnnullaPrescrittoRicevuta().getElencoErroriRicette() != null && this.serviceToMwBean.getAnnullaPrescrittoRicevuta().getElencoErroriRicette().sizeOfErroreRicettaArray() > 0) {
			int sizeError = this.serviceToMwBean.getAnnullaPrescrittoRicevuta().getElencoErroriRicette().sizeOfErroreRicettaArray();
			for (int i = 0; i < sizeError; i++) {
				ErroreRicettaType err = this.serviceToMwBean.getAnnullaPrescrittoRicevuta().getElencoErroriRicette().getErroreRicettaArray(i);
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
						logger.error("Si è verificato un errore in EPrescAnnullaPrescritto.trace: " + e.getMessage());
					}
				}
			}
		}
		try {
			spTransactionService.save(spTransactions);
		} catch (DatabaseException e) {
			logger.error("Si è verificato un errore in EPrescAnnullaPrescritto.trace: " + e.getMessage());
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
