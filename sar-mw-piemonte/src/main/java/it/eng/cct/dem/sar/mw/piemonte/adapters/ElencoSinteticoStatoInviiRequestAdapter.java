package it.eng.cct.dem.sar.mw.piemonte.adapters;

import it.eng.cct.dem.sar.generic.wsclient.commons.utils.ConfigKeys;
import it.eng.cct.dem.sar.generic.wsclient.commons.utils.ModuleConfig;
import it.eng.cct.dem.sar.generic.wsclient.params.IServiceInput;
import it.eng.cct.dem.sar.generic.wsclient.params.impl.Action;
import it.eng.cct.dem.sar.generic.wsclient.params.impl.Login;
import it.eng.cct.dem.sar.generic.wsclient.params.impl.ServiceInput;
import it.eng.cct.dem.sar.mw.adapters.ServiceInputAdapter;
import it.eng.cct.dem.sar.mw.exceptions.MwReqAdapterException;
import it.eng.cct.dem.sar.mw.piemonte.constants.ElencoSinteticoStatoInviiConstants;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TEPrescriptions;
import it.finanze.sanita.dem.dpcm.wsdl.elencoSinteticoStatoInvii.service.VisualizzaElencoStatoInviiDocument;
import it.finanze.sanita.dem.dpcm.wsdl.elencoSinteticoStatoInvii.service.VisualizzaElencoStatoInviiDocument.VisualizzaElencoStatoInvii;
import it.finanze.sanita.mir.auxservices.dto.elencosinteticostatoinvii.ElencoSinteticoStatoInviiDTO;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * 
 * 
 */
public class ElencoSinteticoStatoInviiRequestAdapter extends SistemaTiesseAdapter implements ServiceInputAdapter {

	private TEPrescriptions epRequest;
	private static final Logger logger = LoggerFactory.getLogger(ElencoSinteticoStatoInviiRequestAdapter.class);
	
	
	public ElencoSinteticoStatoInviiRequestAdapter(TEPrescriptions epRequest) {
		super();
		this.epRequest = epRequest;
	}

	public IServiceInput getServiceInput() throws MwReqAdapterException {
		IServiceInput serviceInput = new ServiceInput();
		serviceInput.setAction(new Action(ModuleConfig.getProperty(ElencoSinteticoStatoInviiConstants.CONFIG_CIL_ELENCOSINTETICOSTATOINVII_SERVICE_NAME), ModuleConfig.getProperty(ElencoSinteticoStatoInviiConstants.CONFIG_CIL_ELENCOSINTETICOSTATOINVII_OPERATION_NAME), ModuleConfig.getProperty(ElencoSinteticoStatoInviiConstants.CONFIG_CIL_ELENCOSINTETICOSTATOINVII_SERVICE_ENDPOINT)));
		serviceInput.setSecurityConfigId(ModuleConfig.getProperty(ElencoSinteticoStatoInviiConstants.CONFIG_CIL_ELENCOSINTETICOSTATOINVII_SECCONFIGID));
		serviceInput.setRequestMessage(this.toInputMessage(epRequest));
		serviceInput.setLogin(new Login(ModuleConfig.getProperty(ConfigKeys.CONFIG_AXIS2_CLIENT_HTTP_AUTH_BASIC_USERNAME), ModuleConfig.getProperty(ConfigKeys.CONFIG_AXIS2_CLIENT_HTTP_AUTH_BASIC_PASSWORD)));
		serviceInput.setCustomProps(ModuleConfig.getPropertiesByPrefix(ElencoSinteticoStatoInviiConstants.CONFIG_CIL_ELENCOSINTETICOSTATOINVII_ROOT + ServiceInputAdapter.CUSTOM_PROPS_KEY, true));
		return serviceInput;
	}

	private String toInputMessage(TEPrescriptions request) throws MwReqAdapterException {
		VisualizzaElencoStatoInviiDocument visualizzaElencoStatoInviiDocument = VisualizzaElencoStatoInviiDocument.Factory.newInstance();
		VisualizzaElencoStatoInvii visualizzaElencoStatoInvii = visualizzaElencoStatoInviiDocument.addNewVisualizzaElencoStatoInvii();
		ElencoSinteticoStatoInviiDTO elencoSinteticoStatoInviiDTO = visualizzaElencoStatoInvii.addNewElencoSinteticoStatoInviiDTO();
		this.setMandatory(elencoSinteticoStatoInviiDTO, "pinCodeIn", ModuleConfig.getProperty(ElencoSinteticoStatoInviiConstants.CONFIG_CIL_PINCODE));
		String protocolloSac = request.getProtocolloSAC();
		if (StringUtils.isNotEmpty(protocolloSac)) {
			this.setMandatory(elencoSinteticoStatoInviiDTO, "protocolloSac", protocolloSac);
		} else {
			this.setDateIfNotEmpty(elencoSinteticoStatoInviiDTO, "dataIniRange", request.getDataIniRange(), WS_TIESSE_DPCM_DATE_PATTERN);
			this.setDateIfNotEmpty(elencoSinteticoStatoInviiDTO, "dataFineRange", request.getDataFineRange(), WS_TIESSE_DPCM_DATE_PATTERN);
		}
		return visualizzaElencoStatoInviiDocument.xmlText();
	}

}
