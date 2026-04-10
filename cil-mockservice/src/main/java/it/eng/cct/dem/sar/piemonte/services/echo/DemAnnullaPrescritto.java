package it.eng.cct.dem.sar.piemonte.services.echo;

import it.finanze.sanita.dem.dm.wsdl.annullaprescritto.service.DemAnnullaPrescrittoSkeletonInterface;
import it.finanze.sanita.dem.xsd.annullaprescrittoricevuta.AnnullaPrescrittoRicevutaDocument;
import it.finanze.sanita.dem.xsd.annullaprescrittorichiesta.AnnullaPrescrittoRichiestaDocument;

import java.io.IOException;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlbeans.XmlException;

public class DemAnnullaPrescritto extends SimpleService implements DemAnnullaPrescrittoSkeletonInterface {
	public static final Log logger = LogFactory.getLog(DemAnnullaPrescritto.class);
	public static final String RESPONSE = "samples/responses/AnnullaPrescrittoRicevuta.xml";
	
	public AnnullaPrescrittoRicevutaDocument annullaPrescritto(AnnullaPrescrittoRichiestaDocument annullaPrescrittoRichiesta) {
		AnnullaPrescrittoRicevutaDocument result = null;
		try {
			result = AnnullaPrescrittoRicevutaDocument.Factory.parse(this.getInputStream(RESPONSE));
		} catch (XmlException e) {
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		if (RandomUtils.nextBoolean()) {
			throw new RuntimeException("Errore casuale indotto nel servizio di annullamento!");
		}
		return result;
	}

}
