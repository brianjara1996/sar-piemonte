package it.eng.cct.dem.sar.mw.piemonte.adapters;

import it.eng.cct.dem.sar.mw.exceptions.MwReqAdapterException;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TEPrescription;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TElencoNota;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TErrore;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TNota;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TRicetta;
import it.finanze.sanita.dem.xsd.invioprescrittoricevuta.InvioPrescrittoRicevutaDocument;
import it.finanze.sanita.dem.xsd.invioprescrittoricevuta.InvioPrescrittoRicevutaDocument.InvioPrescrittoRicevuta;
import it.finanze.sanita.dem.xsd.tipodati.ElencoNotaType;
import it.finanze.sanita.dem.xsd.tipodati.NotaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
		// XXX Nuovi campi previsti dal tracciato Lorenzin del DM 9 dic 2015
		ElencoNotaType elencoNotaSar = invioPrescrittoRicevuta.getElencoNota();
		if (elencoNotaSar != null && elencoNotaSar.sizeOfNotaArray() > 0) {
			TElencoNota elencoNota = ricetta.addNewElencoNota();
			for (NotaType notaSar: elencoNotaSar.getNotaArray()) {
				TNota nota = elencoNota.addNewNota();
				this.setIfNotEmpty(nota, "progPrescr", notaSar.getProgrPresc());
				this.setIfNotEmpty(nota, "codProdPrest", notaSar.getCodProdPrest());
				this.setIfNotEmpty(nota, "tipoAmbulatorio", notaSar.getTipoAmbulatorio());
			}
		}
		String flagPromemoria = invioPrescrittoRicevuta.getFlagPromemoria();
		if (SistemaTiesseAdapter.WS_TIESSE_PROMEMORIA_YES.equals(flagPromemoria)) {
			byte[] pdfPromemoria = invioPrescrittoRicevuta.getPdfPromemoria();
			if (pdfPromemoria != null && pdfPromemoria.length > 0) {
				ricetta.setPromemoria(pdfPromemoria);
			}
		}
		//XXX Fine nuovi campi previsti dal tracciato Lorenzin del DM 9 dic 2015
		TErrore[] errori = ServizioTrasmissioneResponseAdapter.getErroriAndComunicazioniArray(invioPrescrittoRicevuta.getElencoComunicazioni(), invioPrescrittoRicevuta.getElencoErroriRicette());
		if (errori != null && errori.length > 0) {
			ePrescriptionResponse.addNewErrori().setErroreArray(errori);
		}
		return ePrescriptionResponse;
	}

}
