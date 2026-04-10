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
import it.finanze.sanita.dem.xsd.sospendierogatoricevuta.SospendiErogatoRicevutaDocument;
import it.finanze.sanita.dem.xsd.sospendierogatorichiesta.SospendiErogatoRichiestaDocument;
import it.finanze.sanita.dem.xsd.tipodati.ErroreRicettaType;

public class SospendiErogatoTrace {

	private SospendiErogatoRichiestaDocument mwInputBean;
	private SospendiErogatoRicevutaDocument mwOutputBean;
	private SpTransactionService spTransactionService;
	private SpTransactionErrorService spTransactionErrorService;
	private Boolean traceInternalMsg;
	private static final Logger logger = LoggerFactory.getLogger(SospendiErogatoTrace.class);

	private static final String SERVICE_NAME = "demSospendiErogato";
	
	public SospendiErogatoTrace(XmlObject mwInputBean, XmlObject mwOutputBean, Boolean traceInternalMsg) {
		super();
		this.mwInputBean = (SospendiErogatoRichiestaDocument) mwInputBean;
		this.mwOutputBean = (SospendiErogatoRicevutaDocument) mwOutputBean;
		this.spTransactionErrorService = (SpTransactionErrorService) ApplicationContextProvider.getApplicationContext().getBean("spTransactionErrorService");
		this.spTransactionService = (SpTransactionService) ApplicationContextProvider.getApplicationContext().getBean("spTransactionService");
		this.traceInternalMsg = traceInternalMsg;
	}

	public void trace() {
		SpTransaction spTransactions = new SpTransaction();

		spTransactions.setTipo(SERVICE_NAME);
		spTransactions.setCfPaziente(StringEncodingUtils.sha256(mwInputBean.getSospendiErogatoRichiesta().getCfAssistito()));
		spTransactions.setNre(mwInputBean.getSospendiErogatoRichiesta().getNre());
		spTransactions.setMsgMwSrv(mwInputBean.xmlText().getBytes());
		spTransactions.setMsgSrvMw(mwOutputBean.xmlText().getBytes());
		if(traceInternalMsg){
			spTransactions.setMsgMwIn(this.mwInputBean.xmlText().getBytes());
			spTransactions.setMsgMwOut(this.mwOutputBean.xmlText().getBytes());
		}
		spTransactions.setDtInsert(new Date());
		spTransactions.setEsito(mwOutputBean.getSospendiErogatoRicevuta().getCodEsitoSospensione());
		try {
			spTransactionService.save(spTransactions);
		} catch (DatabaseException e) {
			logger.error("Si è verificato un errore in SospendiErogatoTrace.trace: " + e.getMessage());
		}
		if (mwOutputBean.getSospendiErogatoRicevuta().getElencoErroriRicette() != null && mwOutputBean.getSospendiErogatoRicevuta().getElencoErroriRicette().sizeOfErroreRicettaArray() > 0) {
			int sizeError = mwOutputBean.getSospendiErogatoRicevuta().getElencoErroriRicette().sizeOfErroreRicettaArray();
			for (int i = 0; i < sizeError; i++) {
				ErroreRicettaType err = mwOutputBean.getSospendiErogatoRicevuta().getElencoErroriRicette().getErroreRicettaArray(i);
				if (StringUtils.isEmpty(err.getCodEsito()) || !err.getCodEsito().equalsIgnoreCase("0000")) {
					SpTransactionError spTransactionsError = new SpTransactionError();
					spTransactionsError.setOperation("SospendiErogato");
					spTransactionsError.setDtInsert(new Date());
					spTransactionsError.setOrder(new Long(i));
					spTransactionsError.setCodError(err.getCodEsito());
					spTransactionsError.setDescrError(err.getTipoErrore());
					spTransactionsError.setIdTrans(spTransactions.getId());
					try {
						spTransactionErrorService.save(spTransactionsError);
					} catch (DatabaseException e) {
						logger.error("Si è verificato un errore in SospendiErogatoTrace.trace: " + e.getMessage());
					}
				}
			}
		}
	}
}
