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
import it.eng.cct.dem.sar.mw.piemonte.constants.DemInterrogaNreUtilizzatiConstants;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TEPrescriptions;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TMedico;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TRicetta;
import it.finanze.sanita.dem.xsd.interroganreutilrichiesta.InterrogaNreUtilRichiestaDocument;
import it.finanze.sanita.dem.xsd.interroganreutilrichiesta.InterrogaNreUtilRichiestaDocument.InterrogaNreUtilRichiesta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * 
 * 
 */
public class InterrogaNreUtilRichiestaAdapter extends SistemaTiesseAdapter implements ServiceInputAdapter {

	private TEPrescriptions epRequest;
	private static final Logger logger = LoggerFactory.getLogger(InterrogaNreUtilRichiestaAdapter.class);
	
	
	public InterrogaNreUtilRichiestaAdapter(TEPrescriptions epRequest) {
		super();
		this.epRequest = epRequest;
	}

	public IServiceInput getServiceInput() throws MwReqAdapterException {
		IServiceInput serviceInput = new ServiceInput();
		serviceInput.setAction(new Action(ModuleConfig.getProperty(DemInterrogaNreUtilizzatiConstants.CONFIG_CIL_DEMINTERROGANREUTILIZZATI_SERVICE_NAME), ModuleConfig.getProperty(DemInterrogaNreUtilizzatiConstants.CONFIG_CIL_DEMINTERROGANREUTILIZZATI_OPERATION_NAME), ModuleConfig.getProperty(DemInterrogaNreUtilizzatiConstants.CONFIG_CIL_DEMINTERROGANREUTILIZZATI_SERVICE_ENDPOINT)));
		serviceInput.setSecurityConfigId(ModuleConfig.getProperty(DemInterrogaNreUtilizzatiConstants.CONFIG_CIL_DEMINTERROGANREUTILIZZATI_SECCONFIGID));
		serviceInput.setRequestMessage(this.toInputMessage(epRequest));
		if (!this.isOAuth2Request(epRequest)) {
			serviceInput.setLogin(new Login(ModuleConfig.getProperty(ConfigKeys.CONFIG_AXIS2_CLIENT_HTTP_AUTH_BASIC_USERNAME), ModuleConfig.getProperty(ConfigKeys.CONFIG_AXIS2_CLIENT_HTTP_AUTH_BASIC_PASSWORD)));
		}
		serviceInput.setCustomProps(this.enrichCustomPropsWithTransportAuth(ModuleConfig.getPropertiesByPrefix(DemInterrogaNreUtilizzatiConstants.CONFIG_CIL_DEMINTERROGANREUTILIZZATI_ROOT + ServiceInputAdapter.CUSTOM_PROPS_KEY, true), epRequest));
		return serviceInput;
	}

	private String toInputMessage(TEPrescriptions request) throws MwReqAdapterException {
		InterrogaNreUtilRichiestaDocument interrogaNreUtilDocument = InterrogaNreUtilRichiestaDocument.Factory.newInstance();
		InterrogaNreUtilRichiesta interrogaNreUtil = interrogaNreUtilDocument.addNewInterrogaNreUtilRichiesta();
		this.propagateSessionToken(interrogaNreUtil, request);

		this.setMandatory(interrogaNreUtil, "cfMedico", request.getCfMedico());
		this.setDateIfNotEmpty(interrogaNreUtil, "dataCompilazioneRicettaAl", request.getDataCompilazioneRicettaAl());
		this.setDateIfNotEmpty(interrogaNreUtil, "dataCompilazioneRicettaDal", request.getDataCompilazioneRicettaDal());
		this.setMandatory(interrogaNreUtil, "tipoPrescr", request.getTipoPrescr());
		this.setMandatory(interrogaNreUtil, "codRegione", ModuleConfig.getProperty(DemInterrogaNreUtilizzatiConstants.CONFIG_CIL_DEMINTERROGANREUTILIZZATI_CODREGIONE));

		return interrogaNreUtilDocument.xmlText();
	}

}
