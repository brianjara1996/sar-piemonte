package it.eng.cct.dem.sar.mw.piemonte.services;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.cct.dem.sar.mw.exceptions.MwException;
import it.eng.cct.dem.sar.mw.exceptions.MwReqAdapterException;
import it.eng.cct.dem.sar.mw.piemonte.adapters.ElencoAnaliticoEsitoRicetteResponseAdapter;
import it.eng.cct.dem.sar.mw.piemonte.db.service.trace.ServiceMonitoringHelper;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.EPrescriptionsDocument;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TEPrescriptions;
import it.eng.cct.dem.sar.mw.services.IServiceRegistry;
import it.eng.cct.dem.sar.piemonte.mw.schemas.SarMiddleware.ServiceFault;
import it.finanze.sanita.dem.dpcm.wsdl.elencoAnaliticoEsitoRicette.service.VisualizzaElencoStatoRicetteResponseDocument;

public class ElencoAnaliticoEsitoRicetteExecutor extends ServizioEPrescription {

	private static final Logger logger = LoggerFactory.getLogger(ElencoAnaliticoEsitoRicetteExecutor.class);

	private IServiceRegistry serviceRegistry;
	private ElencoAnaliticoEsitoRicetteHelper elencoAnaliticoEsitoRicetteHelper;
	private ServiceMonitoringHelper serviceMonitoringHelper;

	public ElencoAnaliticoEsitoRicetteExecutor(IServiceRegistry serviceRegistry, ServiceMonitoringHelper serviceMonitoringHelper) {
		super();
		this.serviceRegistry = serviceRegistry;
		this.serviceMonitoringHelper = serviceMonitoringHelper;
	}

	public EPrescriptionsDocument invoke(EPrescriptionsDocument request) throws ServiceFault {
		this.setMappedDiagnosticContext(StringUtils.isNotEmpty(request.getEPrescriptions().getProtocolloSAC()) ? request.getEPrescriptions().getProtocolloSAC(): "ESITOANALITICO");
		// Che sia una ricetta rossa o una autoimpegnativa, il campo nre dovrebbe essere sempre valorizzato
		TEPrescriptions requestedEPrescriptions = request.getEPrescriptions();
		EPrescriptionsDocument response = EPrescriptionsDocument.Factory.newInstance();
		logger.info("Serving ElencoAnaliticoEsitoRicette...");
		ElencoAnaliticoEsitoRicetteHelper elencoAnaliticoEsitoRicetteHelper = this.getElencoAnaliticoEsitoRicetteHelper();
		VisualizzaElencoStatoRicetteResponseDocument ricevutaDocument = null;
		try {
			ricevutaDocument = elencoAnaliticoEsitoRicetteHelper.visualizzaElencoStatoRicette(requestedEPrescriptions);
		} catch (MwException e) {
			if (e.isAxisFault()) {
				response.setEPrescriptions(ServizioEPrescription.buildErrorMessage(requestedEPrescriptions, e, null));
				return response;
			}
			throw newServiceFault(requestedEPrescriptions, e);
		}
		ElencoAnaliticoEsitoRicetteResponseAdapter responseAdapter = new ElencoAnaliticoEsitoRicetteResponseAdapter(requestedEPrescriptions, ricevutaDocument);
		TEPrescriptions responseEPrescriptions = null;
		try {
			responseEPrescriptions = responseAdapter.getEPrescriptionsDocument();
		} catch (MwReqAdapterException e) {
			throw newServiceFault(requestedEPrescriptions, e);
		}
		response.setEPrescriptions(responseEPrescriptions);
		this.serviceMonitoringHelper.setServiceToMwBean(response);
		return response;
	}
	
	public void setServices(IServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

	private ElencoAnaliticoEsitoRicetteHelper getElencoAnaliticoEsitoRicetteHelper() {
		if (this.elencoAnaliticoEsitoRicetteHelper == null) {
			this.elencoAnaliticoEsitoRicetteHelper = new ElencoAnaliticoEsitoRicetteHelper(serviceRegistry, this.serviceMonitoringHelper);
		}
		return this.elencoAnaliticoEsitoRicetteHelper;
	}

}
