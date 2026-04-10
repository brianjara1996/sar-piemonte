package it.eng.cct.dem.sar.mw.piemonte.services;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.cct.dem.sar.mw.exceptions.MwException;
import it.eng.cct.dem.sar.mw.exceptions.MwReqAdapterException;
import it.eng.cct.dem.sar.mw.piemonte.adapters.InvioTelematicoSanitaAnnullaResponseAdapter;
import it.eng.cct.dem.sar.mw.piemonte.adapters.InvioTelematicoSanitaTrasmettiResponseAdapter;
import it.eng.cct.dem.sar.mw.piemonte.db.service.trace.ServiceMonitoringHelper;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.EPrescriptionsDocument;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TEPrescription;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TEPrescriptions;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TRicetta;
import it.eng.cct.dem.sar.mw.services.IServiceRegistry;
import it.eng.cct.dem.sar.piemonte.mw.schemas.SarMiddleware.ServiceFault;
import it.finanze.sanita.dem.dpcm.wsdl.invioTelematicoSanita.service.RicevutaDocument;
import it.finanze.sanita.dem.xsd.richiestanrericevuta.RichiestaNreRicevutaDocument;

public class InvioTelematicoSanitaExecutor extends ServizioEPrescription {

	private static final Logger logger = LoggerFactory.getLogger(InvioTelematicoSanitaExecutor.class);

	private IServiceRegistry serviceRegistry;

	private RichiestaNreHelper richiestaNreHelper;
	private InvioTelematicoSanitaHelper invioTelematicoSanitaHelper;
	private ServiceMonitoringHelper serviceMonitoringHelper;

	public InvioTelematicoSanitaExecutor(IServiceRegistry serviceRegistry, ServiceMonitoringHelper serviceMonitoringHelper) {
		super();
		this.serviceRegistry = serviceRegistry;
		this.serviceMonitoringHelper = serviceMonitoringHelper;
	}

	public EPrescriptionsDocument annulla(EPrescriptionsDocument request) throws ServiceFault {
		this.setMappedDiagnosticContext("ANNULLAMENTODPCM");
		// Che sia una ricetta rossa o una autoimpegnativa, il campo nre dovrebbe essere sempre valorizzato
		TEPrescriptions requestedEPrescriptions = request.getEPrescriptions();
		EPrescriptionsDocument response = EPrescriptionsDocument.Factory.newInstance();
		logger.info("Serving InvioTelematicoSanita...");
		InvioTelematicoSanitaHelper invioTelematicoSanitaHelper = this.getInvioTelematicoSanitaHelper();
		RicevutaDocument ricevutaDocument = null;
		try {
			ricevutaDocument = invioTelematicoSanitaHelper.inviaFileSanita(requestedEPrescriptions);
		} catch (MwException e) {
			if (e.isAxisFault()) {
				response.setEPrescriptions(ServizioEPrescription.buildErrorMessage(requestedEPrescriptions, e, null));
				this.serviceMonitoringHelper.setServiceToMwBean(response);
				return response;
			}
			throw newServiceFault(requestedEPrescriptions, e);
		}
		this.serviceMonitoringHelper.setServiceToMwBean(ricevutaDocument);
		InvioTelematicoSanitaAnnullaResponseAdapter responseAdapter = new InvioTelematicoSanitaAnnullaResponseAdapter(requestedEPrescriptions, ricevutaDocument);
		TEPrescriptions responseEPrescriptions = null;
		try {
			responseEPrescriptions = responseAdapter.getEPrescriptionsDocument();
		} catch (MwReqAdapterException e) {
			throw newServiceFault(requestedEPrescriptions, e);
		}
		response.setEPrescriptions(responseEPrescriptions);
		return response;
	}
	
	public EPrescriptionsDocument trasmetti(EPrescriptionsDocument request) throws ServiceFault {
		this.setMappedDiagnosticContext("INVIODPCM");
		// Che sia una ricetta rossa o una autoimpegnativa, il campo nre dovrebbe essere sempre valorizzato
		this.provideNres(request);
		TEPrescriptions requestedEPrescriptions = request.getEPrescriptions();
		EPrescriptionsDocument response = EPrescriptionsDocument.Factory.newInstance();
		logger.info("Serving InvioTelematicoSanita...");
		InvioTelematicoSanitaHelper invioTelematicoSanitaHelper = this.getInvioTelematicoSanitaHelper();
		RicevutaDocument ricevutaDocument = null;
		try {
			ricevutaDocument = invioTelematicoSanitaHelper.inviaFileSanita(requestedEPrescriptions);
		} catch (MwException e) {
			if (e.isAxisFault()) {
				response.setEPrescriptions(ServizioEPrescription.buildErrorMessage(requestedEPrescriptions, e, null));
				return response;
			}
			throw newServiceFault(requestedEPrescriptions, e);
		}
		InvioTelematicoSanitaTrasmettiResponseAdapter responseAdapter = new InvioTelematicoSanitaTrasmettiResponseAdapter(requestedEPrescriptions, ricevutaDocument);
		TEPrescriptions responseEPrescriptions = null;
		try {
			responseEPrescriptions = responseAdapter.getEPrescriptionsDocument();
		} catch (MwReqAdapterException e) {
			throw newServiceFault(requestedEPrescriptions, e);
		}
		response.setEPrescriptions(responseEPrescriptions);
		return response;
	}

	private void provideNres(EPrescriptionsDocument request) throws ServiceFault {
		TEPrescriptions ePrescriptions = request.getEPrescriptions();
		if (ePrescriptions == null) {
			return;
		}
		TEPrescription[] ePrescriptionArray = ePrescriptions.getEPrescriptionArray();
		if (ePrescriptionArray == null || ePrescriptionArray.length == 0) {
			return;
		}
		RichiestaNreHelper richiestaNreHelper = this.getRichiestaNreHelper();
		for (TEPrescription current: ePrescriptionArray) {
			TRicetta ricetta = current.getRicetta();
			String nre = ricetta.getNRE();
			if (StringUtils.isEmpty(nre)) {
				logger.warn("Manca nre e codicePoligrafico per la ricetta. Provo a reperire un nuovo nre...");
				RichiestaNreRicevutaDocument richiestaNreRicevuta = null;
				try {
					richiestaNreRicevuta = richiestaNreHelper.richiestaNre(current);
				} catch (MwException e) {
					logger.error("Impossibile ottenere un nre: " + e.getMessage(), e);
				}
				if (richiestaNreRicevuta != null && richiestaNreRicevuta.getRichiestaNreRicevuta().getCodEsitoRichiestaNre().equals("0000")) {
					current.getRicetta().setNRE(richiestaNreRicevuta.getRichiestaNreRicevuta().getNre());
				} else {
					throw newServiceFault(ePrescriptions, new MwException("Fallito il tentativo di reperire un nre per la ricetta che manca di nre e codicePoligrafico."));
				}
			}
		}
	}
	
	public void setServices(IServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

	private InvioTelematicoSanitaHelper getInvioTelematicoSanitaHelper() {
		if (this.invioTelematicoSanitaHelper == null) {
			this.invioTelematicoSanitaHelper = new InvioTelematicoSanitaHelper(serviceRegistry, this.serviceMonitoringHelper);
		}
		return this.invioTelematicoSanitaHelper;
	}

	private RichiestaNreHelper getRichiestaNreHelper() {
		if (this.richiestaNreHelper == null) {
			this.richiestaNreHelper = new RichiestaNreHelper(serviceRegistry);
		}
		return this.richiestaNreHelper;
	}
}
