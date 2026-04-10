package it.eng.cct.dem.sar.mw.piemonte.adapters;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.cct.dem.sar.mw.exceptions.MwReqAdapterException;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TEPrescription;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TEPrescriptions;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TErrore;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TRicetta;
import it.finanze.sanita.dem.xsd.interroganreutilricevuta.InterrogaNreUtilRicevutaDocument;
import it.finanze.sanita.dem.xsd.interroganreutilricevuta.InterrogaNreUtilRicevutaDocument.InterrogaNreUtilRicevuta;
import it.finanze.sanita.dem.xsd.tipodati.ElencoNreUtilRecordType;
import it.finanze.sanita.dem.xsd.tipodati.NreUtilRecordType;

public class ServizioInterrogaResponseAdapter extends AreasEPrescriptionAdapter {

	private static final Logger logger = LoggerFactory.getLogger(ServizioInterrogaResponseAdapter.class);

	private InterrogaNreUtilRicevutaDocument response;
	private TEPrescriptions request;

	public ServizioInterrogaResponseAdapter(TEPrescriptions request, InterrogaNreUtilRicevutaDocument response) {
		super();
		this.response = response;
		this.request = request;
	}

	public TEPrescriptions getEPrescriptionsDocument() throws MwReqAdapterException {
		TEPrescriptions ePrescriptionsResponse = TEPrescriptions.Factory.newInstance();
		InterrogaNreUtilRicevuta interrogaNreUtilRicevuta = this.response.getInterrogaNreUtilRicevuta();
		String codEsitoInterrogaNreUtilizzati = interrogaNreUtilRicevuta.getCodEsitoInterrogaNreUtilizzati();
		TErrore[] errori = ServizioInterrogaResponseAdapter.getErroriAndComunicazioniArray(interrogaNreUtilRicevuta.getElencoComunicazioni(), interrogaNreUtilRicevuta.getElencoErroriRicette());
		if (!codEsitoInterrogaNreUtilizzati.equals("0000")) {
			TEPrescription ePrescription = ePrescriptionsResponse.addNewEPrescription();
			ePrescription.setEsito(codEsitoInterrogaNreUtilizzati);
			if (errori!= null && errori.length > 0) {
				ePrescription.addNewErrori().setErroreArray(errori);
			}
			return ePrescriptionsResponse;
		}
		ElencoNreUtilRecordType elencoNreUtilRecord = interrogaNreUtilRicevuta.getElencoNreUtilRecord();
		
		if (elencoNreUtilRecord != null) {
			NreUtilRecordType[] nreUtilRecordArray = elencoNreUtilRecord.getNreUtilRecordArray();
			if (nreUtilRecordArray != null) {
				int recordCount = nreUtilRecordArray.length;
				TEPrescription[] ePrescriptionArray = new TEPrescription[recordCount];
				for (int i = 0; i < recordCount; i++) {
					TEPrescription currentEPrescription = ePrescriptionArray[i] = TEPrescription.Factory.newInstance();
					NreUtilRecordType currentRecord = nreUtilRecordArray[i];
					currentEPrescription.setEsito(codEsitoInterrogaNreUtilizzati);
					if (errori != null && errori.length > 0) {
						currentEPrescription.addNewErrori().setErroreArray(errori);
					}
					String cfAssistito = currentRecord.getCfAssistito();
					if (StringUtils.isNotEmpty(cfAssistito)) {
						currentEPrescription.addNewPaziente().setCodiceFiscale(cfAssistito);
					}
					String cfMedico = currentRecord.getCfMedico();
					if (StringUtils.isNotEmpty(cfMedico)) {
						currentEPrescription.addNewMedico().setCodiceFiscale(cfMedico);
					}
					TRicetta ricetta = currentEPrescription.addNewRicetta();
					this.setIfNotEmpty(ricetta, "dataCompilazione", currentRecord.getDataCompilazioneRicetta());
					this.setIfNotEmpty(ricetta, "codiceAutenticazione", currentRecord.getCodAutenticazione());
					this.setIfNotEmpty(ricetta, "nRE", currentRecord.getNre());
					currentEPrescription.setNRE(currentRecord.getNre());
					this.setIfNotEmpty(ricetta, "tipoPrescrizione", currentRecord.getTipoPrescrizione());
					// TODO Verificare opportunità di aggiungere alla response anche lotto e provenienza
					//currentRecord.getLotto();
					//currentRecord.getProvenienza();
				}
				ePrescriptionsResponse.setEPrescriptionArray(ePrescriptionArray);
			}
		}
		return ePrescriptionsResponse;
	}

}
