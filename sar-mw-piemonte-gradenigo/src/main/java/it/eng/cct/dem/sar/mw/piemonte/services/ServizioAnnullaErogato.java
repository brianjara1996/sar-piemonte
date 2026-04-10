package it.eng.cct.dem.sar.mw.piemonte.services;

import java.rmi.RemoteException;
import java.util.Collections;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.cct.dem.sar.generic.wsclient.commons.context.impl.SimpleServiceContext;
import it.eng.cct.dem.sar.generic.wsclient.commons.exceptions.ClientInitException;
import it.eng.cct.dem.sar.generic.wsclient.commons.utils.ModuleConfig;
import it.eng.cct.dem.sar.mw.piemonte.constants.AnnullaErogatoConstants;
import it.eng.cct.dem.sar.mw.piemonte.constants.MwConstants;
import it.eng.cct.dem.sar.mw.piemonte.db.service.trace.ServiceMonitoringHelper;
import it.finanze.sanita.dem.wsdl.annullaerogato.DemAnnullaErogatoSkeletonInterface;
import it.finanze.sanita.dem.wsdl.annullaerogato.DemAnnullaErogatoStub;
import it.finanze.sanita.dem.xsd.annullaerogatoricevuta.AnnullaErogatoRicevutaDocument;
import it.finanze.sanita.dem.xsd.annullaerogatoricevuta.AnnullaErogatoRicevutaDocument.Factory;
import it.finanze.sanita.dem.xsd.annullaerogatorichiesta.AnnullaErogatoRichiestaDocument;

public class ServizioAnnullaErogato extends ServizioErogato implements DemAnnullaErogatoSkeletonInterface {
	
	private static final Logger logger = LoggerFactory.getLogger(ServizioAnnullaErogato.class);

	public ServizioAnnullaErogato(SimpleServiceContext serviceContext) throws AxisFault, ClientInitException {
		super(Collections.synchronizedMap(new HashMap<String, org.apache.axis2.client.Stub>()), serviceContext, ModuleConfig.getProperty(AnnullaErogatoConstants.CONFIG_ANNULLAEROGATO_SERVICE_ENDPOINT));
		setBasicAuthOverride(ModuleConfig.getPropertyAsBoolean(AnnullaErogatoConstants.CONFIG_ANNULLAEROGATO_HTTP_AUTH_BASIC_OVERRIDE, Boolean.FALSE));
		setBasicAuthFromDb(ModuleConfig.getPropertyAsBoolean(MwConstants.CONFIG_SAR_CREDENTIAL_FROM_IRIDE_DB, Boolean.FALSE));
	}

	public AnnullaErogatoRicevutaDocument annullaErogato(AnnullaErogatoRichiestaDocument annullaErogatoRichiesta) {
		DemAnnullaErogatoStub demAnnullaErogatoStub = null;
		MessageContext msgCtx = MessageContext.getCurrentMessageContext();
		HttpServletRequest obj = (HttpServletRequest) msgCtx.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
		String au = obj.getHeader(HTTPConstants.HEADER_AUTHORIZATION);
		ServiceMonitoringHelper serviceMonitoringHelper = new ServiceMonitoringHelper();
		serviceMonitoringHelper.setMwInputBean(annullaErogatoRichiesta);
		AnnullaErogatoRicevutaDocument annullaErogatoRicevuta = Factory.newInstance();
		try {
			if ((au != null && au.length() > 0) || getBasicAuthOverride()) {
				String pair = "";
				if(getBasicAuthOverride()){
					pair = getHttpBasicAuthPair();
				}
				else{
					byte[] decodedBytes = Base64.decodeBase64(au.substring("Basic".length()).trim().getBytes());
					pair = new String(decodedBytes);
				}
				demAnnullaErogatoStub = (DemAnnullaErogatoStub) getStub(pair, DemAnnullaErogatoStub.class.getName());
			}
			serviceMonitoringHelper.setMwToServiceBean(annullaErogatoRichiesta);
			annullaErogatoRicevuta = demAnnullaErogatoStub.annullaErogato(annullaErogatoRichiesta);

		} catch (AxisFault e) {
			logger.error(e.getMessage());
			setExceptionResponse(annullaErogatoRicevuta, e);
		} catch (RemoteException e) {
			logger.error(e.getMessage());
			setExceptionResponse(annullaErogatoRicevuta, e);
		} catch (ClientInitException e) {
			logger.error(e.getMessage());
			setExceptionResponse(annullaErogatoRicevuta, e);
		}
		serviceMonitoringHelper.setServiceToMwBean(annullaErogatoRicevuta);
		serviceMonitoringHelper.setMwOutputBean(annullaErogatoRicevuta);
		serviceMonitoringHelper.run();
		return annullaErogatoRicevuta;
	}

}
