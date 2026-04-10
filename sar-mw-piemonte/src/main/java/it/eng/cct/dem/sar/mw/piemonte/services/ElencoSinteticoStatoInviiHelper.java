package it.eng.cct.dem.sar.mw.piemonte.services;

import it.eng.cct.dem.sar.mw.adapters.ServiceInputAdapter;
import it.eng.cct.dem.sar.mw.exceptions.MwException;
import it.eng.cct.dem.sar.mw.exceptions.MwRespAdapterException;
import it.eng.cct.dem.sar.mw.piemonte.adapters.ElencoSinteticoStatoInviiRequestAdapter;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TEPrescriptions;
import it.eng.cct.dem.sar.mw.services.IServiceRegistry;
import it.eng.cct.dem.sar.mw.services.ServiceClientHelper;
import it.finanze.sanita.dem.dpcm.wsdl.elencoSinteticoStatoInvii.service.VisualizzaElencoStatoInviiResponseDocument;

import org.apache.xmlbeans.XmlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElencoSinteticoStatoInviiHelper extends ServiceClientHelper {

	private static final Logger logger = LoggerFactory.getLogger(ElencoSinteticoStatoInviiHelper.class);

	public ElencoSinteticoStatoInviiHelper(IServiceRegistry serviceRegistry) {
		super(serviceRegistry);
	}

	public VisualizzaElencoStatoInviiResponseDocument visualizzaElencoStatoInvii(TEPrescriptions request) throws MwException {
		ServiceInputAdapter requestAdapter = new ElencoSinteticoStatoInviiRequestAdapter(request);
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

	private VisualizzaElencoStatoInviiResponseDocument parseResponse(String responseMessage) throws MwRespAdapterException {
		VisualizzaElencoStatoInviiResponseDocument responseDocument;
		try {
			responseDocument = VisualizzaElencoStatoInviiResponseDocument.Factory.parse(responseMessage);
		} catch (XmlException e) {
			throw new MwRespAdapterException(e.getMessage(), e);
		}
		return responseDocument;
	}

}
