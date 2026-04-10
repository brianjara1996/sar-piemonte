package it.eng.cct.dem.sar.mw.piemonte.services;

import it.eng.cct.dem.sar.mw.adapters.ServiceInputAdapter;
import it.eng.cct.dem.sar.mw.exceptions.MwException;
import it.eng.cct.dem.sar.mw.exceptions.MwRespAdapterException;
import it.eng.cct.dem.sar.mw.piemonte.adapters.InterrogaNreUtilRichiestaAdapter;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TEPrescriptions;
import it.eng.cct.dem.sar.mw.services.IServiceRegistry;
import it.eng.cct.dem.sar.mw.services.ServiceClientHelper;
import it.finanze.sanita.dem.xsd.interroganreutilricevuta.InterrogaNreUtilRicevutaDocument;

import org.apache.xmlbeans.XmlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DemInterrogaNreUtilizzatiHelper extends ServiceClientHelper {

	private static final Logger logger = LoggerFactory.getLogger(DemInterrogaNreUtilizzatiHelper.class);

	public DemInterrogaNreUtilizzatiHelper(IServiceRegistry serviceRegistry) {
		super(serviceRegistry);
	}

	public InterrogaNreUtilRicevutaDocument interrogaNreUtilizzati(TEPrescriptions request) throws MwException {
		ServiceInputAdapter requestAdapter = new InterrogaNreUtilRichiestaAdapter(request);
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

	private InterrogaNreUtilRicevutaDocument parseResponse(String responseMessage) throws MwRespAdapterException {
		InterrogaNreUtilRicevutaDocument responseDocument;
		try {
			responseDocument = InterrogaNreUtilRicevutaDocument.Factory.parse(responseMessage);
		} catch (XmlException e) {
			throw new MwRespAdapterException(e.getMessage(), e);
		}
		return responseDocument;
	}

}
