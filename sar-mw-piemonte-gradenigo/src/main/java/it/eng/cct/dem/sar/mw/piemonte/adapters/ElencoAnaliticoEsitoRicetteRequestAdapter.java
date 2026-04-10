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
import it.eng.cct.dem.sar.mw.piemonte.constants.ElencoAnaliticoEsitoRicetteConstants;
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
import it.finanze.sanita.dem.dpcm.wsdl.elencoAnaliticoEsitoRicette.service.VisualizzaElencoStatoRicetteDocument;
import it.finanze.sanita.dem.dpcm.wsdl.elencoAnaliticoEsitoRicette.service.VisualizzaElencoStatoRicetteDocument.VisualizzaElencoStatoRicette;
import it.finanze.sanita.mir.auxservices.dto.elencoanaliticoesitoricette.ElencoAnaliticoEsitoRicetteDTO;

/*
 * 
 * 
 */
public class ElencoAnaliticoEsitoRicetteRequestAdapter extends SistemaTiesseAdapter implements ServiceInputAdapter {

	private TEPrescriptions epRequest;
	private static final Logger logger = LoggerFactory.getLogger(ElencoAnaliticoEsitoRicetteRequestAdapter.class);
	private ServiceMonitoringHelper serviceMonitoringHelper;
	private SiKeyradService siKeyradService;
	private SiKeyradDatiService siKeyradDatiService;
	private boolean sarCredFromDb;
	private boolean cilEmulation;

	public ElencoAnaliticoEsitoRicetteRequestAdapter(TEPrescriptions epRequest, ServiceMonitoringHelper serviceMonitoringHelper) {
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
		serviceInput.setAction(new Action(ModuleConfig.getProperty(ElencoAnaliticoEsitoRicetteConstants.CONFIG_CIL_ELENCOANALITICOESITORICETTE_SERVICE_NAME), ModuleConfig.getProperty(ElencoAnaliticoEsitoRicetteConstants.CONFIG_CIL_ELENCOANALITICOESITORICETTE_OPERATION_NAME), ModuleConfig.getProperty(ElencoAnaliticoEsitoRicetteConstants.CONFIG_CIL_ELENCOANALITICOESITORICETTE_SERVICE_ENDPOINT)));
		serviceInput.setSecurityConfigId(ModuleConfig.getProperty(ElencoAnaliticoEsitoRicetteConstants.CONFIG_CIL_ELENCOANALITICOESITORICETTE_SECCONFIGID));
		try {
			serviceInput.setRequestMessage(this.toInputMessage(epRequest));
		} catch (DatabaseException e) {
			throw new MwReqAdapterException(e);
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
		
		serviceInput.setLogin(new Login(httpBasicUsername, httpBasicPassword));
		
		serviceInput.setCustomProps(ModuleConfig.getPropertiesByPrefix(ElencoAnaliticoEsitoRicetteConstants.CONFIG_CIL_ELENCOANALITICOESITORICETTE_ROOT + ServiceInputAdapter.CUSTOM_PROPS_KEY, true));
		return serviceInput;
	}

	private String toInputMessage(TEPrescriptions request) throws MwReqAdapterException, DatabaseException {
		VisualizzaElencoStatoRicetteDocument visualizzaElencoStatoRicetteDocument = VisualizzaElencoStatoRicetteDocument.Factory.newInstance();
		VisualizzaElencoStatoRicette visualizzaElencoStatoRicette = visualizzaElencoStatoRicetteDocument.addNewVisualizzaElencoStatoRicette();
		ElencoAnaliticoEsitoRicetteDTO elencoAnaliticoEsitoRicetteDTO = visualizzaElencoStatoRicette.addNewElencoAnaliticoEsitoRicetteDTO();
		String pinCode = "";
		if(this.sarCredFromDb){
			IrideSiKeyrad siKeyrad = this.siKeyradService.getByKrNome(ModuleConfig.getProperty(IrideConstants.SAR_INTEG_PIN));
			IrideSiKeyradDati siKeyradDati = this.siKeyradDatiService.getByKdKey(siKeyrad.getKrKey());
			pinCode = siKeyradDati.getKdValue();
		}else if(this.cilEmulation)
			pinCode = ModuleConfig.getProperty(SarConstants.CONFIG_SAR_PINCODE);
		else
			pinCode =  ModuleConfig.getProperty(ElencoAnaliticoEsitoRicetteConstants.CONFIG_CIL_PINCODE);
		if(this.cilEmulation){
			try {
				pinCode = RsaCipherUtils.encryptToBase64(ModuleConfig.getServiceClientProps(), pinCode.getBytes());
			} catch (CryptoException e) {
				throw new MwReqAdapterException(e);
			}
		}
		this.setMandatory(elencoAnaliticoEsitoRicetteDTO, "pinCodeIn", pinCode);
		String protocolloSac = request.getProtocolloSAC();
		if (StringUtils.isNotEmpty(protocolloSac)) {
			this.setMandatory(elencoAnaliticoEsitoRicetteDTO, "protocolloSac", protocolloSac);
		} else {
			this.setDateIfNotEmpty(elencoAnaliticoEsitoRicetteDTO, "dataIniRange", request.getDataIniRange(), WS_TIESSE_DPCM_DATE_PATTERN);
			this.setDateIfNotEmpty(elencoAnaliticoEsitoRicetteDTO, "dataFineRange", request.getDataFineRange(), WS_TIESSE_DPCM_DATE_PATTERN);
		}
		this.serviceMonitoringHelper.setMwToServiceBean(visualizzaElencoStatoRicetteDocument);
		return visualizzaElencoStatoRicetteDocument.xmlText();
	}

}
