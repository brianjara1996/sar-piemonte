package it.eng.cct.dem.sar.mw.piemonte.services;

import org.apache.xmlbeans.XmlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.cct.dem.sar.mw.adapters.ServiceInputAdapter;
import it.eng.cct.dem.sar.mw.exceptions.MwException;
import it.eng.cct.dem.sar.mw.exceptions.MwRespAdapterException;
import it.eng.cct.dem.sar.mw.piemonte.adapters.ElencoAnaliticoEsitoRicetteRequestAdapter;
import it.eng.cct.dem.sar.mw.piemonte.db.service.trace.ServiceMonitoringHelper;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TEPrescriptions;
import it.eng.cct.dem.sar.mw.services.IServiceRegistry;
import it.eng.cct.dem.sar.mw.services.ServiceClientHelper;
import it.finanze.sanita.dem.dpcm.wsdl.elencoAnaliticoEsitoRicette.service.VisualizzaElencoStatoRicetteResponseDocument;

public class ElencoAnaliticoEsitoRicetteHelper extends ServiceClientHelper {

	private static final Logger logger = LoggerFactory.getLogger(ElencoAnaliticoEsitoRicetteHelper.class);
	private ServiceMonitoringHelper serviceMonitoringHelper;

	public ElencoAnaliticoEsitoRicetteHelper(IServiceRegistry serviceRegistry, ServiceMonitoringHelper serviceMonitoringHelper) {
		super(serviceRegistry);
		this.serviceMonitoringHelper = serviceMonitoringHelper;
	}

	public VisualizzaElencoStatoRicetteResponseDocument visualizzaElencoStatoRicette(TEPrescriptions request) throws MwException {
		ServiceInputAdapter requestAdapter = new ElencoAnaliticoEsitoRicetteRequestAdapter(request, this.serviceMonitoringHelper);
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

	private VisualizzaElencoStatoRicetteResponseDocument parseResponse(String responseMessage) throws MwRespAdapterException {
		VisualizzaElencoStatoRicetteResponseDocument responseDocument;
		try {
			responseDocument = VisualizzaElencoStatoRicetteResponseDocument.Factory.parse(responseMessage);
		} catch (XmlException e) {
			throw new MwRespAdapterException(e.getMessage(), e);
		}
		return responseDocument;
	}

}
