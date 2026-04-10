package it.eng.cct.dem.sar.mw.piemonte.services;

import org.apache.xmlbeans.XmlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.cct.dem.sar.mw.adapters.ServiceInputAdapter;
import it.eng.cct.dem.sar.mw.exceptions.MwException;
import it.eng.cct.dem.sar.mw.exceptions.MwRespAdapterException;
import it.eng.cct.dem.sar.mw.piemonte.adapters.ServizioRichiestaLottoNreRequestAdapter;
import it.eng.cct.dem.sar.mw.services.IServiceRegistry;
import it.eng.cct.dem.sar.mw.services.ServiceClientHelper;
import it.finanze.sanita.mirsac.nre.xsd.lottoricevuta.LottoRicevutaNREDocument;
import it.finanze.sanita.mirsac.nre.xsd.lottorichiesta.LottoRichiestaNREDocument;

public class ServizioRichiestaNreHelper extends ServiceClientHelper {
	
	private static final Logger logger = LoggerFactory.getLogger(ServizioRichiestaNreHelper.class);

	public ServizioRichiestaNreHelper(IServiceRegistry serviceRegistry) {
		super(serviceRegistry);
	}
	
	public LottoRicevutaNREDocument richiestaNuovoLotto(LottoRichiestaNREDocument lottoRichiestaNREDocument) throws MwException{
		ServiceInputAdapter requestAdapter = new ServizioRichiestaLottoNreRequestAdapter(lottoRichiestaNREDocument);
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

	private LottoRicevutaNREDocument parseResponse(String responseMessage) throws MwRespAdapterException {
		LottoRicevutaNREDocument responseDocument;
		try {
			responseDocument = LottoRicevutaNREDocument.Factory.parse(responseMessage);
		} catch (XmlException e) {
			throw new MwRespAdapterException(e.getMessage(), e);
		}
		return responseDocument;
	}

}
