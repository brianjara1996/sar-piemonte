package it.eng.cct.dem.sar.mw.piemonte.adapters;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.cct.dem.sar.generic.wsclient.commons.utils.ConfigKeys;
import it.eng.cct.dem.sar.generic.wsclient.commons.utils.ModuleConfig;
import it.eng.cct.dem.sar.generic.wsclient.commons.utils.RsaCipherUtils;
import it.eng.cct.dem.sar.generic.wsclient.commons.utils.exceptions.CryptoException;
import it.eng.cct.dem.sar.generic.wsclient.params.IServiceInput;
import it.eng.cct.dem.sar.generic.wsclient.params.impl.Action;
import it.eng.cct.dem.sar.generic.wsclient.params.impl.Login;
import it.eng.cct.dem.sar.generic.wsclient.params.impl.ServiceInput;
import it.eng.cct.dem.sar.mw.adapters.ServiceInputAdapter;
import it.eng.cct.dem.sar.mw.exceptions.MwReqAdapterException;
import it.eng.cct.dem.sar.mw.piemonte.constants.ElencoSinteticoStatoInviiConstants;
import it.eng.cct.dem.sar.mw.piemonte.constants.IrideConstants;
import it.eng.cct.dem.sar.mw.piemonte.constants.MwConstants;
import it.eng.cct.dem.sar.mw.piemonte.constants.SarConstants;
import it.eng.cct.dem.sar.mw.piemonte.db.model.IrideSiKeyrad;
import it.eng.cct.dem.sar.mw.piemonte.db.model.IrideSiKeyradDati;
import it.eng.cct.dem.sar.mw.piemonte.db.service.SiKeyradDatiService;
import it.eng.cct.dem.sar.mw.piemonte.db.service.SiKeyradService;
import it.eng.cct.dem.sar.mw.piemonte.db.service.trace.ServiceMonitoringHelper;
import it.eng.cct.dem.sar.mw.piemonte.exception.DatabaseException;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TEPrescriptions;
import it.eng.cct.dem.sar.mw.spring.ApplicationContextProvider;
import it.finanze.sanita.dem.dpcm.wsdl.elencoSinteticoStatoInvii.service.VisualizzaElencoStatoInviiDocument;
import it.finanze.sanita.dem.dpcm.wsdl.elencoSinteticoStatoInvii.service.VisualizzaElencoStatoInviiDocument.VisualizzaElencoStatoInvii;
import it.finanze.sanita.mir.auxservices.dto.elencosinteticostatoinvii.ElencoSinteticoStatoInviiDTO;

/*
 * 
 * 
 */
public class ElencoSinteticoStatoInviiRequestAdapter extends SistemaTiesseAdapter implements ServiceInputAdapter {

	private TEPrescriptions epRequest;
	private static final Logger logger = LoggerFactory.getLogger(ElencoSinteticoStatoInviiRequestAdapter.class);
	private ServiceMonitoringHelper serviceMonitoringHelper;
	private SiKeyradService siKeyradService;
	private SiKeyradDatiService siKeyradDatiService;
	private boolean sarCredFromDb;
	private boolean cilEmulation;
	
	public ElencoSinteticoStatoInviiRequestAdapter(TEPrescriptions epRequest, ServiceMonitoringHelper serviceMonitoringHelper) {
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
		serviceInput.setAction(new Action(ModuleConfig.getProperty(ElencoSinteticoStatoInviiConstants.CONFIG_CIL_ELENCOSINTETICOSTATOINVII_SERVICE_NAME), ModuleConfig.getProperty(ElencoSinteticoStatoInviiConstants.CONFIG_CIL_ELENCOSINTETICOSTATOINVII_OPERATION_NAME), ModuleConfig.getProperty(ElencoSinteticoStatoInviiConstants.CONFIG_CIL_ELENCOSINTETICOSTATOINVII_SERVICE_ENDPOINT)));
		serviceInput.setSecurityConfigId(ModuleConfig.getProperty(ElencoSinteticoStatoInviiConstants.CONFIG_CIL_ELENCOSINTETICOSTATOINVII_SECCONFIGID));
		try {
			serviceInput.setRequestMessage(this.toInputMessage(epRequest));
		} catch (DatabaseException e1) {
			throw new MwReqAdapterException(e1);
		}
		
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
		
		if (!this.isOAuth2Request(epRequest)) {
			serviceInput.setLogin(new Login(httpBasicUsername, httpBasicPassword));
		}
		
		serviceInput.setCustomProps(this.enrichCustomPropsWithTransportAuth(ModuleConfig.getPropertiesByPrefix(ElencoSinteticoStatoInviiConstants.CONFIG_CIL_ELENCOSINTETICOSTATOINVII_ROOT + ServiceInputAdapter.CUSTOM_PROPS_KEY, true), epRequest));
		return serviceInput;
	}

	private String toInputMessage(TEPrescriptions request) throws MwReqAdapterException, DatabaseException {
		VisualizzaElencoStatoInviiDocument visualizzaElencoStatoInviiDocument = VisualizzaElencoStatoInviiDocument.Factory.newInstance();
		VisualizzaElencoStatoInvii visualizzaElencoStatoInvii = visualizzaElencoStatoInviiDocument.addNewVisualizzaElencoStatoInvii();
		this.propagateSessionToken(visualizzaElencoStatoInvii, request);
		ElencoSinteticoStatoInviiDTO elencoSinteticoStatoInviiDTO = visualizzaElencoStatoInvii.addNewElencoSinteticoStatoInviiDTO();
		
		String pinCode = "";
		if(this.sarCredFromDb){
			IrideSiKeyrad siKeyrad = siKeyradService.getByKrNome(ModuleConfig.getProperty(IrideConstants.SAR_INTEG_PIN));
			IrideSiKeyradDati siKeyradDati = siKeyradDatiService.getByKdKey(siKeyrad.getKrKey());
			pinCode = siKeyradDati.getKdValue();
		}else if(this.cilEmulation)
			pinCode = ModuleConfig.getProperty(SarConstants.CONFIG_SAR_PINCODE);
		else
			pinCode =  ModuleConfig.getProperty(ElencoSinteticoStatoInviiConstants.CONFIG_CIL_PINCODE);
		if(this.cilEmulation){
			try {
				pinCode = RsaCipherUtils.encryptToBase64(ModuleConfig.getServiceClientProps(), pinCode.getBytes());
			} catch (CryptoException e) {
				throw new MwReqAdapterException(e);
			}
		}
		this.setMandatory(elencoSinteticoStatoInviiDTO, "pinCodeIn", this.getPinCodeForRequest(pinCode, request));
		String protocolloSac = request.getProtocolloSAC();
		if (StringUtils.isNotEmpty(protocolloSac)) {
			this.setMandatory(elencoSinteticoStatoInviiDTO, "protocolloSac", protocolloSac);
		} else {
			this.setDateIfNotEmpty(elencoSinteticoStatoInviiDTO, "dataIniRange", request.getDataIniRange(), WS_TIESSE_DPCM_DATE_PATTERN);
			this.setDateIfNotEmpty(elencoSinteticoStatoInviiDTO, "dataFineRange", request.getDataFineRange(), WS_TIESSE_DPCM_DATE_PATTERN);
		}
		this.serviceMonitoringHelper.setMwToServiceBean(visualizzaElencoStatoInviiDocument);
		return visualizzaElencoStatoInviiDocument.xmlText();
	}

}
