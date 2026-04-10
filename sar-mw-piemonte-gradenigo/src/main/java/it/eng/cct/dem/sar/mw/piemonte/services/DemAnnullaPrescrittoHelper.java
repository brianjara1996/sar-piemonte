package it.eng.cct.dem.sar.mw.piemonte.services;

import org.apache.xmlbeans.XmlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.cct.dem.sar.mw.adapters.ServiceInputAdapter;
import it.eng.cct.dem.sar.mw.exceptions.MwException;
import it.eng.cct.dem.sar.mw.exceptions.MwRespAdapterException;
import it.eng.cct.dem.sar.mw.piemonte.adapters.AnnullaPrescrittoRichiestaAdapter;
import it.eng.cct.dem.sar.mw.piemonte.db.service.trace.ServiceMonitoringHelper;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TEPrescription;
import it.eng.cct.dem.sar.mw.services.IServiceRegistry;
import it.eng.cct.dem.sar.mw.services.ServiceClientHelper;
import it.finanze.sanita.dem.xsd.annullaprescrittoricevuta.AnnullaPrescrittoRicevutaDocument;

public class DemAnnullaPrescrittoHelper extends ServiceClientHelper {

	private static final Logger logger = LoggerFactory.getLogger(DemAnnullaPrescrittoHelper.class);
	private ServiceMonitoringHelper serviceMonitoringHelper;

	public DemAnnullaPrescrittoHelper(IServiceRegistry serviceRegistry, ServiceMonitoringHelper serviceMonitoringHelper) {
		super(serviceRegistry);
		this.serviceMonitoringHelper = serviceMonitoringHelper;
	}

	public AnnullaPrescrittoRicevutaDocument annullaPrescritto(TEPrescription request) throws MwException {
		ServiceInputAdapter requestAdapter = new AnnullaPrescrittoRichiestaAdapter(request, this.serviceMonitoringHelper);
		String responseXml = null;
		try {
			requestAdapter.getServiceInput().getRequestMessage();
			responseXml = this.invoke(requestAdapter);
		} catch (MwException e) {
			logger.error("Impossibile ottenere una risposta dal SAR. " + e.getMessage(), e);
			throw e;
		} catch (Throwable e) {
			logger.error("Impossibile ottenere una risposta dal SAR. " + e.getMessage(), e);
			throw new MwException(e.getMessage(), e);
		}
		return this.parseResponse(responseXml);
	}

	private AnnullaPrescrittoRicevutaDocument parseResponse(String responseMessage) throws MwRespAdapterException {
		AnnullaPrescrittoRicevutaDocument responseDocument;
		try {
			responseDocument = AnnullaPrescrittoRicevutaDocument.Factory.parse(responseMessage);
		} catch (XmlException e) {
			throw new MwRespAdapterException(e.getMessage(), e);
		}
		this.serviceMonitoringHelper.setServiceToMwBean(responseDocument);
		return responseDocument;
	}

}
