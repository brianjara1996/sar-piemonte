package it.eng.cct.dem.sar.mw.piemonte.services;

import org.apache.xmlbeans.XmlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.cct.dem.sar.mw.adapters.ServiceInputAdapter;
import it.eng.cct.dem.sar.mw.exceptions.MwException;
import it.eng.cct.dem.sar.mw.exceptions.MwRespAdapterException;
import it.eng.cct.dem.sar.mw.piemonte.adapters.VisualizzaPrescrittoRichiestaAdapter;
import it.eng.cct.dem.sar.mw.piemonte.db.service.trace.ServiceMonitoringHelper;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TEPrescription;
import it.eng.cct.dem.sar.mw.services.IServiceRegistry;
import it.eng.cct.dem.sar.mw.services.ServiceClientHelper;
import it.finanze.sanita.dem.xsd.visualizzaprescrittoricevuta.VisualizzaPrescrittoRicevutaDocument;

public class DemVisualizzaPrescrittoHelper extends ServiceClientHelper {

	private static final Logger logger = LoggerFactory.getLogger(DemVisualizzaPrescrittoHelper.class);
	private ServiceMonitoringHelper serviceMonitoringHelper;

	public DemVisualizzaPrescrittoHelper(IServiceRegistry serviceRegistry, ServiceMonitoringHelper serviceMonitoringHelper) {
		super(serviceRegistry);
		this.serviceMonitoringHelper = serviceMonitoringHelper;
	}

	public VisualizzaPrescrittoRicevutaDocument visualizzaPrescritto(TEPrescription request) throws MwException {
		ServiceInputAdapter requestAdapter = new VisualizzaPrescrittoRichiestaAdapter(request, this.serviceMonitoringHelper);
		String responseXml = null;
		try {
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

	private VisualizzaPrescrittoRicevutaDocument parseResponse(String responseMessage) throws MwRespAdapterException {
		VisualizzaPrescrittoRicevutaDocument responseDocument;
		try {
			responseDocument = VisualizzaPrescrittoRicevutaDocument.Factory.parse(responseMessage);
		} catch (XmlException e) {
			throw new MwRespAdapterException(e.getMessage(), e);
		}
		this.serviceMonitoringHelper.setServiceToMwBean(responseDocument);
		return responseDocument;
	}

}
