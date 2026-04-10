package it.eng.cct.dem.sar.mw.piemonte.adapters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.cct.dem.sar.generic.wsclient.commons.utils.ConfigKeys;
import it.eng.cct.dem.sar.generic.wsclient.commons.utils.ModuleConfig;
import it.eng.cct.dem.sar.generic.wsclient.params.IServiceInput;
import it.eng.cct.dem.sar.generic.wsclient.params.impl.Action;
import it.eng.cct.dem.sar.generic.wsclient.params.impl.Login;
import it.eng.cct.dem.sar.generic.wsclient.params.impl.ServiceInput;
import it.eng.cct.dem.sar.mw.adapters.ServiceInputAdapter;
import it.eng.cct.dem.sar.mw.exceptions.MwReqAdapterException;
import it.eng.cct.dem.sar.mw.piemonte.constants.DemInterrogaNreUtilizzatiConstants;
import it.eng.cct.dem.sar.mw.piemonte.constants.IrideConstants;
import it.eng.cct.dem.sar.mw.piemonte.constants.MwConstants;
import it.eng.cct.dem.sar.mw.piemonte.db.model.IrideSiKeyrad;
import it.eng.cct.dem.sar.mw.piemonte.db.model.IrideSiKeyradDati;
import it.eng.cct.dem.sar.mw.piemonte.db.service.SiKeyradDatiService;
import it.eng.cct.dem.sar.mw.piemonte.db.service.SiKeyradService;
import it.eng.cct.dem.sar.mw.piemonte.db.service.trace.ServiceMonitoringHelper;
import it.eng.cct.dem.sar.mw.piemonte.exception.DatabaseException;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TEPrescriptions;
import it.eng.cct.dem.sar.mw.spring.ApplicationContextProvider;
import it.finanze.sanita.dem.xsd.interroganreutilrichiesta.InterrogaNreUtilRichiestaDocument;
import it.finanze.sanita.dem.xsd.interroganreutilrichiesta.InterrogaNreUtilRichiestaDocument.InterrogaNreUtilRichiesta;

/*
 * 
 * 
 */
public class InterrogaNreUtilRichiestaAdapter extends SistemaTiesseAdapter implements ServiceInputAdapter {

	private TEPrescriptions epRequest;
	private static final Logger logger = LoggerFactory.getLogger(InterrogaNreUtilRichiestaAdapter.class);
	private ServiceMonitoringHelper serviceMonitoringHelper;
	private SiKeyradService siKeyradService;
	private SiKeyradDatiService siKeyradDatiService;
	private boolean sarCredFromDb;
	private boolean cilEmulation;
	
	public InterrogaNreUtilRichiestaAdapter(TEPrescriptions epRequest, ServiceMonitoringHelper serviceMonitoringHelper) {
		super();
		this.epRequest = epRequest;
		this.serviceMonitoringHelper = serviceMonitoringHelper;
		this.sarCredFromDb = ModuleConfig.getPropertyAsBoolean(MwConstants.CONFIG_SAR_CREDENTIAL_FROM_IRIDE_DB, Boolean.FALSE);
		this.cilEmulation = ModuleConfig.getPropertyAsBoolean(MwConstants.CONFIG_CIL_EMULATION, Boolean.FALSE);
		if(this.sarCredFromDb){
			this.siKeyradService = (SiKeyradService) ApplicationContextProvider.getApplicationContext().getBean("siKeyradService", SiKeyradService.class);
			this.siKeyradDatiService = (SiKeyradDatiService) ApplicationContextProvider.getApplicationContext().getBean("siKeyradDatiService", SiKeyradDatiService.class);
		}
	}

