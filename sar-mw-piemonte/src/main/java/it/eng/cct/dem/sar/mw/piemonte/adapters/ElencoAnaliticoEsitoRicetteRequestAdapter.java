package it.eng.cct.dem.sar.mw.piemonte.adapters;

import it.eng.cct.dem.sar.generic.wsclient.commons.utils.ConfigKeys;
import it.eng.cct.dem.sar.generic.wsclient.commons.utils.ModuleConfig;
import it.eng.cct.dem.sar.generic.wsclient.params.IServiceInput;
import it.eng.cct.dem.sar.generic.wsclient.params.impl.Action;
import it.eng.cct.dem.sar.generic.wsclient.params.impl.Login;
import it.eng.cct.dem.sar.generic.wsclient.params.impl.ServiceInput;
import it.eng.cct.dem.sar.mw.adapters.ServiceInputAdapter;
import it.eng.cct.dem.sar.mw.exceptions.MwReqAdapterException;
import it.eng.cct.dem.sar.mw.piemonte.constants.ElencoAnaliticoEsitoRicetteConstants;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TEPrescriptions;
import it.finanze.sanita.dem.dpcm.wsdl.elencoAnaliticoEsitoRicette.service.VisualizzaElencoStatoRicetteDocument;
import it.finanze.sanita.dem.dpcm.wsdl.elencoAnaliticoEsitoRicette.service.VisualizzaElencoStatoRicetteDocument.VisualizzaElencoStatoRicette;
import it.finanze.sanita.mir.auxservices.dto.elencoanaliticoesitoricette.ElencoAnaliticoEsitoRicetteDTO;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * 
 * 
 */
public class ElencoAnaliticoEsitoRicetteRequestAdapter extends SistemaTiesseAdapter implements ServiceInputAdapter {

	private TEPrescriptions epRequest;
	private static final Logger logger = LoggerFactory.getLogger(ElencoAnaliticoEsitoRicetteRequestAdapter.class);

	public ElencoAnaliticoEsitoRicetteRequestAdapter(TEPrescriptions epRequest) {
		super();
		this.epRequest = epRequest;
	}

	public IServiceInput getServiceInput() throws MwReqAdapterException {
		IServiceInput serviceInput = new ServiceInput();
		serviceInput.setAction(new Action(ModuleConfig.getProperty(ElencoAnaliticoEsitoRicetteConstants.CONFIG_CIL_ELENCOANALITICOESITORICETTE_SERVICE_NAME), ModuleConfig.getProperty(ElencoAnaliticoEsitoRicetteConstants.CONFIG_CIL_ELENCOANALITICOESITORICETTE_OPERATION_NAME), ModuleConfig.getProperty(ElencoAnaliticoEsitoRicetteConstants.CONFIG_CIL_ELENCOANALITICOESITORICETTE_SERVICE_ENDPOINT)));
		serviceInput.setSecurityConfigId(ModuleConfig.getProperty(ElencoAnaliticoEsitoRicetteConstants.CONFIG_CIL_ELENCOANALITICOESITORICETTE_SECCONFIGID));
		serviceInput.setRequestMessage(this.toInputMessage(epRequest));
		if (!this.isOAuth2Request(epRequest)) {
			serviceInput.setLogin(new Login(ModuleConfig.getProperty(ConfigKeys.CONFIG_AXIS2_CLIENT_HTTP_AUTH_BASIC_USERNAME), ModuleConfig.getProperty(ConfigKeys.CONFIG_AXIS2_CLIENT_HTTP_AUTH_BASIC_PASSWORD)));
		}
		serviceInput.setCustomProps(this.enrichCustomPropsWithTransportAuth(ModuleConfig.getPropertiesByPrefix(ElencoAnaliticoEsitoRicetteConstants.CONFIG_CIL_ELENCOANALITICOESITORICETTE_ROOT + ServiceInputAdapter.CUSTOM_PROPS_KEY, true), epRequest));
		return serviceInput;
	}

	private String toInputMessage(TEPrescriptions request) throws MwReqAdapterException {
		VisualizzaElencoStatoRicetteDocument visualizzaElencoStatoRicetteDocument = VisualizzaElencoStatoRicetteDocument.Factory.newInstance();
		VisualizzaElencoStatoRicette visualizzaElencoStatoRicette = visualizzaElencoStatoRicetteDocument.addNewVisualizzaElencoStatoRicette();
		this.propagateSessionToken(visualizzaElencoStatoRicette, request);
		ElencoAnaliticoEsitoRicetteDTO elencoAnaliticoEsitoRicetteDTO = visualizzaElencoStatoRicette.addNewElencoAnaliticoEsitoRicetteDTO();
		this.setMandatory(elencoAnaliticoEsitoRicetteDTO, "pinCodeIn", this.getPinCodeForRequest(ModuleConfig.getProperty(ElencoAnaliticoEsitoRicetteConstants.CONFIG_CIL_PINCODE), request));
		String protocolloSac = request.getProtocolloSAC();
		if (StringUtils.isNotEmpty(protocolloSac)) {
			this.setMandatory(elencoAnaliticoEsitoRicetteDTO, "protocolloSac", protocolloSac);
		} else {
			this.setDateIfNotEmpty(elencoAnaliticoEsitoRicetteDTO, "dataIniRange", request.getDataIniRange(), WS_TIESSE_DPCM_DATE_PATTERN);
			this.setDateIfNotEmpty(elencoAnaliticoEsitoRicetteDTO, "dataFineRange", request.getDataFineRange(), WS_TIESSE_DPCM_DATE_PATTERN);
		}
		return visualizzaElencoStatoRicetteDocument.xmlText();
	}

}
