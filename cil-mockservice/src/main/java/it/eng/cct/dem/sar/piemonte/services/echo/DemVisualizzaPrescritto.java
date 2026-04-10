package it.eng.cct.dem.sar.piemonte.services.echo;

import it.finanze.sanita.dem.dm.wsdl.visualizzaprescritto.service.DemVisualizzaPrescrittoSkeletonInterface;
import it.finanze.sanita.dem.xsd.visualizzaprescrittoricevuta.VisualizzaPrescrittoRicevutaDocument;
import it.finanze.sanita.dem.xsd.visualizzaprescrittorichiesta.VisualizzaPrescrittoRichiestaDocument;

import java.io.IOException;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlbeans.XmlException;

public class DemVisualizzaPrescritto extends SimpleService implements DemVisualizzaPrescrittoSkeletonInterface {
	public static final Log logger = LogFactory.getLog(DemVisualizzaPrescritto.class);
	public static final String RESPONSE = "samples/responses/VisualizzaPrescrittoRicevuta.xml";

	public VisualizzaPrescrittoRicevutaDocument visualizzaPrescritto(VisualizzaPrescrittoRichiestaDocument visualizzaPrescrittoRichiesta) {
		VisualizzaPrescrittoRicevutaDocument result = null;
		try {
			result = VisualizzaPrescrittoRicevutaDocument.Factory.parse(this.getInputStream(RESPONSE));
		} catch (XmlException e) {
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		
		result.getVisualizzaPrescrittoRicevuta().setNre(visualizzaPrescrittoRichiesta.getVisualizzaPrescrittoRichiesta().getNre());
		if (RandomUtils.nextBoolean()) {
			throw new RuntimeException("Errore casuale ndotto nel servizio di recupero!");
		}
		return result;
	}

}
