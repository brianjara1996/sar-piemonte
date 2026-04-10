package it.eng.cct.dem.sar.mw.piemonte.adapters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.cct.dem.sar.mw.exceptions.MwReqAdapterException;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TEPrescription;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TEPrescriptions;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TErrore;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TErrori;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TListaMessaggi;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TMessageObj;
import it.finanze.sanita.dem.dpcm.wsdl.elencoAnaliticoEsitoRicette.service.VisualizzaElencoStatoRicetteResponseDocument;
import it.finanze.sanita.dem.dpcm.wsdl.elencoAnaliticoEsitoRicette.service.VisualizzaElencoStatoRicetteResponseDocument.VisualizzaElencoStatoRicetteResponse;
import it.finanze.sanita.mir.auxservices.dto.elencoanaliticoesitoricette.ArrayOfElencoEsitoRicetteRecord;
import it.finanze.sanita.mir.auxservices.dto.elencoanaliticoesitoricette.ArrayOfErrori;
import it.finanze.sanita.mir.auxservices.dto.elencoanaliticoesitoricette.ArrayOfTns3NillableMessageObj;
import it.finanze.sanita.mir.auxservices.dto.elencoanaliticoesitoricette.ElencoAnaliticoEsitoRicetteDTO;
import it.finanze.sanita.mir.auxservices.dto.elencoanaliticoesitoricette.ElencoEsitoRicetteRecord;
import it.finanze.sanita.mir.auxservices.dto.elencoanaliticoesitoricette.Errori;
import it.finanze.sanita.simossssa.dto.MessageObj;

public class ElencoAnaliticoEsitoRicetteResponseAdapter extends AreasEPrescriptionAdapter {

	private static final Logger logger = LoggerFactory.getLogger(ElencoAnaliticoEsitoRicetteResponseAdapter.class);

	private VisualizzaElencoStatoRicetteResponseDocument response;
	private TEPrescriptions request;

	public ElencoAnaliticoEsitoRicetteResponseAdapter(TEPrescriptions request, VisualizzaElencoStatoRicetteResponseDocument response) {
		super();
		this.response = response;
		this.request = request;
	}

	public TEPrescriptions getEPrescriptionsDocument() throws MwReqAdapterException {
		TEPrescriptions ePrescriptionsResponse = TEPrescriptions.Factory.newInstance();
		VisualizzaElencoStatoRicetteResponse visualizzaElencoStatoRicetteResponse = this.response.getVisualizzaElencoStatoRicetteResponse();
		ElencoAnaliticoEsitoRicetteDTO elencoAnaliticoEsitoRicetteDTO = visualizzaElencoStatoRicetteResponse.getVisualizzaElencoStatoRicetteReturn();
		if (!elencoAnaliticoEsitoRicetteDTO.getIsOperazioneValida()) {
			ArrayOfTns3NillableMessageObj arrayOfTns3NillableMessageObj = elencoAnaliticoEsitoRicetteDTO.getListaMessaggi();
			if (arrayOfTns3NillableMessageObj != null) {
				MessageObj[] messageObjs = arrayOfTns3NillableMessageObj.getMessageObjArray();
				if (messageObjs != null && messageObjs.length > 0) {
					TListaMessaggi listaMessaggi = ePrescriptionsResponse.addNewListaMessaggi();
					for (MessageObj messageObj : messageObjs) {
						TMessageObj newMsg = listaMessaggi.addNewMessageObj();
						this.setIfNotEmpty(newMsg, "codiceMessaggio", messageObj.getCodiceMessaggio());
						this.setIfNotEmpty(newMsg, "descrizioneMessaggio", messageObj.getDescrizioneMessaggio());
					}
				}
			}
		}
		ArrayOfElencoEsitoRicetteRecord arrayOfElencoEsitoRicetteRecord = elencoAnaliticoEsitoRicetteDTO.getElencoEsitoRicetteRecords();
		if (arrayOfElencoEsitoRicetteRecord != null) {
			ElencoEsitoRicetteRecord[] elencoEsitoRicetteRecords = arrayOfElencoEsitoRicetteRecord.getElencoEsitoRicetteRecordArray();
			if (elencoEsitoRicetteRecords != null && elencoEsitoRicetteRecords.length > 0) {
				for (ElencoEsitoRicetteRecord elencoEsitoRicetteRecord : elencoEsitoRicetteRecords) {
					TEPrescription ePrescription = ePrescriptionsResponse.addNewEPrescription();
					this.setIfNotEmpty(ePrescription, "codRicetta", elencoEsitoRicetteRecord.getCodRicetta());
					this.setDate(ePrescription, "dataAccoglienza", elencoEsitoRicetteRecord.getDataAccoglienza(), DPCM_ESITI_DATE_PATTERN);
					this.setIfNotEmpty(ePrescription, "protocolloSAC", elencoEsitoRicetteRecord.getProtocolloSac());
					ArrayOfErrori arrayOfErrori = elencoEsitoRicetteRecord.getErrori();
					if (arrayOfErrori != null) {
						Errori[] errori = arrayOfErrori.getErroriArray();
						if (errori != null && errori.length > 0) {
							TErrori erroriEp = ePrescription.addNewErrori();
							for (Errori errore: errori) {
								TErrore erroreEp = erroriEp.addNewErrore();
								this.setIfNotEmpty(erroreEp, "codice", errore.getCodice());
								this.setIfNotEmpty(erroreEp, "descrizione", errore.getDescrizione());
							}
						}
					}
				}
			}
		}
		return ePrescriptionsResponse;
	}

}
