package it.eng.cct.dem.sar.mw.piemonte.services;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.cct.dem.sar.mw.exceptions.MwException;
import it.eng.cct.dem.sar.mw.exceptions.MwReqAdapterException;
import it.eng.cct.dem.sar.mw.piemonte.adapters.ElencoSinteticoStatoInviiResponseAdapter;
import it.eng.cct.dem.sar.mw.piemonte.db.service.trace.ServiceMonitoringHelper;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.EPrescriptionsDocument;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TEPrescriptions;
import it.eng.cct.dem.sar.mw.services.IServiceRegistry;
import it.eng.cct.dem.sar.piemonte.mw.schemas.SarMiddleware.ServiceFault;
import it.finanze.sanita.dem.dpcm.wsdl.elencoSinteticoStatoInvii.service.VisualizzaElencoStatoInviiResponseDocument;

public class ElencoSinteticoStatoInviiExecutor extends ServizioEPrescription {

	private static final Logger logger = LoggerFactory.getLogger(ElencoSinteticoStatoInviiExecutor.class);

	private IServiceRegistry serviceRegistry;

	private ElencoSinteticoStatoInviiHelper elencoSinteticoStatoInviiHelper;
	
	private ServiceMonitoringHelper serviceMonitoringHelper;

	public ElencoSinteticoStatoInviiExecutor(IServiceRegistry serviceRegistry, ServiceMonitoringHelper serviceMonitoringHelper) {
		super();
		this.serviceRegistry = serviceRegistry;
		this.serviceMonitoringHelper = serviceMonitoringHelper;
	}

	public EPrescriptionsDocument invoke(EPrescriptionsDocument request) throws ServiceFault {
		this.setMappedDiagnosticContext(StringUtils.isNotEmpty(request.getEPrescriptions().getProtocolloSAC()) ? request.getEPrescriptions().getProtocolloSAC(): "ESITOSINTETICO");
		TEPrescriptions requestedEPrescriptions = request.getEPrescriptions();
		EPrescriptionsDocument response = EPrescriptionsDocument.Factory.newInstance();
		logger.info("Serving ElencoSinteticoStatoInvii...");
		ElencoSinteticoStatoInviiHelper elencoSinteticoStatoInviiHelper = this.getElencoSinteticoStatoInviiHelper();
		VisualizzaElencoStatoInviiResponseDocument ricevutaDocument = null;
		try {
			ricevutaDocument = elencoSinteticoStatoInviiHelper.visualizzaElencoStatoInvii(requestedEPrescriptions);
		} catch (MwException e) {
			if (e.isAxisFault()) {
				response.setEPrescriptions(ServizioEPrescription.buildErrorMessage(requestedEPrescriptions, e, null));
				return response;
			}
			throw newServiceFault(requestedEPrescriptions, e);
		}
		ElencoSinteticoStatoInviiResponseAdapter responseAdapter = new ElencoSinteticoStatoInviiResponseAdapter(requestedEPrescriptions, ricevutaDocument);
		TEPrescriptions responseEPrescriptions = null;
		try {
			responseEPrescriptions = responseAdapter.getEPrescriptionsDocument();
		} catch (MwReqAdapterException e) {
			throw newServiceFault(requestedEPrescriptions, e);
		}
		response.setEPrescriptions(responseEPrescriptions);
		return response;
	}

	
	public void setServices(IServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

	private ElencoSinteticoStatoInviiHelper getElencoSinteticoStatoInviiHelper() {
		if (this.elencoSinteticoStatoInviiHelper == null) {
			this.elencoSinteticoStatoInviiHelper = new ElencoSinteticoStatoInviiHelper(serviceRegistry, this.serviceMonitoringHelper);
		}
		return this.elencoSinteticoStatoInviiHelper;
	}
}
