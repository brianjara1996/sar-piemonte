package it.eng.cct.dem.sar.mw.piemonte.adapters;

import it.eng.cct.dem.sar.generic.wsclient.commons.utils.ConfigKeys;
import it.eng.cct.dem.sar.generic.wsclient.commons.utils.ModuleConfig;
import it.eng.cct.dem.sar.generic.wsclient.params.IServiceInput;
import it.eng.cct.dem.sar.generic.wsclient.params.impl.Action;
import it.eng.cct.dem.sar.generic.wsclient.params.impl.Login;
import it.eng.cct.dem.sar.generic.wsclient.params.impl.ServiceInput;
import it.eng.cct.dem.sar.mw.adapters.ServiceInputAdapter;
import it.eng.cct.dem.sar.mw.exceptions.MwReqAdapterException;
import it.eng.cct.dem.sar.mw.piemonte.constants.DemAnnullaPrescrittoConstants;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TEPrescription;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TMedico;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TRicetta;
import it.finanze.sanita.dem.xsd.annullaprescrittorichiesta.AnnullaPrescrittoRichiestaDocument;
import it.finanze.sanita.dem.xsd.annullaprescrittorichiesta.AnnullaPrescrittoRichiestaDocument.AnnullaPrescrittoRichiesta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * 
 * 
 */
public class AnnullaPrescrittoRichiestaAdapter extends SistemaTiesseAdapter implements ServiceInputAdapter {

	private TEPrescription request;
	private static final Logger logger = LoggerFactory.getLogger(AnnullaPrescrittoRichiestaAdapter.class);

	public AnnullaPrescrittoRichiestaAdapter(TEPrescription request) {
		super();
		this.request = request;
	}

	public IServiceInput getServiceInput() throws MwReqAdapterException {
		IServiceInput serviceInput = new ServiceInput();
		serviceInput.setAction(new Action(ModuleConfig.getProperty(DemAnnullaPrescrittoConstants.CONFIG_CIL_DEMANNULLAPRESCRITTO_SERVICE_NAME), ModuleConfig.getProperty(DemAnnullaPrescrittoConstants.CONFIG_CIL_DEMANNULLAPRESCRITTO_OPERATION_NAME), ModuleConfig.getProperty(DemAnnullaPrescrittoConstants.CONFIG_CIL_DEMANNULLAPRESCRITTO_SERVICE_ENDPOINT)));
		serviceInput.setSecurityConfigId(ModuleConfig.getProperty(DemAnnullaPrescrittoConstants.CONFIG_CIL_DEMANNULLAPRESCRITTO_SECCONFIGID));
		serviceInput.setRequestMessage(this.toInputMessage(this.request));
		serviceInput.setLogin(new Login(ModuleConfig.getProperty(ConfigKeys.CONFIG_AXIS2_CLIENT_HTTP_AUTH_BASIC_USERNAME), ModuleConfig.getProperty(ConfigKeys.CONFIG_AXIS2_CLIENT_HTTP_AUTH_BASIC_PASSWORD)));
		serviceInput.setCustomProps(ModuleConfig.getPropertiesByPrefix(DemAnnullaPrescrittoConstants.CONFIG_CIL_DEMANNULLAPRESCRITTO_ROOT + ServiceInputAdapter.CUSTOM_PROPS_KEY, true));
		return serviceInput;
	}

	private String toInputMessage(TEPrescription epRequest) throws MwReqAdapterException {
		AnnullaPrescrittoRichiestaDocument annullaPrescrittoRichiestaDocument = AnnullaPrescrittoRichiestaDocument.Factory.newInstance();
		AnnullaPrescrittoRichiesta annullaPrescrittoRichiesta = annullaPrescrittoRichiestaDocument.addNewAnnullaPrescrittoRichiesta();
		this.setMandatory(annullaPrescrittoRichiesta, "pinCode", ModuleConfig.getProperty(DemAnnullaPrescrittoConstants.CONFIG_CIL_PINCODE));		
		TMedico medico = epRequest.getMedico();
		if (medico== null) {
			throw new MwReqAdapterException("Property medico cannot be null!");
		}
		this.setMandatory(annullaPrescrittoRichiesta, "cfMedico" , medico.getCodiceFiscale());
		TRicetta ricetta = epRequest.getRicetta();
		if (ricetta == null) {
			throw new MwReqAdapterException("Property ricetta cannot be null!");
		}
		this.setMandatory(annullaPrescrittoRichiesta, "nre", ricetta.getNRE());
		return annullaPrescrittoRichiestaDocument.xmlText();
	}

}
