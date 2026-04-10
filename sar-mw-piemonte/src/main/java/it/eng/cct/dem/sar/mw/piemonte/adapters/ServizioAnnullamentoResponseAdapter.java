package it.eng.cct.dem.sar.mw.piemonte.adapters;

import it.eng.cct.dem.sar.mw.exceptions.MwReqAdapterException;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.EPrescriptionDocument;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TEPrescription;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TErrore;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TRicetta;
import it.finanze.sanita.dem.xsd.annullaprescrittoricevuta.AnnullaPrescrittoRicevutaDocument;
import it.finanze.sanita.dem.xsd.annullaprescrittoricevuta.AnnullaPrescrittoRicevutaDocument.AnnullaPrescrittoRicevuta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServizioAnnullamentoResponseAdapter extends AreasEPrescriptionAdapter {

	private static final Logger logger = LoggerFactory.getLogger(ServizioAnnullamentoResponseAdapter.class);

	private AnnullaPrescrittoRicevutaDocument response;
	private TEPrescription request;

	public ServizioAnnullamentoResponseAdapter(TEPrescription request, AnnullaPrescrittoRicevutaDocument response) {
		super();
		this.response = response;
		this.request = request;
	}

	public TEPrescription getEPrescriptionDocument() throws MwReqAdapterException {
		TEPrescription ePrescriptionResponse = TEPrescription.Factory.newInstance();
		this.setMandatory(ePrescriptionResponse, "id", this.request.getId());
		AnnullaPrescrittoRicevuta annullaPrescrittoRicevuta = this.response.getAnnullaPrescrittoRicevuta();
		this.setMandatory(ePrescriptionResponse, "esito", annullaPrescrittoRicevuta.getCodEsitoAnnullamento());
		TRicetta ricetta = ePrescriptionResponse.addNewRicetta();
		this.setMandatory(ricetta, "nRE", annullaPrescrittoRicevuta.getNre());
		TErrore[] errori = MessageAdapter.getErroriAndComunicazioniArray(annullaPrescrittoRicevuta.getElencoComunicazioni(), annullaPrescrittoRicevuta.getElencoErroriRicette());
		if (errori != null && errori.length > 0) {
			ePrescriptionResponse.addNewErrori().setErroreArray(errori);
		}
		return ePrescriptionResponse;
	}

}
