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
import it.eng.cct.piemonte.eprescription.types.TrasmissioneDocument;
import it.finanze.sanita.dem.xsd.invioprescrittoricevuta.InvioPrescrittoRicevutaDocument;
import it.finanze.sanita.dem.xsd.invioprescrittorichiesta.InvioPrescrittoRichiestaDocument;
import it.finanze.sanita.dem.xsd.tipodati.ErroreRicettaType;

public class EPrescInvioPrescrittoTrace {

	private TrasmissioneDocument mwInputBean;
	private TrasmissioneDocument mwOutputBean;
	private SpTransactionService spTransactionService;
	private SpTransactionErrorService spTransactionErrorService;
	private InvioPrescrittoRichiestaDocument mwToServiceBean;
	private InvioPrescrittoRicevutaDocument serviceToMwBean;
	private Boolean traceInternalMsg;
	private static final Logger logger = LoggerFactory.getLogger(EPrescInvioPrescrittoTrace.class);

	private static final String SERVICE_NAME = "demInvioPrescritto";

	public EPrescInvioPrescrittoTrace(XmlObject mwInputBean, XmlObject mwOutputBean, XmlObject mwToServiceBean, XmlObject serviceToMwBean, Boolean traceInternalMsg) {
		super();
		this.mwInputBean = (TrasmissioneDocument) mwInputBean;
		this.mwOutputBean = (TrasmissioneDocument) mwOutputBean;
		this.mwToServiceBean = (InvioPrescrittoRichiestaDocument) mwToServiceBean;
		this.serviceToMwBean = (InvioPrescrittoRicevutaDocument) serviceToMwBean;
		this.spTransactionErrorService = (SpTransactionErrorService) ApplicationContextProvider.getApplicationContext().getBean("spTransactionErrorService");
		this.spTransactionService = (SpTransactionService) ApplicationContextProvider.getApplicationContext().getBean("spTransactionService");
		this.traceInternalMsg = traceInternalMsg;
	}

	public void trace() {
		SpTransaction spTransactions = new SpTransaction();
		spTransactions.setTipo(SERVICE_NAME);
		spTransactions.setMsgMwSrv(this.mwToServiceBean.xmlText().getBytes());
		spTransactions.setMsgSrvMw(this.serviceToMwBean.xmlText().getBytes());
		if(traceInternalMsg){
			spTransactions.setMsgMwIn(this.mwInputBean.xmlText().getBytes());
			spTransactions.setMsgMwOut(this.mwOutputBean.xmlText().getBytes());
		}
		spTransactions.setDtInsert(new Date());
		spTransactions.setNre(this.mwToServiceBean.getInvioPrescrittoRichiesta().getNre());
		spTransactions.setEsito(this.serviceToMwBean.getInvioPrescrittoRicevuta().getCodEsitoInserimento());
		spTransactions.setCfPaziente(StringEncodingUtils.sha256(this.mwToServiceBean.getInvioPrescrittoRichiesta().getCodiceAss()));
		spTransactions.setCfMedico(this.mwToServiceBean.getInvioPrescrittoRichiesta().getCfMedico1());
		if (this.mwToServiceBean.getInvioPrescrittoRichiesta().getCodiceAss() != null) {
				String cf = this.mwToServiceBean.getInvioPrescrittoRichiesta().getCodiceAss();
				spTransactions.setCfPaziente(StringEncodingUtils.sha256(cf));
		}
		try {
			spTransactionService.save(spTransactions);
		} catch (DatabaseException e) {
			logger.error("Si è verificato un errore in EPrescInvioTelematicoPrescrittoTrace.trace: " + e.getMessage());
		}
		if (this.serviceToMwBean.getInvioPrescrittoRicevuta().getElencoErroriRicette() != null && this.serviceToMwBean.getInvioPrescrittoRicevuta().getElencoErroriRicette().sizeOfErroreRicettaArray() > 0) {
			int sizeError = this.serviceToMwBean.getInvioPrescrittoRicevuta().getElencoErroriRicette().sizeOfErroreRicettaArray();
			for (int i = 0; i < sizeError; i++) {
				ErroreRicettaType err = this.serviceToMwBean.getInvioPrescrittoRicevuta().getElencoErroriRicette().getErroreRicettaArray(i);
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
						logger.error("Si è verificato un errore in EPrescInvioTelematicoPrescrittoTrace.trace: " + e.getMessage());
					}
				}
			}
		}
	}

	public void save(SpTransaction spTransaction) throws DatabaseException {
		spTransactionService.save(spTransaction);
	}

	public void save(SpTransactionError spTransactionsError) throws DatabaseException {
		spTransactionErrorService.save(spTransactionsError);
	}
}
