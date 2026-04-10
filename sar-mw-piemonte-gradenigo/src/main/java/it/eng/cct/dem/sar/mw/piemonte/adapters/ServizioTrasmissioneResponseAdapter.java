package it.eng.cct.dem.sar.mw.piemonte.adapters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.cct.dem.sar.mw.exceptions.MwReqAdapterException;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TEPrescription;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TErrore;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TRicetta;
import it.finanze.sanita.dem.xsd.invioprescrittoricevuta.InvioPrescrittoRicevutaDocument;
import it.finanze.sanita.dem.xsd.invioprescrittoricevuta.InvioPrescrittoRicevutaDocument.InvioPrescrittoRicevuta;

public class ServizioTrasmissioneResponseAdapter extends AreasEPrescriptionAdapter {

	private static final Logger logger = LoggerFactory.getLogger(ServizioTrasmissioneResponseAdapter.class);

	private InvioPrescrittoRicevutaDocument response;
	private TEPrescription request;

	public ServizioTrasmissioneResponseAdapter(TEPrescription request, InvioPrescrittoRicevutaDocument response) {
		super();
		this.response = response;
		this.request = request;
	}

	public TEPrescription getEPrescriptionDocument() throws MwReqAdapterException {
		TEPrescription ePrescriptionResponse = TEPrescription.Factory.newInstance();
		TEPrescription reqMsg = this.request;
		InvioPrescrittoRicevuta invioPrescrittoRicevuta = this.response.getInvioPrescrittoRicevuta();
		ePrescriptionResponse.setId(reqMsg.getId());
		ePrescriptionResponse.setEsito(invioPrescrittoRicevuta.getCodEsitoInserimento());
		TRicetta ricetta = ePrescriptionResponse.addNewRicetta();
		ricetta.setCodiceAutenticazione(invioPrescrittoRicevuta.getCodAutenticazione());
		ricetta.setNRE(invioPrescrittoRicevuta.getNre());
		ricetta.setCodiceRegionale(invioPrescrittoRicevuta.getNre());
		// TODO TROVARE UNA COLLOCAZIONE PER LA DATA DI INSERIMENTO NEL
		// MESSAGGIO EPRESCRIPTION
		// invioPrescrittoRicevuta.getDataInserimento();
		TErrore[] errori = ServizioTrasmissioneResponseAdapter.getErroriAndComunicazioniArray(invioPrescrittoRicevuta.getElencoComunicazioni(), invioPrescrittoRicevuta.getElencoErroriRicette());
		if (errori != null && errori.length > 0) {
			ePrescriptionResponse.addNewErrori().setErroreArray(errori);
		}
		return ePrescriptionResponse;
	}

}
