package it.eng.cct.dem.sar.mw.piemonte.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.cct.dem.sar.mw.exceptions.MwException;
import it.eng.cct.dem.sar.mw.exceptions.MwReqAdapterException;
import it.eng.cct.dem.sar.mw.piemonte.adapters.ServizioInterrogaResponseAdapter;
import it.eng.cct.dem.sar.mw.piemonte.db.service.trace.ServiceMonitoringHelper;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.EPrescriptionsDocument;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TEPrescriptions;
import it.eng.cct.dem.sar.mw.services.IServiceRegistry;
import it.eng.cct.dem.sar.piemonte.mw.schemas.SarMiddleware.ServiceFault;
import it.finanze.sanita.dem.xsd.interroganreutilricevuta.InterrogaNreUtilRicevutaDocument;

public class ServizioInterrogaExecutor extends ServizioEPrescription {

	private static final Logger logger = LoggerFactory.getLogger(ServizioInterrogaExecutor.class);

	private IServiceRegistry serviceRegistry;

	private DemInterrogaNreUtilizzatiHelper interrogaNreUtilizzatiHelper;
	
	private ServiceMonitoringHelper serviceMonitoringHelper;

	public ServizioInterrogaExecutor(IServiceRegistry serviceRegistry, ServiceMonitoringHelper serviceMonitoringHelper) {
		super();
		this.serviceRegistry = serviceRegistry;
		this.serviceMonitoringHelper = serviceMonitoringHelper;
	}

	public EPrescriptionsDocument invoke(EPrescriptionsDocument request) throws ServiceFault {
		this.setMappedDiagnosticContext("INVIODPCM");
		TEPrescriptions requestedEPrescriptions = request.getEPrescriptions();
		EPrescriptionsDocument response = EPrescriptionsDocument.Factory.newInstance();
		logger.info("Serving InvioTelematicoSanita...");
		DemInterrogaNreUtilizzatiHelper interrogaNreUtilizzatiHelper = this.getInterrogaNreUtilizzatiHelper();
		InterrogaNreUtilRicevutaDocument ricevutaDocument = null;
		try {
			ricevutaDocument = interrogaNreUtilizzatiHelper.interrogaNreUtilizzati(requestedEPrescriptions);
		} catch (MwException e) {
			if (e.isAxisFault()) {
				response.setEPrescriptions(ServizioEPrescription.buildErrorMessage(requestedEPrescriptions, e, null));
				return response;
			}
			throw newServiceFault(requestedEPrescriptions, e);
		}
		ServizioInterrogaResponseAdapter responseAdapter = new ServizioInterrogaResponseAdapter(requestedEPrescriptions, ricevutaDocument);
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

	private DemInterrogaNreUtilizzatiHelper getInterrogaNreUtilizzatiHelper() {
		if (this.interrogaNreUtilizzatiHelper == null) {
			this.interrogaNreUtilizzatiHelper = new DemInterrogaNreUtilizzatiHelper(serviceRegistry, this.serviceMonitoringHelper);
		}
		return this.interrogaNreUtilizzatiHelper;
	}

}
