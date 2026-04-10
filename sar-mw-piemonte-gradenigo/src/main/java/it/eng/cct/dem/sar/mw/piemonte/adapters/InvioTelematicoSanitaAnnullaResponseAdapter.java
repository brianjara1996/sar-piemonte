package it.eng.cct.dem.sar.mw.piemonte.adapters;

import it.eng.cct.dem.sar.mw.exceptions.MwReqAdapterException;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TEPrescription;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TEPrescriptions;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TMedico;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TRicetta;
import it.finanze.sanita.dem.dpcm.wsdl.invioTelematicoSanita.service.RicevutaDocument;

public class InvioTelematicoSanitaAnnullaResponseAdapter extends InvioTelematicoSanitaResponseAdapter {

	public InvioTelematicoSanitaAnnullaResponseAdapter(TEPrescriptions request, RicevutaDocument response) {
		super(request, response);
	}
	
	public TEPrescriptions getEPrescriptionsDocument() throws MwReqAdapterException {
		return this.getEPrescriptionsDocument(this.cloneRequestedEPrescriptions());
	}

	private TEPrescription[] cloneRequestedEPrescriptions() {
		TEPrescription[] reqEPrescriptionArray = super.request.getEPrescriptionArray();
		int numEPrescriptions = reqEPrescriptionArray.length;
		TEPrescription[] respEPrescriptionArray = new TEPrescription[numEPrescriptions];
		for (int i = 0; i < numEPrescriptions; i++) {
			TEPrescription requested = reqEPrescriptionArray[i];
			TEPrescription result = respEPrescriptionArray[i] = TEPrescription.Factory.newInstance();
			result.setId(requested.getId());
			TMedico medico = result.addNewMedico();
			medico.setCodiceFiscale(requested.getMedico().getCodiceFiscale());
			medico.setCodiceFiscaleCompilante(requested.getMedico().getCodiceFiscaleCompilante());
			result.setRicetta((TRicetta)requested.getRicetta().copy());
			result.getRicetta().setCodiceRegionale(result.getRicetta().getNRE());
		}
		return respEPrescriptionArray;
	}

}
