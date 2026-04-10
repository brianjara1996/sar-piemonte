package it.eng.cct.dem.sar.mw.piemonte.adapters;

import it.eng.cct.dem.sar.generic.wsclient.commons.utils.ConfigKeys;
import it.eng.cct.dem.sar.generic.wsclient.commons.utils.ModuleConfig;
import it.eng.cct.dem.sar.generic.wsclient.params.IServiceInput;
import it.eng.cct.dem.sar.generic.wsclient.params.impl.Action;
import it.eng.cct.dem.sar.generic.wsclient.params.impl.Login;
import it.eng.cct.dem.sar.generic.wsclient.params.impl.ServiceInput;
import it.eng.cct.dem.sar.mw.adapters.ServiceInputAdapter;
import it.eng.cct.dem.sar.mw.exceptions.MwReqAdapterException;
import it.eng.cct.dem.sar.mw.piemonte.constants.DemInterrogaNreUtilizzatiConstants;
import it.eng.cct.dem.sar.mw.piemonte.constants.RichiestaNreConstants;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TEPrescription;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TMedico;
import it.finanze.sanita.dem.xsd.richiestanrerichiesta.RichiestaNreRichiestaDocument;
import it.finanze.sanita.dem.xsd.richiestanrerichiesta.RichiestaNreRichiestaDocument.RichiestaNreRichiesta;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * 
 * 
 */
public class RichiestaNreRichiestaAdapter implements ServiceInputAdapter {

	private TEPrescription request;
	private static final Logger logger = LoggerFactory.getLogger(RichiestaNreRichiestaAdapter.class);

	public RichiestaNreRichiestaAdapter(TEPrescription request) {
		super();
		this.request = request;
	}

	public IServiceInput getServiceInput() throws MwReqAdapterException {
		IServiceInput serviceInput = new ServiceInput();
		serviceInput.setAction(new Action(ModuleConfig.getProperty(RichiestaNreConstants.CONFIG_CIL_RICHIESTANRE_SERVICE_NAME), ModuleConfig.getProperty(RichiestaNreConstants.CONFIG_CIL_RICHIESTANRE_OPERATION_NAME), ModuleConfig.getProperty(RichiestaNreConstants.CONFIG_CIL_RICHIESTANRE_SERVICE_ENDPOINT)));
		serviceInput.setSecurityConfigId(ModuleConfig.getProperty(RichiestaNreConstants.CONFIG_CIL_RICHIESTANRE_SECCONFIGID));
		serviceInput.setRequestMessage(this.toInputMessage(request));
		serviceInput.setLogin(new Login(ModuleConfig.getProperty(ConfigKeys.CONFIG_AXIS2_CLIENT_HTTP_AUTH_BASIC_USERNAME), ModuleConfig.getProperty(ConfigKeys.CONFIG_AXIS2_CLIENT_HTTP_AUTH_BASIC_PASSWORD)));
		serviceInput.setCustomProps(ModuleConfig.getPropertiesByPrefix(RichiestaNreConstants.CONFIG_CIL_RICHIESTANRE_ROOT + ServiceInputAdapter.CUSTOM_PROPS_KEY, true));
		return serviceInput;
	}

	private String toInputMessage(TEPrescription ePrescription) throws MwReqAdapterException {
		if (ePrescription == null) {
			throw new MwReqAdapterException("ePrescription non può essere null!");
		}
		TMedico medico = ePrescription.getMedico();
		String cfMedico = medico.getCodiceFiscale();
		if (StringUtils.isEmpty(cfMedico)) {
			throw new MwReqAdapterException("cfMedico non può essere null!");
		}
		RichiestaNreRichiestaDocument richiestaNreRichiestaDocument = RichiestaNreRichiestaDocument.Factory.newInstance();
		RichiestaNreRichiesta richiestaNreRichiesta = richiestaNreRichiestaDocument.addNewRichiestaNreRichiesta();
		richiestaNreRichiesta.setCfMedico(cfMedico);
		richiestaNreRichiesta.setPinCode(ModuleConfig.getProperty(RichiestaNreConstants.CONFIG_CIL_PINCODE));
		return richiestaNreRichiestaDocument.xmlText();
	}

}
