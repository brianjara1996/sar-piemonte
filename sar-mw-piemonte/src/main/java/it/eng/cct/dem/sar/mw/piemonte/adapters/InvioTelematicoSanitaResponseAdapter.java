package it.eng.cct.dem.sar.mw.piemonte.adapters;

import it.eng.cct.dem.sar.mw.exceptions.MwReqAdapterException;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TEPrescription;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TEPrescriptions;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TMedico;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TPaziente;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TRicetta;
import it.finanze.sanita.dem.dpcm.wsdl.invioTelematicoSanita.service.RicevutaDocument;
import it.finanze.sanita.dem.dpcm.wsdl.invioTelematicoSanita.service.RicevutaSAC;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InvioTelematicoSanitaResponseAdapter extends AreasEPrescriptionAdapter {

	private static final Logger logger = LoggerFactory.getLogger(InvioTelematicoSanitaResponseAdapter.class);

	protected RicevutaDocument response;
	protected TEPrescriptions request;

	public InvioTelematicoSanitaResponseAdapter(TEPrescriptions request, RicevutaDocument response) {
		super();
		this.response = response;
		this.request = request;
	}

	public TEPrescriptions getEPrescriptionsDocument() throws MwReqAdapterException {
		return this.getEPrescriptionsDocument(this.cloneRequestedEPrescriptions());
	}
	
	protected TEPrescriptions getEPrescriptionsDocument(TEPrescription[] respEPrescriptionArray) throws MwReqAdapterException {
		RicevutaSAC ricevutaSAC = this.response.getRicevuta();
		String codiceEsito = ricevutaSAC.getCodiceEsito();
		String dataAccoglienza = ricevutaSAC.getDataAccoglienza();
		String descrizioneEsito = ricevutaSAC.getDescrizioneEsito();
		String dimensioneFileAllegato = ricevutaSAC.getDimensioneFileAllegato();
		String nomeFileAllegato = ricevutaSAC.getNomeFileAllegato();
		String protocolloSAC = ricevutaSAC.getProtocolloSAC();
		for (int i = 0; i < respEPrescriptionArray.length; i++) {
			TEPrescription currentEPrescription = respEPrescriptionArray[i];
			currentEPrescription.setEsito(codiceEsito);
			TRicetta currentRicetta = currentEPrescription.getRicetta();
			currentRicetta.setNumeroDPCM2008(protocolloSAC);
			currentRicetta.setDataAccoglienza(dataAccoglienza);
		}
		TEPrescriptions ePrescriptionsResponse = TEPrescriptions.Factory.newInstance();
		ePrescriptionsResponse.setEPrescriptionArray(respEPrescriptionArray);
		return ePrescriptionsResponse;
	}


	private TEPrescription[] cloneRequestedEPrescriptions() {
		TEPrescription[] reqEPrescriptionArray = this.request.getEPrescriptionArray();
		int numEPrescriptions = reqEPrescriptionArray.length;
		TEPrescription[] respEPrescriptionArray = new TEPrescription[numEPrescriptions];
		for (int i = 0; i < numEPrescriptions; i++) {
			TEPrescription requested = reqEPrescriptionArray[i];
			TEPrescription result = respEPrescriptionArray[i] = TEPrescription.Factory.newInstance();
			result.setId(requested.getId());
			TPaziente paziente = result.addNewPaziente();
			paziente.setCodiceFiscale(requested.getPaziente().getCodiceFiscale());
			TMedico medico = result.addNewMedico();
			medico.setCodiceFiscale(requested.getMedico().getCodiceFiscale());
			medico.setCodiceFiscaleCompilante(requested.getMedico().getCodiceFiscaleCompilante());
			result.setRicetta((TRicetta)requested.getRicetta().copy());
			result.getRicetta().setCodiceRegionale(result.getRicetta().getNRE());
		}
		return respEPrescriptionArray;
	}
}
