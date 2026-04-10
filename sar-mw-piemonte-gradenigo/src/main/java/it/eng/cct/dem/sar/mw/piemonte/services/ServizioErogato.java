package it.eng.cct.dem.sar.mw.piemonte.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.Stub;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.httpclient.auth.AuthPolicy;

import it.eng.cct.dem.sar.generic.wsclient.commons.context.ServiceContext;
import it.eng.cct.dem.sar.generic.wsclient.commons.context.impl.SimpleServiceContext;
import it.eng.cct.dem.sar.generic.wsclient.commons.exceptions.ClientInitException;
import it.eng.cct.dem.sar.generic.wsclient.commons.utils.ConfigKeys;
import it.eng.cct.dem.sar.generic.wsclient.commons.utils.ModuleConfig;
import it.eng.cct.dem.sar.generic.wsclient.stubs.StubConfigurator;
import it.eng.cct.dem.sar.generic.wsclient.stubs.wrappers.SarStubWrapper;
import it.eng.cct.dem.sar.mw.piemonte.constants.IrideConstants;
import it.eng.cct.dem.sar.mw.piemonte.db.model.IrideSiKeyrad;
import it.eng.cct.dem.sar.mw.piemonte.db.model.IrideSiKeyradDati;
import it.eng.cct.dem.sar.mw.piemonte.db.service.SiKeyradDatiService;
import it.eng.cct.dem.sar.mw.piemonte.db.service.SiKeyradService;
import it.eng.cct.dem.sar.mw.piemonte.exception.DatabaseException;
import it.finanze.sanita.dem.xsd.annullaerogatoricevuta.AnnullaErogatoRicevutaDocument;
import it.finanze.sanita.dem.xsd.invioerogatoricevuta.InvioErogatoRicevutaDocument;
import it.finanze.sanita.dem.xsd.sospendierogatoricevuta.SospendiErogatoRicevutaDocument;
import it.finanze.sanita.dem.xsd.tipodati.ElencoErroriRicetteType;
import it.finanze.sanita.dem.xsd.visualizzaerogatoricevuta.VisualizzaErogatoRicevutaDocument;

public abstract class ServizioErogato {

	private Map<String, Stub> stubMap;
	private SimpleServiceContext serviceContext;
	private Boolean basicAuthOverride;
	private Boolean basicAuthFromDb;
	private SiKeyradService siKeyradService;
	private SiKeyradDatiService siKeyradDatiService;

	public ServizioErogato(Map<String, Stub> stubMap, SimpleServiceContext serviceContext, String endpoint) throws ClientInitException {
		this.stubMap = stubMap;
		try {
			this.serviceContext = (SimpleServiceContext) serviceContext.clone();
		} catch (CloneNotSupportedException e) {
			throw new ClientInitException("Impossibile creare un ServiceContext." + e.getMessage(), e);
		}
		this.serviceContext.setTargetEndpoint(endpoint);
	}

	protected Stub getStub(String key, String className) throws ClientInitException, AxisFault {

		Stub stub = this.stubMap.get(key);
		if (stub == null) {
			synchronized (this.stubMap) {
				// Check again, after having acquired the lock to make sure
				// the instance was not created meanwhile by another thread
				stub = this.stubMap.get(key);
				if (stub == null) {
					ServiceContext serviceContext;
					try {
						serviceContext = (ServiceContext) this.serviceContext.clone();
					} catch (CloneNotSupportedException e) {
						throw new ClientInitException("Impossibile creare un ServiceContext." + e.getMessage(), e);
					}
					StubConfigurator stubConfigurator = new StubConfigurator(serviceContext);
					// Lazily create instance
					stub = new SarStubWrapper(className, stubConfigurator).getStub();
					// Add it to map
					HttpTransportProperties.Authenticator basicAuth = new HttpTransportProperties.Authenticator();
					String[] userDetails = key.split(":", 2);
					List<String> authprefs = new ArrayList<String>();
					authprefs.add(AuthPolicy.BASIC);
					basicAuth.setAuthSchemes(authprefs);
					basicAuth.setUsername(userDetails[0]);
					basicAuth.setPassword(userDetails[1]);
					basicAuth.setPreemptiveAuthentication(true);
					Options clientOptions = stub._getServiceClient().getOptions();
					clientOptions.setProperty(HTTPConstants.AUTHENTICATE, basicAuth);
					this.stubMap.put(key, stub);
				}
			}
		}
		return stub;
	}
	
