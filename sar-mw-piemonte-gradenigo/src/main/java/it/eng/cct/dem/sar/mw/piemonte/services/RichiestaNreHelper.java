package it.eng.cct.dem.sar.mw.piemonte.services;

import org.apache.xmlbeans.XmlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.cct.dem.sar.mw.adapters.ServiceInputAdapter;
import it.eng.cct.dem.sar.mw.exceptions.MwException;
import it.eng.cct.dem.sar.mw.exceptions.MwRespAdapterException;
import it.eng.cct.dem.sar.mw.piemonte.adapters.RichiestaNreRichiestaAdapter;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TEPrescription;
import it.eng.cct.dem.sar.mw.services.IServiceRegistry;
import it.eng.cct.dem.sar.mw.services.ServiceClientHelper;
import it.finanze.sanita.dem.xsd.richiestanrericevuta.RichiestaNreRicevutaDocument;

public class RichiestaNreHelper extends ServiceClientHelper {

	private static final Logger logger = LoggerFactory.getLogger(RichiestaNreHelper.class);

	public RichiestaNreHelper(IServiceRegistry serviceRegistry) {
		super(serviceRegistry);
	}

	public RichiestaNreRicevutaDocument richiestaNre(TEPrescription request) throws MwException {
		ServiceInputAdapter requestAdapter = new RichiestaNreRichiestaAdapter(request);
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

	private RichiestaNreRicevutaDocument parseResponse(String responseMessage) throws MwRespAdapterException {
		RichiestaNreRicevutaDocument responseDocument;
		try {
			responseDocument = RichiestaNreRicevutaDocument.Factory.parse(responseMessage);
		} catch (XmlException e) {
			throw new MwRespAdapterException(e.getMessage(), e);
		}
		return responseDocument;
	}

}
