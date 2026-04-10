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
import it.finanze.sanita.dem.xsd.invioerogatoricevuta.InvioErogatoRicevutaDocument;
import it.finanze.sanita.dem.xsd.invioerogatorichiesta.InvioErogatoRichiestaDocument;
import it.finanze.sanita.dem.xsd.tipodati.ErroreRicettaType;

public class InvioErogatoTrace {

	private InvioErogatoRichiestaDocument mwInputBean;
	private InvioErogatoRicevutaDocument mwOutputBean;
	private SpTransactionService spTransactionService;
	private SpTransactionErrorService spTransactionErrorService;
	private Boolean traceInternalMsg;
	private static final Logger logger = LoggerFactory.getLogger(InvioErogatoTrace.class);

	public InvioErogatoTrace(XmlObject mwInputBean, XmlObject mwOutputBean, Boolean traceInternalMsg) {
		super();
		this.mwInputBean = (InvioErogatoRichiestaDocument) mwInputBean;
		this.mwOutputBean = (InvioErogatoRicevutaDocument) mwOutputBean;
		this.spTransactionErrorService = (SpTransactionErrorService) ApplicationContextProvider.getApplicationContext().getBean("spTransactionErrorService");
		this.spTransactionService = (SpTransactionService) ApplicationContextProvider.getApplicationContext().getBean("spTransactionService");
		this.traceInternalMsg = traceInternalMsg;
	}

	public void trace() {

		SpTransaction spTransactions = new SpTransaction();
		spTransactions.setTipo("demInvioErogato");
		spTransactions.setCfPaziente(StringEncodingUtils.sha256(mwInputBean.getInvioErogatoRichiesta().getCfAssistito()));
		spTransactions.setNre(mwInputBean.getInvioErogatoRichiesta().getNre());
		spTransactions.setMsgMwSrv(mwInputBean.xmlText().getBytes());
		spTransactions.setMsgSrvMw(mwOutputBean.xmlText().getBytes());
		if(traceInternalMsg){
			spTransactions.setMsgMwIn(this.mwInputBean.xmlText().getBytes());
			spTransactions.setMsgMwOut(this.mwOutputBean.xmlText().getBytes());
		}
		spTransactions.setDtInsert(new Date());
		spTransactions.setEsito(mwOutputBean.getInvioErogatoRicevuta().getCodEsitoInserimento());
		try {
			spTransactionService.save(spTransactions);
		} catch (DatabaseException e) {
			logger.error("Si è verificato un errore in InvioErogatoTrace.trace: " + e.getMessage());
		}
		if (mwOutputBean.getInvioErogatoRicevuta().getElencoErroriRicette() != null && mwOutputBean.getInvioErogatoRicevuta().getElencoErroriRicette().sizeOfErroreRicettaArray() > 0) {
			int sizeError = mwOutputBean.getInvioErogatoRicevuta().getElencoErroriRicette().sizeOfErroreRicettaArray();
			for (int i = 0; i < sizeError; i++) {
				ErroreRicettaType err = mwOutputBean.getInvioErogatoRicevuta().getElencoErroriRicette().getErroreRicettaArray(i);
				if (StringUtils.isEmpty(err.getCodEsito()) || !err.getCodEsito().equalsIgnoreCase("0000")) {
					SpTransactionError spTransactionsError = new SpTransactionError();
					spTransactionsError.setOperation("InvioErogato");
					spTransactionsError.setDtInsert(new Date());
					spTransactionsError.setOrder(new Long(i));
					spTransactionsError.setCodError(err.getCodEsito());
					spTransactionsError.setDescrError(err.getTipoErrore());
					spTransactionsError.setIdTrans(spTransactions.getId());
					try {
						spTransactionErrorService.save(spTransactionsError);
					} catch (DatabaseException e) {
						logger.error("Si è verificato un errore in InvioErogatoTrace.trace: " + e.getMessage());
					}
				}
			}
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
