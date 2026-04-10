package it.eng.cct.dem.sar.piemonte.services.echo;

import it.csi.rel.relbl.interfacews.frontend.other.nre.RichiestaNreImplServiceSkeletonInterface;
import it.finanze.sanita.dem.dm.wsdl.richiestanre.ComunicazioneType;
import it.finanze.sanita.dem.dm.wsdl.richiestanre.ElencoComunicazioniType;
import it.finanze.sanita.dem.xsd.richiestanrericevuta.RichiestaNreRicevutaDocument;
import it.finanze.sanita.dem.xsd.richiestanrericevuta.RichiestaNreRicevutaDocument.RichiestaNreRicevuta;
import it.finanze.sanita.dem.xsd.richiestanrerichiesta.RichiestaNreRichiestaDocument;

import java.io.IOException;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.service.Lifecycle;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlbeans.XmlException;

public class RichiestaNreImplService extends SimpleService implements RichiestaNreImplServiceSkeletonInterface, Lifecycle {
	public static final Log logger = LogFactory.getLog(RichiestaNreImplService.class);
	public static final String RESPONSE = "samples/responses/RichiestaNreRicevuta.xml";

	 public static final String COUNT = "count";
	
	public RichiestaNreRicevutaDocument richiestaNre(RichiestaNreRichiestaDocument richiestaNreRichiesta) {
		RichiestaNreRicevutaDocument result = null;
		try {
			result = RichiestaNreRicevutaDocument.Factory.parse(this.getInputStream(RESPONSE));
		} catch (XmlException e) {
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		result.getRichiestaNreRicevuta().setNre("0123456789abc_" + this.getCount());
		if (RandomUtils.nextBoolean()) {
			throw new RuntimeException("Errore casuale indotto nel servizio di recupero NRE!");
		}
		return result;
	}

	
	protected RichiestaNreRicevutaDocument getSampleResponse() {
		RichiestaNreRicevutaDocument response = RichiestaNreRicevutaDocument.Factory.newInstance();
		RichiestaNreRicevuta richiestaNreRicevuta = response.addNewRichiestaNreRicevuta();
		richiestaNreRicevuta.setCodEsitoRichiestaNre("0000");
		richiestaNreRicevuta.setNre("0123456789abcde" + this.getCount());
		ElencoComunicazioniType elencoComunicazioni = richiestaNreRicevuta.addNewElencoComunicazioni();
		ComunicazioneType comunicazione = elencoComunicazioni.addNewComunicazione();
		comunicazione.setCodice("0001");
		comunicazione.setMessaggio("Nessuna comunicazione!");
		return response;
	}
	
	public int getCount() {
        ServiceContext serviceContext = MessageContext.getCurrentMessageContext().getServiceContext();
        Integer storedVaue = (Integer) serviceContext.getProperty(COUNT);
        int newCount = storedVaue.intValue() + 1;
        serviceContext.setProperty(COUNT, new Integer(newCount));
        return newCount;
    }

    public void init(ServiceContext serviceContext) {
        logger.info("Initializing the service context");
        // initialize the count to zero
        serviceContext.setProperty(COUNT, new Integer(0));
    }

    public void destroy(ServiceContext serviceContext) {
    	logger.info("Destroying the service context");
    }
}
