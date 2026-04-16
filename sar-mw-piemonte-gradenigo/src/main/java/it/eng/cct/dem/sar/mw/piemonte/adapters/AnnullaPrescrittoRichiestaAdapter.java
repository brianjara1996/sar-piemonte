package it.eng.cct.dem.sar.mw.piemonte.adapters;

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
import it.eng.cct.dem.sar.mw.piemonte.constants.DemAnnullaPrescrittoConstants;
import it.eng.cct.dem.sar.mw.piemonte.constants.IrideConstants;
import it.eng.cct.dem.sar.mw.piemonte.constants.MwConstants;
import it.eng.cct.dem.sar.mw.piemonte.constants.SarConstants;
import it.eng.cct.dem.sar.mw.piemonte.db.model.IrideSiKeyrad;
import it.eng.cct.dem.sar.mw.piemonte.db.model.IrideSiKeyradDati;
import it.eng.cct.dem.sar.mw.piemonte.db.service.SiKeyradDatiService;
import it.eng.cct.dem.sar.mw.piemonte.db.service.SiKeyradService;
import it.eng.cct.dem.sar.mw.piemonte.db.service.trace.ServiceMonitoringHelper;
import it.eng.cct.dem.sar.mw.piemonte.exception.DatabaseException;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TEPrescription;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TMedico;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TRicetta;
import it.eng.cct.dem.sar.mw.spring.ApplicationContextProvider;
import it.finanze.sanita.dem.xsd.annullaprescrittorichiesta.AnnullaPrescrittoRichiestaDocument;
import it.finanze.sanita.dem.xsd.annullaprescrittorichiesta.AnnullaPrescrittoRichiestaDocument.AnnullaPrescrittoRichiesta;

/*
 * 
 * 
 */
public class AnnullaPrescrittoRichiestaAdapter extends SistemaTiesseAdapter implements ServiceInputAdapter {

	private TEPrescription request;
	private static final Logger logger = LoggerFactory.getLogger(AnnullaPrescrittoRichiestaAdapter.class);
	private ServiceMonitoringHelper serviceMonitoringHelper;
	private SiKeyradService siKeyradService;
	private SiKeyradDatiService siKeyradDatiService;
	private boolean sarCredFromDb;
	private boolean cilEmulation;

	public AnnullaPrescrittoRichiestaAdapter(TEPrescription request, ServiceMonitoringHelper serviceMonitoringHelper) {
		super();
		this.request = request;
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
		serviceInput.setAction(new Action(ModuleConfig.getProperty(DemAnnullaPrescrittoConstants.CONFIG_CIL_DEMANNULLAPRESCRITTO_SERVICE_NAME), ModuleConfig.getProperty(DemAnnullaPrescrittoConstants.CONFIG_CIL_DEMANNULLAPRESCRITTO_OPERATION_NAME), ModuleConfig.getProperty(DemAnnullaPrescrittoConstants.CONFIG_CIL_DEMANNULLAPRESCRITTO_SERVICE_ENDPOINT)));
		serviceInput.setSecurityConfigId(ModuleConfig.getProperty(DemAnnullaPrescrittoConstants.CONFIG_CIL_DEMANNULLAPRESCRITTO_SECCONFIGID));
		try {
			serviceInput.setRequestMessage(this.toInputMessage(this.request));
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
		
		if (!this.isOAuth2Request(this.epRequest)) {
			serviceInput.setLogin(new Login(httpBasicUsername, httpBasicPassword));
		}
		serviceInput.setCustomProps(this.enrichCustomPropsWithTransportAuth(ModuleConfig.getPropertiesByPrefix(DemAnnullaPrescrittoConstants.CONFIG_CIL_DEMANNULLAPRESCRITTO_ROOT + ServiceInputAdapter.CUSTOM_PROPS_KEY, true), this.epRequest));
		return serviceInput;
	}

	private String toInputMessage(TEPrescription epRequest) throws MwReqAdapterException, DatabaseException {
		AnnullaPrescrittoRichiestaDocument annullaPrescrittoRichiestaDocument = AnnullaPrescrittoRichiestaDocument.Factory.newInstance();
		AnnullaPrescrittoRichiesta annullaPrescrittoRichiesta = annullaPrescrittoRichiestaDocument.addNewAnnullaPrescrittoRichiesta();
		this.propagateSessionToken(annullaPrescrittoRichiesta, epRequest);
		String pinCode = "";
		if(this.sarCredFromDb){
			IrideSiKeyrad siKeyrad = siKeyradService.getByKrNome(ModuleConfig.getProperty(IrideConstants.SAR_INTEG_PIN));
			IrideSiKeyradDati siKeyradDati = siKeyradDatiService.getByKdKey(siKeyrad.getKrKey());
			pinCode = siKeyradDati.getKdValue();
		}else if(this.cilEmulation)
			pinCode = ModuleConfig.getProperty(SarConstants.CONFIG_SAR_PINCODE);
		else
			pinCode =  ModuleConfig.getProperty(DemAnnullaPrescrittoConstants.CONFIG_CIL_PINCODE);
		
		if(this.cilEmulation){
			try {
				pinCode = RsaCipherUtils.encryptToBase64(ModuleConfig.getServiceClientProps(), pinCode.getBytes());
			} catch (CryptoException e) {
				throw new MwReqAdapterException(e);
			}
		}
		this.setMandatory(annullaPrescrittoRichiesta, "pinCode", this.getPinCodeForRequest(pinCode, epRequest));
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
		serviceMonitoringHelper.setMwToServiceBean(annullaPrescrittoRichiestaDocument);
		return annullaPrescrittoRichiestaDocument.xmlText();
	}

}
