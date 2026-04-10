package it.eng.cct.dem.sar.mw.piemonte.adapters;

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
import it.eng.cct.dem.sar.mw.piemonte.constants.IrideConstants;
import it.eng.cct.dem.sar.mw.piemonte.constants.MwConstants;
import it.eng.cct.dem.sar.mw.piemonte.constants.SarConstants;
import it.eng.cct.dem.sar.mw.piemonte.constants.ServizioRichiestaLottoNreConstants;
import it.eng.cct.dem.sar.mw.piemonte.db.model.IrideSiKeyrad;
import it.eng.cct.dem.sar.mw.piemonte.db.model.IrideSiKeyradDati;
import it.eng.cct.dem.sar.mw.piemonte.db.service.SiKeyradDatiService;
import it.eng.cct.dem.sar.mw.piemonte.db.service.SiKeyradService;
import it.eng.cct.dem.sar.mw.piemonte.exception.DatabaseException;
import it.eng.cct.dem.sar.mw.spring.ApplicationContextProvider;
import it.finanze.sanita.mirsac.nre.xsd.lottorichiesta.LottoRichiestaNREDocument;

public class ServizioRichiestaLottoNreRequestAdapter extends SistemaTiesseAdapter implements ServiceInputAdapter{

	private LottoRichiestaNREDocument request;
	private SiKeyradService siKeyradService;
	private SiKeyradDatiService siKeyradDatiService;
	private boolean sarCredFromDb;
//	Trattandosi di un servizio emulato CIL...
	private boolean cilEmulation = Boolean.TRUE;

	public ServizioRichiestaLottoNreRequestAdapter(LottoRichiestaNREDocument lottoRichiestaNREDocument) {
		super();
		this.request = lottoRichiestaNREDocument;
		this.sarCredFromDb = ModuleConfig.getPropertyAsBoolean(MwConstants.CONFIG_SAR_CREDENTIAL_FROM_IRIDE_DB, Boolean.FALSE);
		if(this.sarCredFromDb){
			this.siKeyradService = (SiKeyradService) ApplicationContextProvider.getApplicationContext().getBean("siKeyradService", SiKeyradService.class);
			this.siKeyradDatiService = (SiKeyradDatiService) ApplicationContextProvider.getApplicationContext().getBean("siKeyradDatiService", SiKeyradDatiService.class);
		}
	}
	
	public IServiceInput getServiceInput() throws MwReqAdapterException {
		IServiceInput serviceInput = new ServiceInput();
		serviceInput.setAction(new Action(ModuleConfig.getProperty(ServizioRichiestaLottoNreConstants.CONFIG_SAR_SERVIZIORICHIESTALOTTO_SERVICE_NAME), ModuleConfig.getProperty(ServizioRichiestaLottoNreConstants.CONFIG_SAR_SERVIZIORICHIESTALOTTO_OPERATION_NAME), ModuleConfig.getProperty(ServizioRichiestaLottoNreConstants.CONFIG_SAR_SERVIZIORICHIESTALOTTO_SERVICE_ENDPOINT)));
		serviceInput.setSecurityConfigId(ModuleConfig.getProperty(ServizioRichiestaLottoNreConstants.CONFIG_SAR_SERVIZIORICHIESTALOTTO_SECCONFIGID));
		try {
			serviceInput.setRequestMessage(this.toInputMessage(request));
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
		
		serviceInput.setCustomProps(ModuleConfig.getPropertiesByPrefix(ServizioRichiestaLottoNreConstants.CONFIG_SAR_SERVIZIORICHIESTALOTTO_ROOT + ServiceInputAdapter.CUSTOM_PROPS_KEY, true));
		return serviceInput;
	}
	
	private String toInputMessage(LottoRichiestaNREDocument request2) throws MwReqAdapterException, DatabaseException{
		String pinCode = "";
		if(this.sarCredFromDb){
			IrideSiKeyrad siKeyrad = siKeyradService.getByKrNome(ModuleConfig.getProperty(IrideConstants.SAR_INTEG_PIN));
			IrideSiKeyradDati siKeyradDati = siKeyradDatiService.getByKdKey(siKeyrad.getKrKey());
			pinCode = siKeyradDati.getKdValue();
		}else
			pinCode = ModuleConfig.getProperty(SarConstants.CONFIG_SAR_PINCODE);
		
		try {
			pinCode = RsaCipherUtils.encryptToBase64(ModuleConfig.getServiceClientProps(), pinCode.getBytes());
		} catch (CryptoException e) {
			throw new MwReqAdapterException(e);
		}
		this.setMandatory(request2, "pinCode", pinCode);		
		return request2.xmlText();
	}

}