	public IServiceInput getServiceInput() throws MwReqAdapterException {
		IServiceInput serviceInput = new ServiceInput();
		serviceInput.setAction(new Action(ModuleConfig.getProperty(DemInterrogaNreUtilizzatiConstants.CONFIG_CIL_DEMINTERROGANREUTILIZZATI_SERVICE_NAME), ModuleConfig.getProperty(DemInterrogaNreUtilizzatiConstants.CONFIG_CIL_DEMINTERROGANREUTILIZZATI_OPERATION_NAME), ModuleConfig.getProperty(DemInterrogaNreUtilizzatiConstants.CONFIG_CIL_DEMINTERROGANREUTILIZZATI_SERVICE_ENDPOINT)));
		serviceInput.setSecurityConfigId(ModuleConfig.getProperty(DemInterrogaNreUtilizzatiConstants.CONFIG_CIL_DEMINTERROGANREUTILIZZATI_SECCONFIGID));
		serviceInput.setRequestMessage(this.toInputMessage(epRequest));

		String httpBasicUsername = "";
		String httpBasicPassword = "";
		if(this.sarCredFromDb){
			IrideSiKeyrad siKeyrad;
			IrideSiKeyradDati siKeyradDati;
			try {
				siKeyrad = siKeyradService.getByKrNome(ModuleConfig.getProperty(IrideConstants.SAR_INTEG_USERNAME));
				siKeyradDati = siKeyradDatiService.getByKdKey(siKeyrad.getKrKey());
			} catch (DatabaseException e) {
				throw new MwReqAdapterException(e);
			}
			httpBasicUsername = siKeyradDati.getKdValue();
			try {
				siKeyrad = siKeyradService.getByKrNome(ModuleConfig.getProperty(IrideConstants.SAR_INTEG_PASSWORD));
				siKeyradDati = siKeyradDatiService.getByKdKey(siKeyrad.getKrKey());
			} catch (DatabaseException e) {
				throw new MwReqAdapterException(e);
			}
			httpBasicPassword = siKeyradDati.getKdValue();
		}else{
			httpBasicUsername = ModuleConfig.getProperty(ConfigKeys.CONFIG_AXIS2_CLIENT_HTTP_AUTH_BASIC_USERNAME);
			httpBasicPassword = ModuleConfig.getProperty(ConfigKeys.CONFIG_AXIS2_CLIENT_HTTP_AUTH_BASIC_PASSWORD);
		}
		
		serviceInput.setLogin(new Login(httpBasicUsername, httpBasicPassword));
		
		serviceInput.setCustomProps(ModuleConfig.getPropertiesByPrefix(DemInterrogaNreUtilizzatiConstants.CONFIG_CIL_DEMINTERROGANREUTILIZZATI_ROOT + ServiceInputAdapter.CUSTOM_PROPS_KEY, true));
		return serviceInput;
	}

	private String toInputMessage(TEPrescriptions request) throws MwReqAdapterException {
		InterrogaNreUtilRichiestaDocument interrogaNreUtilDocument = InterrogaNreUtilRichiestaDocument.Factory.newInstance();
		InterrogaNreUtilRichiesta interrogaNreUtil = interrogaNreUtilDocument.addNewInterrogaNreUtilRichiesta();
		
		this.setMandatory(interrogaNreUtil, "cfMedico", request.getCfMedico());
		this.setDateIfNotEmpty(interrogaNreUtil, "dataCompilazioneRicettaAl", request.getDataCompilazioneRicettaAl());
		this.setDateIfNotEmpty(interrogaNreUtil, "dataCompilazioneRicettaDal", request.getDataCompilazioneRicettaDal());
		this.setMandatory(interrogaNreUtil, "tipoPrescr", request.getTipoPrescr());
		this.setMandatory(interrogaNreUtil, "codRegione", ModuleConfig.getProperty(DemInterrogaNreUtilizzatiConstants.CONFIG_CIL_DEMINTERROGANREUTILIZZATI_CODREGIONE));
		this.serviceMonitoringHelper.setMwToServiceBean(interrogaNreUtilDocument);
		return interrogaNreUtilDocument.xmlText();
	}

}
