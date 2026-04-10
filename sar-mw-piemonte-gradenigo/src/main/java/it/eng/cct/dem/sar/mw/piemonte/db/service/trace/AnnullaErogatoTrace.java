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
import it.finanze.sanita.dem.xsd.annullaerogatoricevuta.AnnullaErogatoRicevutaDocument;
import it.finanze.sanita.dem.xsd.annullaerogatorichiesta.AnnullaErogatoRichiestaDocument;
import it.finanze.sanita.dem.xsd.tipodati.ErroreRicettaType;

public class AnnullaErogatoTrace {

	private AnnullaErogatoRichiestaDocument mwInputBean;
	private AnnullaErogatoRicevutaDocument mwOutputBean;
	private SpTransactionService spTransactionService;
	private SpTransactionErrorService spTransactionErrorService;
	private Boolean traceInternalMsg;
	private static final Logger logger = LoggerFactory.getLogger(AnnullaErogatoTrace.class);

	private static final String SERVICE_NAME = "demAnnullaErogato";

	public AnnullaErogatoTrace(XmlObject mwInputBean, XmlObject mwOutputBean, Boolean traceInternalMsg) {
		super();
		this.mwInputBean = (AnnullaErogatoRichiestaDocument) mwInputBean;
		this.mwOutputBean = (AnnullaErogatoRicevutaDocument) mwOutputBean;
		this.spTransactionErrorService = (SpTransactionErrorService) ApplicationContextProvider.getApplicationContext().getBean("spTransactionErrorService");
		this.spTransactionService = (SpTransactionService) ApplicationContextProvider.getApplicationContext().getBean("spTransactionService");
		this.traceInternalMsg = traceInternalMsg;
	}

	public void trace() {

		SpTransaction spTransactions = new SpTransaction();

		spTransactions.setTipo(SERVICE_NAME);
		spTransactions.setCfPaziente(StringEncodingUtils.sha256(mwInputBean.getAnnullaErogatoRichiesta().getCfAssistito()));
		spTransactions.setNre(mwInputBean.getAnnullaErogatoRichiesta().getNre());
		
		spTransactions.setMsgMwSrv(mwInputBean.xmlText().getBytes());
		spTransactions.setMsgSrvMw(mwOutputBean.xmlText().getBytes());
		if(traceInternalMsg){
			spTransactions.setMsgMwIn(mwInputBean.xmlText().getBytes());
			spTransactions.setMsgMwOut(mwOutputBean.xmlText().getBytes());
		}
		spTransactions.setDtInsert(new Date());
		spTransactions.setEsito(mwOutputBean.getAnnullaErogatoRicevuta().getCodEsitoAnnullamento());
		try {
			spTransactionService.save(spTransactions);
		} catch (DatabaseException e) {
			logger.error("Si è verificato un errore in AnnullaErogatoTrace.trace: " + e.getMessage());
		}
		if (mwOutputBean.getAnnullaErogatoRicevuta().getElencoErroriRicette() != null && mwOutputBean.getAnnullaErogatoRicevuta().getElencoErroriRicette().sizeOfErroreRicettaArray() > 0) {
			int sizeError = mwOutputBean.getAnnullaErogatoRicevuta().getElencoErroriRicette().sizeOfErroreRicettaArray();
			for (int i = 0; i < sizeError; i++) {
				ErroreRicettaType err = mwOutputBean.getAnnullaErogatoRicevuta().getElencoErroriRicette().getErroreRicettaArray(i);
				if (StringUtils.isEmpty(err.getCodEsito()) || !err.getCodEsito().equalsIgnoreCase("0000")) {
					SpTransactionError spTransactionsError = new SpTransactionError();
					spTransactionsError.setOperation(SERVICE_NAME);
					spTransactionsError.setDtInsert(new Date());
					spTransactionsError.setOrder(new Long(i));
					spTransactionsError.setCodError(err.getCodEsito());
					spTransactionsError.setDescrError(err.getEsito());
					spTransactionsError.setIdTrans(spTransactions.getId());
				
					try {
						spTransactionErrorService.save(spTransactionsError);
					} catch (DatabaseException e) {
						logger.error("Si è verificato un errore in AnnullaErogatoTrace.trace: " + e.getMessage());
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
