package it.eng.cct.dem.sar.mw.piemonte.db.service.trace;

import java.util.Date;

import org.apache.xmlbeans.XmlObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.cct.dem.sar.mw.piemonte.db.model.SpTransaction;
import it.eng.cct.dem.sar.mw.piemonte.db.service.SpTransactionErrorService;
import it.eng.cct.dem.sar.mw.piemonte.db.service.SpTransactionService;
import it.eng.cct.dem.sar.mw.piemonte.exception.DatabaseException;
import it.eng.cct.dem.sar.mw.spring.ApplicationContextProvider;
import it.eng.cct.piemonte.eprescription.types.EsitiDocument;
import it.finanze.sanita.dem.dpcm.wsdl.elencoAnaliticoEsitoRicette.service.VisualizzaElencoStatoRicetteDocument;
import it.finanze.sanita.dem.dpcm.wsdl.elencoAnaliticoEsitoRicette.service.VisualizzaElencoStatoRicetteResponseDocument;

public class EPrescEsitiAnaliticoTrace {

	private EsitiDocument mwInputBean;
	private EsitiDocument mwOutputBean;
	private SpTransactionService spTransactionService;
	private SpTransactionErrorService spTransactionErrorService;
	private VisualizzaElencoStatoRicetteDocument mwToServiceBean;
	private VisualizzaElencoStatoRicetteResponseDocument serviceToMwBean;
	private Boolean traceInternalMsg;
	private static final Logger logger = LoggerFactory.getLogger(EPrescEsitiAnaliticoTrace.class);

	private static final String SERVICE_NAME = "EsitiAnalitico";

	public EPrescEsitiAnaliticoTrace(XmlObject mwInputBean, XmlObject mwOutputBean, XmlObject mwToServiceBean, XmlObject serviceToMwBean, Boolean traceInternalMsg) {
		super();
		this.mwInputBean = (EsitiDocument) mwInputBean;
		this.mwOutputBean = (EsitiDocument) mwOutputBean;
		this.mwToServiceBean = (VisualizzaElencoStatoRicetteDocument) mwToServiceBean;
		this.serviceToMwBean = (VisualizzaElencoStatoRicetteResponseDocument) serviceToMwBean;
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
		spTransactions.setProtocolloSac(this.serviceToMwBean.getVisualizzaElencoStatoRicetteResponse().getVisualizzaElencoStatoRicetteReturn().getProtocolloSac());
		spTransactions.setEsito(Boolean.toString(this.serviceToMwBean.getVisualizzaElencoStatoRicetteResponse().getVisualizzaElencoStatoRicetteReturn().getIsOperazioneValida()));
		
		try {
			spTransactionService.save(spTransactions);
		} catch (DatabaseException e) {
			logger.error("Si è verificato un errore in EPrescEsitiAnaliticoTrace.trace: " + e.getMessage());
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
