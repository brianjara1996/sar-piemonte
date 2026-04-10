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
import it.eng.cct.dem.sar.mw.piemonte.constants.DemVisualizzaPrescrittoConstants;
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
import it.finanze.sanita.dem.xsd.visualizzaprescrittorichiesta.VisualizzaPrescrittoRichiestaDocument;
import it.finanze.sanita.dem.xsd.visualizzaprescrittorichiesta.VisualizzaPrescrittoRichiestaDocument.VisualizzaPrescrittoRichiesta;

/*
 * 
 * 
 */
public class VisualizzaPrescrittoRichiestaAdapter extends SistemaTiesseAdapter implements ServiceInputAdapter {

	private TEPrescription epRequest;
	private static final Logger logger = LoggerFactory.getLogger(VisualizzaPrescrittoRichiestaAdapter.class);
	private ServiceMonitoringHelper serviceMonitoringHelper;
	private SiKeyradService siKeyradService;
	private SiKeyradDatiService siKeyradDatiService;
	private boolean sarCredFromDb;
	private boolean cilEmulation;
	
	public VisualizzaPrescrittoRichiestaAdapter(TEPrescription epRequest, ServiceMonitoringHelper serviceMonitoringHelper) {
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
		serviceInput.setAction(new Action(ModuleConfig.getProperty(DemVisualizzaPrescrittoConstants.CONFIG_CIL_DEMVISUALIZZAPRESCRITTO_SERVICE_NAME), ModuleConfig.getProperty(DemVisualizzaPrescrittoConstants.CONFIG_CIL_DEMVISUALIZZAPRESCRITTO_OPERATION_NAME), ModuleConfig.getProperty(DemVisualizzaPrescrittoConstants.CONFIG_CIL_DEMVISUALIZZAPRESCRITTO_SERVICE_ENDPOINT)));
		serviceInput.setSecurityConfigId(ModuleConfig.getProperty(DemVisualizzaPrescrittoConstants.CONFIG_CIL_DEMVISUALIZZAPRESCRITTO_SECCONFIGID));
		try {
			serviceInput.setRequestMessage(this.toInputMessage(this.epRequest));
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
		
		serviceInput.setLogin(new Login(httpBasicUsername, httpBasicPassword));
		serviceInput.setCustomProps(ModuleConfig.getPropertiesByPrefix(DemVisualizzaPrescrittoConstants.CONFIG_CIL_DEMVISUALIZZAPRESCRITTO_ROOT + ServiceInputAdapter.CUSTOM_PROPS_KEY, true));
		return serviceInput;
	}

	private String toInputMessage(TEPrescription request) throws MwReqAdapterException, DatabaseException {
		VisualizzaPrescrittoRichiestaDocument visualizzaPrescrittoRichiestaDocument = VisualizzaPrescrittoRichiestaDocument.Factory.newInstance();
		VisualizzaPrescrittoRichiesta visualizzaPrescrittoRichiesta = visualizzaPrescrittoRichiestaDocument.addNewVisualizzaPrescrittoRichiesta();
		
		
		String pinCode = "";
		if(this.sarCredFromDb){
			IrideSiKeyrad siKeyrad = siKeyradService.getByKrNome(ModuleConfig.getProperty(IrideConstants.SAR_INTEG_PIN));
			IrideSiKeyradDati siKeyradDati = siKeyradDatiService.getByKdKey(siKeyrad.getKrKey());
			pinCode = siKeyradDati.getKdValue();
		}else if(this.cilEmulation)
			pinCode = ModuleConfig.getProperty(SarConstants.CONFIG_SAR_PINCODE);
		else
			pinCode =  ModuleConfig.getProperty(DemVisualizzaPrescrittoConstants.CONFIG_CIL_PINCODE);
		
		if(this.cilEmulation){
			try {
				pinCode = RsaCipherUtils.encryptToBase64(ModuleConfig.getServiceClientProps(), pinCode.getBytes());
			} catch (CryptoException e) {
				throw new MwReqAdapterException(e);
			}
		}
		this.setMandatory(visualizzaPrescrittoRichiesta, "pinCode", pinCode);
		
		
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
		this.serviceMonitoringHelper.setMwToServiceBean(visualizzaPrescrittoRichiestaDocument);
		return visualizzaPrescrittoRichiestaDocument.xmlText();
	}

}