	protected String getHttpBasicAuthPair() throws ClientInitException{
		String httpBasicUsername = "";
		String httpBasicPassword = "";
		if(this.basicAuthFromDb){
			IrideSiKeyrad siKeyrad;
			IrideSiKeyradDati siKeyradDati;
			try {
				siKeyrad = siKeyradService.getByKrNome(ModuleConfig.getProperty(IrideConstants.SAR_INTEG_USERNAME));
				siKeyradDati = siKeyradDatiService.getByKdKey(siKeyrad.getKrKey());
			} catch (DatabaseException e) {
				throw new ClientInitException("Attenzione! Servizio configurato per l'override di HttpBasicAuth ma non è stato possibile recuperare USERNAME dal DB");
			}
			httpBasicUsername = siKeyradDati.getKdValue();
			try {
				siKeyrad = siKeyradService.getByKrNome(ModuleConfig.getProperty(IrideConstants.SAR_INTEG_PASSWORD));
				siKeyradDati = siKeyradDatiService.getByKdKey(siKeyrad.getKrKey());
			} catch (DatabaseException e) {
				throw new ClientInitException("Attenzione! Servizio configurato per l'override di HttpBasicAuth ma non è stato possibile recuperare PASSWORD dal DB");
			}
			httpBasicPassword = siKeyradDati.getKdValue();
		}else{
			httpBasicUsername = ModuleConfig.getProperty(ConfigKeys.CONFIG_AXIS2_CLIENT_HTTP_AUTH_BASIC_USERNAME);
			httpBasicPassword = ModuleConfig.getProperty(ConfigKeys.CONFIG_AXIS2_CLIENT_HTTP_AUTH_BASIC_PASSWORD);
		}
		return new String(httpBasicUsername + ":" +  httpBasicPassword);
	}
	
	protected void setExceptionResponse(org.apache.xmlbeans.XmlObject ricevutaDocument, Exception e){
		ElencoErroriRicetteType elencoErroriRicette = ElencoErroriRicetteType.Factory.newInstance();
		elencoErroriRicette.addNewErroreRicetta().setTipoErrore(e.getMessage());
		
		if(ricevutaDocument instanceof VisualizzaErogatoRicevutaDocument){
			VisualizzaErogatoRicevutaDocument visualizzaErogatoRicevutaDocument = (VisualizzaErogatoRicevutaDocument) ricevutaDocument;
			if (visualizzaErogatoRicevutaDocument.getVisualizzaErogatoRicevuta() == null)
				visualizzaErogatoRicevutaDocument.addNewVisualizzaErogatoRicevuta();
			visualizzaErogatoRicevutaDocument.getVisualizzaErogatoRicevuta().setElencoErroriRicette(elencoErroriRicette);
		}
		else if(ricevutaDocument instanceof InvioErogatoRicevutaDocument){
			InvioErogatoRicevutaDocument invioErogatoRicevutaDocument = (InvioErogatoRicevutaDocument) ricevutaDocument;
			if (invioErogatoRicevutaDocument.getInvioErogatoRicevuta() == null)
				invioErogatoRicevutaDocument.addNewInvioErogatoRicevuta();
			invioErogatoRicevutaDocument.getInvioErogatoRicevuta().setElencoErroriRicette(elencoErroriRicette);
		}
		else if(ricevutaDocument instanceof AnnullaErogatoRicevutaDocument){
			AnnullaErogatoRicevutaDocument annullaErogatoRicevutaDocument = (AnnullaErogatoRicevutaDocument) ricevutaDocument;
			if (annullaErogatoRicevutaDocument.getAnnullaErogatoRicevuta() == null)
				annullaErogatoRicevutaDocument.addNewAnnullaErogatoRicevuta();
			annullaErogatoRicevutaDocument.getAnnullaErogatoRicevuta().setElencoErroriRicette(elencoErroriRicette);
		}
		else if(ricevutaDocument instanceof SospendiErogatoRicevutaDocument){
			SospendiErogatoRicevutaDocument sospendiErogatoRicevutaDocument = (SospendiErogatoRicevutaDocument) ricevutaDocument;
			if (sospendiErogatoRicevutaDocument.getSospendiErogatoRicevuta() == null)
				sospendiErogatoRicevutaDocument.addNewSospendiErogatoRicevuta();
			sospendiErogatoRicevutaDocument.getSospendiErogatoRicevuta().setElencoErroriRicette(elencoErroriRicette);
		}
	}

	protected Map<String, Stub> getStubMap() {
		return stubMap;
	}

	protected void setStubMap(Map<String, Stub> stubMap) {
		this.stubMap = stubMap;
	}

	protected SimpleServiceContext getServiceContext() {
		return serviceContext;
	}

	protected void setServiceContext(SimpleServiceContext serviceContext) {
		this.serviceContext = serviceContext;
	}

	protected Boolean getBasicAuthOverride() {
		return basicAuthOverride;
	}

	protected void setBasicAuthOverride(Boolean basicAuthOverride) {
		this.basicAuthOverride = basicAuthOverride;
	}

	protected Boolean getBasicAuthFromDb() {
		return basicAuthFromDb;
	}

	protected void setBasicAuthFromDb(Boolean basicAuthFromDb) {
		this.basicAuthFromDb = basicAuthFromDb;
	}

	public SiKeyradService getSiKeyradService() {
		return siKeyradService;
	}

	public void setSiKeyradService(SiKeyradService siKeyradService) {
		this.siKeyradService = siKeyradService;
	}

	public SiKeyradDatiService getSiKeyradDatiService() {
		return siKeyradDatiService;
	}

	public void setSiKeyradDatiService(SiKeyradDatiService siKeyradDatiService) {
		this.siKeyradDatiService = siKeyradDatiService;
	}
}
