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
import it.finanze.sanita.dem.dpcm.wsdl.elencoSinteticoStatoInvii.service.VisualizzaElencoStatoInviiDocument;
import it.finanze.sanita.dem.dpcm.wsdl.elencoSinteticoStatoInvii.service.VisualizzaElencoStatoInviiResponseDocument;

public class EPrescEsitiSintTrace {

	private EsitiDocument mwInputBean;
	private EsitiDocument mwOutputBean;
	private SpTransactionService spTransactionService;
	private SpTransactionErrorService spTransactionErrorService;
	private VisualizzaElencoStatoInviiDocument mwToServiceBean;
	private VisualizzaElencoStatoInviiResponseDocument serviceToMwBean;
	private Boolean traceInternalMsg;
	private static final Logger logger = LoggerFactory.getLogger(EPrescEsitiSintTrace.class);

	private static final String SERVICE_NAME = "EsitiSintetico";

	public EPrescEsitiSintTrace(XmlObject mwInputBean, XmlObject mwOutputBean, XmlObject mwToServiceBean, XmlObject serviceToMwBean, Boolean traceInternalMsg) {
		super();
		this.mwInputBean = (EsitiDocument) mwInputBean;
		this.mwOutputBean = (EsitiDocument) mwOutputBean;
		this.mwToServiceBean = (VisualizzaElencoStatoInviiDocument) mwToServiceBean;
		this.serviceToMwBean = (VisualizzaElencoStatoInviiResponseDocument) serviceToMwBean;
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
		
		spTransactions.setProtocolloSac(this.serviceToMwBean.getVisualizzaElencoStatoInviiResponse().getVisualizzaElencoStatoInviiReturn().getProtocolloSac());
		spTransactions.setEsito(Boolean.toString(this.serviceToMwBean.getVisualizzaElencoStatoInviiResponse().getVisualizzaElencoStatoInviiReturn().getIsOperazioneValida()));
		

		try {
			spTransactionService.save(spTransactions);
		} catch (DatabaseException e) {
			logger.error("Si è verificato un errore in EPrescRecuperoPrescrittoTrace.trace: " + e.getMessage());
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
