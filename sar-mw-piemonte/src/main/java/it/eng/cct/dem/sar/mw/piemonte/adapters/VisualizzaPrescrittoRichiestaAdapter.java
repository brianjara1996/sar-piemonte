package it.eng.cct.dem.sar.mw.piemonte.adapters;

import it.eng.cct.dem.sar.generic.wsclient.commons.utils.ConfigKeys;
import it.eng.cct.dem.sar.generic.wsclient.commons.utils.ModuleConfig;
import it.eng.cct.dem.sar.generic.wsclient.params.IServiceInput;
import it.eng.cct.dem.sar.generic.wsclient.params.impl.Action;
import it.eng.cct.dem.sar.generic.wsclient.params.impl.Login;
import it.eng.cct.dem.sar.generic.wsclient.params.impl.ServiceInput;
import it.eng.cct.dem.sar.mw.adapters.ServiceInputAdapter;
import it.eng.cct.dem.sar.mw.exceptions.MwReqAdapterException;
import it.eng.cct.dem.sar.mw.piemonte.constants.DemVisualizzaPrescrittoConstants;
import it.eng.cct.dem.sar.mw.piemonte.constants.InvioTelematicoSanitaConstants;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TEPrescription;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TMedico;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TRicetta;
import it.finanze.sanita.dem.xsd.visualizzaprescrittorichiesta.VisualizzaPrescrittoRichiestaDocument;
import it.finanze.sanita.dem.xsd.visualizzaprescrittorichiesta.VisualizzaPrescrittoRichiestaDocument.VisualizzaPrescrittoRichiesta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * 
 * 
 */
public class VisualizzaPrescrittoRichiestaAdapter extends SistemaTiesseAdapter implements ServiceInputAdapter {

	private TEPrescription epRequest;
	private static final Logger logger = LoggerFactory.getLogger(VisualizzaPrescrittoRichiestaAdapter.class);
	
	
	public VisualizzaPrescrittoRichiestaAdapter(TEPrescription epRequest) {
		super();
		this.epRequest = epRequest;
	}

	public IServiceInput getServiceInput() throws MwReqAdapterException {
		IServiceInput serviceInput = new ServiceInput();
		serviceInput.setAction(new Action(ModuleConfig.getProperty(DemVisualizzaPrescrittoConstants.CONFIG_CIL_DEMVISUALIZZAPRESCRITTO_SERVICE_NAME), ModuleConfig.getProperty(DemVisualizzaPrescrittoConstants.CONFIG_CIL_DEMVISUALIZZAPRESCRITTO_OPERATION_NAME), ModuleConfig.getProperty(DemVisualizzaPrescrittoConstants.CONFIG_CIL_DEMVISUALIZZAPRESCRITTO_SERVICE_ENDPOINT)));
		serviceInput.setSecurityConfigId(ModuleConfig.getProperty(DemVisualizzaPrescrittoConstants.CONFIG_CIL_DEMVISUALIZZAPRESCRITTO_SECCONFIGID));
		serviceInput.setRequestMessage(this.toInputMessage(this.epRequest));
		serviceInput.setLogin(new Login(ModuleConfig.getProperty(ConfigKeys.CONFIG_AXIS2_CLIENT_HTTP_AUTH_BASIC_USERNAME), ModuleConfig.getProperty(ConfigKeys.CONFIG_AXIS2_CLIENT_HTTP_AUTH_BASIC_PASSWORD)));
		serviceInput.setCustomProps(ModuleConfig.getPropertiesByPrefix(DemVisualizzaPrescrittoConstants.CONFIG_CIL_DEMVISUALIZZAPRESCRITTO_ROOT + ServiceInputAdapter.CUSTOM_PROPS_KEY, true));
		return serviceInput;
	}

	private String toInputMessage(TEPrescription request) throws MwReqAdapterException {
		VisualizzaPrescrittoRichiestaDocument visualizzaPrescrittoRichiestaDocument = VisualizzaPrescrittoRichiestaDocument.Factory.newInstance();
		VisualizzaPrescrittoRichiesta visualizzaPrescrittoRichiesta = visualizzaPrescrittoRichiestaDocument.addNewVisualizzaPrescrittoRichiesta();
		this.setMandatory(visualizzaPrescrittoRichiesta, "pinCode", ModuleConfig.getProperty(DemVisualizzaPrescrittoConstants.CONFIG_CIL_PINCODE));		
		TMedico medico = request.getMedico();
		if (medico== null) {
			throw new MwReqAdapterException("Property medico cannot be null!");
		}
		this.setMandatory(visualizzaPrescrittoRichiesta, "cfMedico" , medico.getCodiceFiscale());
		TRicetta ricetta = request.getRicetta();
		if (ricetta == null) {
			throw new MwReqAdapterException("Property ricetta cannot be null!");
		}
		this.setMandatory(visualizzaPrescrittoRichiesta, "nre", ricetta.getNRE());
		return visualizzaPrescrittoRichiestaDocument.xmlText();
	}

}
