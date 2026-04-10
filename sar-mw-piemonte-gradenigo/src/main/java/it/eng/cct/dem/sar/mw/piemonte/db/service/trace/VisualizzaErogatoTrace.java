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
import it.finanze.sanita.dem.xsd.tipodati.ErroreRicettaType;
import it.finanze.sanita.dem.xsd.visualizzaerogatoricevuta.VisualizzaErogatoRicevutaDocument;
import it.finanze.sanita.dem.xsd.visualizzaerogatorichiesta.VisualizzaErogatoRichiestaDocument;

public class VisualizzaErogatoTrace {

	private VisualizzaErogatoRichiestaDocument mwInputBean;
	private VisualizzaErogatoRicevutaDocument mwOutputBean;
	private SpTransactionService spTransactionService;
	private SpTransactionErrorService spTransactionErrorService;
	private Boolean traceInternalMsg;
	private static final Logger logger = LoggerFactory.getLogger(VisualizzaErogatoTrace.class);

	private static final String SERVICE_NAME = "demVisualizzaErogato";
	
	public VisualizzaErogatoTrace(XmlObject mwInputBean, XmlObject mwOutputBean, Boolean traceInternalMsg) {
		super();
		this.mwInputBean = (VisualizzaErogatoRichiestaDocument) mwInputBean;
		this.mwOutputBean = (VisualizzaErogatoRicevutaDocument) mwOutputBean;
		this.spTransactionErrorService = (SpTransactionErrorService) ApplicationContextProvider.getApplicationContext().getBean("spTransactionErrorService");
		this.spTransactionService = (SpTransactionService) ApplicationContextProvider.getApplicationContext().getBean("spTransactionService");
		this.traceInternalMsg = traceInternalMsg;
	}

	public void trace() {
		SpTransaction spTransactions = new SpTransaction();

		spTransactions.setTipo(SERVICE_NAME);
		spTransactions.setCfPaziente(StringEncodingUtils.sha256(mwInputBean.getVisualizzaErogatoRichiesta().getCfAssistito()));
		spTransactions.setNre(mwInputBean.getVisualizzaErogatoRichiesta().getNre());
		spTransactions.setMsgMwSrv(mwInputBean.xmlText().getBytes());
		spTransactions.setMsgSrvMw(mwOutputBean.xmlText().getBytes());
		if(traceInternalMsg){
			spTransactions.setMsgMwIn(this.mwInputBean.xmlText().getBytes());
			spTransactions.setMsgMwOut(this.mwOutputBean.xmlText().getBytes());
		}
		spTransactions.setDtInsert(new Date());
		spTransactions.setEsito(mwOutputBean.getVisualizzaErogatoRicevuta().getCodEsitoVisualizzazione());
		try {
			spTransactionService.save(spTransactions);
		} catch (DatabaseException e) {
			logger.error("Si è verificato un errore in VisualizzaErogatoTrace.trace: " + e.getMessage());
		}
		if (mwOutputBean.getVisualizzaErogatoRicevuta().getElencoErroriRicette() != null && mwOutputBean.getVisualizzaErogatoRicevuta().getElencoErroriRicette().sizeOfErroreRicettaArray() > 0) {
			int sizeError = mwOutputBean.getVisualizzaErogatoRicevuta().getElencoErroriRicette().sizeOfErroreRicettaArray();
			for (int i = 0; i < sizeError; i++) {
				ErroreRicettaType err = mwOutputBean.getVisualizzaErogatoRicevuta().getElencoErroriRicette().getErroreRicettaArray(i);
				if (StringUtils.isEmpty(err.getCodEsito()) || !err.getCodEsito().equalsIgnoreCase("0000")) {
					SpTransactionError spTransactionsError = new SpTransactionError();
					spTransactionsError.setOperation(SERVICE_NAME);
					spTransactionsError.setDtInsert(new Date());
					spTransactionsError.setOrder(new Long(i));
					spTransactionsError.setCodError(err.getCodEsito());
					spTransactionsError.setDescrError(err.getTipoErrore());
					spTransactionsError.setIdTrans(spTransactions.getId());
					try {
						spTransactionErrorService.save(spTransactionsError);
					} catch (DatabaseException e) {
						logger.error("Si è verificato un errore in VisualizzaErogatoTrace.trace: " + e.getMessage());
					}
				}
			}
		}
	}
}
