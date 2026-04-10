package it.eng.cct.dem.sar.mw.piemonte.adapters;

import it.eng.cct.dem.sar.mw.exceptions.MwReqAdapterException;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TDettagli;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TDettaglio;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TEPrescription;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TErrore;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TMedico;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TPaziente;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TRicetta;
import it.finanze.sanita.dem.xsd.tipodati.DettaglioPrescrizioneType;
import it.finanze.sanita.dem.xsd.tipodati.ElencoDettagliPrescrizioniType;
import it.finanze.sanita.dem.xsd.visualizzaprescrittoricevuta.VisualizzaPrescrittoRicevutaDocument;
import it.finanze.sanita.dem.xsd.visualizzaprescrittoricevuta.VisualizzaPrescrittoRicevutaDocument.VisualizzaPrescrittoRicevuta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServizioRecuperoResponseAdapter extends AreasEPrescriptionAdapter {

	private static final Logger logger = LoggerFactory.getLogger(ServizioRecuperoResponseAdapter.class);

	private VisualizzaPrescrittoRicevutaDocument response;
	private TEPrescription request;

	public ServizioRecuperoResponseAdapter(TEPrescription request, VisualizzaPrescrittoRicevutaDocument response) {
		super();
		this.response = response;
		this.request = request;
	}

	public TEPrescription getEPrescriptionDocument() throws MwReqAdapterException {
		TEPrescription ePrescriptionResponse = TEPrescription.Factory.newInstance();
		VisualizzaPrescrittoRicevuta visualizzaPrescrittoRicevuta = this.response.getVisualizzaPrescrittoRicevuta();
		this.setIfNotEmpty(ePrescriptionResponse, "id", this.request.getId());
		this.setIfNotEmpty(ePrescriptionResponse, "esito", visualizzaPrescrittoRicevuta.getCodEsitoVisualizzazione());
		TPaziente paziente = ePrescriptionResponse.addNewPaziente();
		// this.setIfNotEmpty(paziente, "id", "");
		this.setDate(paziente, "dataScadenzaTS", visualizzaPrescrittoRicevuta.getDataScadTessera());
		this.setIfNotEmpty(paziente, "codiceEsenzione", visualizzaPrescrittoRicevuta.getCodEsenzione());
		// TODO this.setIfNotEmpty(paziente, "fasciaReddito", "");
		// TODO this.setIfNotEmpty(paziente, "reddito",
		// visualizzaPrescrittoRicevuta.getReddito());
		this.setIfNotEmpty(paziente, "codiceFiscale", visualizzaPrescrittoRicevuta.getCodiceAss());
		this.setIfNotEmpty(paziente, "regioneResidenza", visualizzaPrescrittoRicevuta.getCodRegione());
		this.setIfNotEmpty(paziente, "aslResidenza", visualizzaPrescrittoRicevuta.getAslAssistito());
		this.setIfNotEmpty(paziente, "provinciaResidenza", visualizzaPrescrittoRicevuta.getProvAssistito());
		this.setIfNotEmpty(paziente, "regioneAssistenza", visualizzaPrescrittoRicevuta.getCodRegione());
		this.setIfNotEmpty(paziente, "aslAssistenza", visualizzaPrescrittoRicevuta.getCodASLAo());
		this.setIfNotEmpty(paziente, "indirizzoDomicilio", visualizzaPrescrittoRicevuta.getIndirizzo());
		this.setIfNotEmpty(paziente, "indirizzoResidenza", visualizzaPrescrittoRicevuta.getIndirizzo());
		// this.setIfNotEmpty(paziente, "cognome", "");
		// this.setIfNotEmpty(paziente, "nome", "");
		// this.setIfNotEmpty(paziente, "sesso", "");
		// this.setDate(paziente, "dataNascita", "");
		// this.setIfNotEmpty(paziente, "comuneNascita", "");
		// this.setIfNotEmpty(paziente, "cittadinanza", "");
		this.setIfNotEmpty(paziente, "numeroSASN", visualizzaPrescrittoRicevuta.getNumTessSasn());
		this.setIfNotEmpty(paziente, "societaSASN", visualizzaPrescrittoRicevuta.getSocNavigaz());
		// this.setIfNotEmpty(paziente, "telefono", "");
		// this.setIfNotEmpty(paziente, "email", "");
		this.setIfNotEmpty(paziente, "istituzioneEstero", visualizzaPrescrittoRicevuta.getIstituzCompetente());
		this.setIfNotEmpty(paziente, "statoEstero", visualizzaPrescrittoRicevuta.getStatoEstero());
		this.setIfNotEmpty(paziente, "identificazioneEstero", visualizzaPrescrittoRicevuta.getNumIdentPers());
		this.setIfNotEmpty(paziente, "tesseraEstero", visualizzaPrescrittoRicevuta.getNumIdentTess());
		// this.setIfNotEmpty(paziente, "consenso", "");
		TMedico medico = ePrescriptionResponse.addNewMedico();
		this.setIfNotEmpty(medico, "rapporto", visualizzaPrescrittoRicevuta.getCodSpecializzazione());
		this.setIfNotEmpty(medico, "codiceFiscale", visualizzaPrescrittoRicevuta.getCfMedico1());
		this.setIfNotEmpty(medico, "regione", visualizzaPrescrittoRicevuta.getCodRegione());
		this.setIfNotEmpty(medico, "asl", visualizzaPrescrittoRicevuta.getCodASLAo());
		this.setIfNotEmpty(medico, "struttura", visualizzaPrescrittoRicevuta.getCodStruttura());
		// this.setIfNotEmpty(medico, "cognome", "");
		// this.setIfNotEmpty(medico, "nome", "");
		// this.setIfNotEmpty(medico, "codiceRegionale", "");
		// this.setIfNotEmpty(medico, "indirizzo", "");
		this.setIfNotEmpty(medico, "codiceFiscaleCompilante", visualizzaPrescrittoRicevuta.getCfMedico2());
		// this.setIfNotEmpty(medico, "cognomeCompilante", "");
		// this.setIfNotEmpty(medico, "nomeCompilante", "");
		TRicetta ricetta = ePrescriptionResponse.addNewRicetta();
		this.setIfNotEmpty(ricetta, "NRE", visualizzaPrescrittoRicevuta.getNre());
		this.setIfNotEmpty(ricetta, "codiceAutenticazione", visualizzaPrescrittoRicevuta.getCodAutenticazione());
		// this.setIfNotEmpty(ricetta, "protocolloSAC", "");
		this.setIfNotEmpty(ricetta, "tipoPrescrizione", visualizzaPrescrittoRicevuta.getTipoPrescrizione());
		this.transcodeAndSet1Null(ricetta, "interna", visualizzaPrescrittoRicevuta.getRicettaInterna());
		// this.setIfNotEmpty(ricetta, "aziendaPercorso", "");
		// this.setIfNotEmpty(ricetta, "strutturaPercorso", "");
		// this.setIfNotEmpty(ricetta, "codicePercorso", "");
		// this.setIfNotEmpty(ricetta, "descrizionePercorso", "");
		this.setIfNotEmpty(ricetta, "codiceRegionale", visualizzaPrescrittoRicevuta.getNre());
		this.transcodeAndSet1Null(ricetta, "oscuramento", visualizzaPrescrittoRicevuta.getOscuramDati());
		this.setIfNotEmpty(ricetta, "tipoRicetta", visualizzaPrescrittoRicevuta.getTipoRic());
		this.setIfNotEmpty(ricetta, "tipoVisita", visualizzaPrescrittoRicevuta.getTipoVisita());
		this.setIfNotEmpty(ricetta, "disposizioniRegionali", visualizzaPrescrittoRicevuta.getDispReg());
		this.setIfNotEmpty(ricetta, "indicazione", visualizzaPrescrittoRicevuta.getIndicazionePrescr());
		this.setIfNotEmpty(ricetta, "altro", visualizzaPrescrittoRicevuta.getAltro());
		this.setIfNotEmpty(ricetta, "priorita", visualizzaPrescrittoRicevuta.getClassePriorita());
		this.setIfNotEmpty(ricetta, "codiceDiagnosi", visualizzaPrescrittoRicevuta.getCodDiagnosi());
		this.setIfNotEmpty(ricetta, "quesito", visualizzaPrescrittoRicevuta.getDescrizioneDiagnosi());
		// this.setIfNotEmpty(ricetta, "propostaTerapeutica", "");

		ElencoDettagliPrescrizioniType elencoDettagli = visualizzaPrescrittoRicevuta.getElencoDettagliPrescrizioni();
		if (elencoDettagli != null) {
			DettaglioPrescrizioneType[] dettagli = elencoDettagli.getDettaglioPrescrizioneArray();
			if (dettagli != null && dettagli.length > 0) {
				this.setIfNotEmpty(ricetta, "quantita", String.valueOf(dettagli.length));
				TDettagli listaDettagli = ricetta.addNewDettagli();
				for (int i = 0; i < dettagli.length; i++) {
					DettaglioPrescrizioneType dettaglioPrescrizione = dettagli[i];
					TDettaglio dettaglio = listaDettagli.addNewDettaglio();
					this.setIfNotEmpty(dettaglio, "progressivo", String.valueOf(i));
					this.setIfNotEmpty(dettaglio, "codice", dettaglioPrescrizione.getCodProdPrest());
					// this.setIfNotEmpty(dettaglio, "atc", "");
					this.setIfNotEmpty(dettaglio, "codiceCatalogoRegionale", dettaglioPrescrizione.getPrescrizione2());
					this.setIfNotEmpty(dettaglio, "codiceCatalogoAziendale", dettaglioPrescrizione.getPrescrizione1());
					// Il codice della branca è presente solo nel dataset
					// dell'erogazione
					// this.setIfNotEmpty(dettaglio, "branca", "");
					this.setIfNotEmpty(dettaglio, "descrizione", dettaglioPrescrizione.getDescrProdPrest());
					// this.setIfNotEmpty(dettaglio, "codicePacchetto", "");
					this.transcodeAndSet1Null(dettaglio, "nonSostituibile", dettaglioPrescrizione.getNonSost());
					this.setIfNotEmpty(dettaglio, "motivazione", dettaglioPrescrizione.getCodMotivazione());
					// this.setIfNotEmpty(ricetta, "note", "");
					this.transcodeAndSet1Null(dettaglio, "testoLibero", dettaglioPrescrizione.getTestoLibero());
					this.setIfNotEmpty(dettaglio, "descrizioneTestoLibero", dettaglioPrescrizione.getDescrTestoLiberoNote());
					this.setIfNotEmpty(dettaglio, "codiceGruppo", dettaglioPrescrizione.getCodGruppoEquival());
					this.setIfNotEmpty(dettaglio, "descrizioneGruppo", dettaglioPrescrizione.getDescrGruppoEquival());
					this.setIfNotEmpty(dettaglio, "quantita", dettaglioPrescrizione.getQuantita());
					this.setIfNotEmpty(dettaglio, "numsedute", dettaglioPrescrizione.getNumsedute());
					this.setIfNotEmpty(dettaglio, "notaAIFA", dettaglioPrescrizione.getNotaProd());
					// this.setIfNotEmpty(dettaglio, "posologia", "");
					//TODO Valori aggiunti per l'adeguamento alle nuove specifiche tecniche SAC
					//TODO codiceCatalogoRegionale è già settato a partire da prescrizione2
					//this.setIfNotEmpty(dettaglio, "codiceCatalogoRegionale", dettaglioPrescrizione.getCodCatalogoPrescr());
					this.transcodeAndSetAccessType(dettaglio, "tipoAccesso", dettaglioPrescrizione.getTipoAccesso());
					//XXX Valori aggiunti per decreto Lorenzin DM 9 dic 2016, specifiche SAC 01/03/2016
					this.setIfNotEmpty(dettaglio, "numeroNota", dettaglioPrescrizione.getNumeroNota());
					this.setIfNotEmpty(dettaglio, "condErogabilita", dettaglioPrescrizione.getCondErogabilita());
					this.setIfNotEmpty(dettaglio, "approprPrescrittiva", dettaglioPrescrizione.getApproprPrescrittiva());
					this.setIfNotEmpty(dettaglio, "patologia", dettaglioPrescrizione.getPatologia());
					//XXX Fine valori aggiunti per decreto Lorenzin DM 9 dic 2016, specifiche SAC 01/03/2016

				}
			}
		}
		TErrore[] errori = this.getErroriAndComunicazioniArray(visualizzaPrescrittoRicevuta.getElencoComunicazioni(), visualizzaPrescrittoRicevuta.getElencoErroriRicette());
		if (errori != null && errori.length > 0) {
			ePrescriptionResponse.addNewErrori().setErroreArray(errori);
		}
		return ePrescriptionResponse;
	}

}
