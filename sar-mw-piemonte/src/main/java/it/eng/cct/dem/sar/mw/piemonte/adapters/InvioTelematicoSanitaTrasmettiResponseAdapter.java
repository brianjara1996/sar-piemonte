package it.eng.cct.dem.sar.mw.piemonte.adapters;

import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TEPrescriptions;
import it.finanze.sanita.dem.dpcm.wsdl.invioTelematicoSanita.service.RicevutaDocument;

public class InvioTelematicoSanitaTrasmettiResponseAdapter extends InvioTelematicoSanitaResponseAdapter {

	public InvioTelematicoSanitaTrasmettiResponseAdapter(TEPrescriptions request, RicevutaDocument response) {
		super(request, response);
	}

}
