package it.eng.cct.dem.sar.piemonte.services.echo;

import it.finanze.sanita.dem.dm.wsdl.invioprescritto.service.DemInvioPrescrittoSkeletonInterface;
import it.finanze.sanita.dem.xsd.invioprescrittoricevuta.InvioPrescrittoRicevutaDocument;
import it.finanze.sanita.dem.xsd.invioprescrittorichiesta.InvioPrescrittoRichiestaDocument;

import java.io.IOException;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlbeans.XmlException;

public class DemInvioPrescritto extends SimpleService implements DemInvioPrescrittoSkeletonInterface {
	public static final Log logger = LogFactory.getLog(DemInvioPrescritto.class);
	public static final String RESPONSE = "samples/responses/InvioPrescrittoRicevuta.xml";

	public InvioPrescrittoRicevutaDocument invioPrescritto(InvioPrescrittoRichiestaDocument invioPrescrittoRichiesta) {
		InvioPrescrittoRicevutaDocument result = null;
		try {
			result = InvioPrescrittoRicevutaDocument.Factory.parse(this.getInputStream(RESPONSE));
		} catch (XmlException e) {
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		if (RandomUtils.nextBoolean()) {
			throw new RuntimeException("Errore casuale indotto nel servizio di trasmissione!");
		}
		return result;
	}

}
