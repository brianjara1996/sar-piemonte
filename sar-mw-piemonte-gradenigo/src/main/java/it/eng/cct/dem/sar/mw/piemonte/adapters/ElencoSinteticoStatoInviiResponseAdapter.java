package it.eng.cct.dem.sar.mw.piemonte.adapters;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.cct.dem.sar.mw.exceptions.MwReqAdapterException;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TEPrescription;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TEPrescriptions;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TListaMessaggi;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TMessageObj;
import it.finanze.sanita.dem.dpcm.wsdl.elencoSinteticoStatoInvii.service.VisualizzaElencoStatoInviiResponseDocument;
import it.finanze.sanita.dem.dpcm.wsdl.elencoSinteticoStatoInvii.service.VisualizzaElencoStatoInviiResponseDocument.VisualizzaElencoStatoInviiResponse;
import it.finanze.sanita.mir.auxservices.dto.elencosinteticostatoinvii.ArrayOfElencoStatoInviiRecord;
import it.finanze.sanita.mir.auxservices.dto.elencosinteticostatoinvii.ArrayOfTns3NillableMessageObj;
import it.finanze.sanita.mir.auxservices.dto.elencosinteticostatoinvii.ElencoSinteticoStatoInviiDTO;
import it.finanze.sanita.mir.auxservices.dto.elencosinteticostatoinvii.ElencoStatoInviiRecord;
import it.finanze.sanita.simossssa.dto.MessageObj;

public class ElencoSinteticoStatoInviiResponseAdapter extends AreasEPrescriptionAdapter {

	private static final Logger logger = LoggerFactory.getLogger(ElencoSinteticoStatoInviiResponseAdapter.class);

	private VisualizzaElencoStatoInviiResponseDocument response;
	private TEPrescriptions request;

	public ElencoSinteticoStatoInviiResponseAdapter(TEPrescriptions request, VisualizzaElencoStatoInviiResponseDocument response) {
		super();
		this.response = response;
		this.request = request;
	}

	public TEPrescriptions getEPrescriptionsDocument() throws MwReqAdapterException {
		TEPrescriptions ePrescriptionsResponse = TEPrescriptions.Factory.newInstance();
		VisualizzaElencoStatoInviiResponse visualizzaElencoStatoInviiResponse = this.response.getVisualizzaElencoStatoInviiResponse();
		ElencoSinteticoStatoInviiDTO elencoSinteticoStatoInviiDTO = visualizzaElencoStatoInviiResponse.getVisualizzaElencoStatoInviiReturn();
		if (!elencoSinteticoStatoInviiDTO.getIsOperazioneValida()) {
			ArrayOfTns3NillableMessageObj arrayOfTns3NillableMessageObj = elencoSinteticoStatoInviiDTO.getListaMessaggi();
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
		ArrayOfElencoStatoInviiRecord arrayOfElencoStatoInviiRecord = elencoSinteticoStatoInviiDTO.getArrayRecordStatoInvii();
		if (arrayOfElencoStatoInviiRecord != null) {
			ElencoStatoInviiRecord[] elencoStatoInviiRecords = arrayOfElencoStatoInviiRecord.getElencoStatoInviiRecordArray();
			if (elencoStatoInviiRecords != null && elencoStatoInviiRecords.length> 0) {
				for (ElencoStatoInviiRecord elencoStatoInviiRecord: elencoStatoInviiRecords) {
					TEPrescription ePrescription = ePrescriptionsResponse.addNewEPrescription();
					String dataInvio = elencoStatoInviiRecord.getDataInvio();
					if (StringUtils.isNotEmpty(dataInvio)) {
						this.setDate(ePrescription, "dataInvio", StringUtils.substring(dataInvio, 0, dataInvio.length() - 3), DPCM_ESITI_DATE_PATTERN);
					}
					this.setIfNotEmpty(ePrescription, "protocolloSAC", elencoStatoInviiRecord.getProtocolloSac());
					this.setIfNotEmpty(ePrescription, "statoInvio", elencoStatoInviiRecord.getStatoInvio());
				}
			}
		}
		return ePrescriptionsResponse;
	}
}
