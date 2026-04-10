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
import it.eng.cct.dem.sar.mw.piemonte.constants.InvioErogatoConstants;
import it.eng.cct.dem.sar.mw.piemonte.constants.MwConstants;
import it.eng.cct.dem.sar.mw.piemonte.db.service.trace.ServiceMonitoringHelper;
import it.finanze.sanita.dem.dpcm.wsdl.invioErogato.service.DemInvioErogatoStub;
import it.finanze.sanita.dem.wsdl.invioerogato.DemInvioErogatoSkeletonInterface;
import it.finanze.sanita.dem.xsd.invioerogatoricevuta.InvioErogatoRicevutaDocument;
import it.finanze.sanita.dem.xsd.invioerogatoricevuta.InvioErogatoRicevutaDocument.Factory;
import it.finanze.sanita.dem.xsd.invioerogatorichiesta.InvioErogatoRichiestaDocument;

public class ServizioInvioErogato extends ServizioErogato implements DemInvioErogatoSkeletonInterface {

	private static final Logger logger = LoggerFactory.getLogger(ServizioInvioErogato.class);

	public ServizioInvioErogato(SimpleServiceContext serviceContext) throws AxisFault, ClientInitException {
		super(Collections.synchronizedMap(new HashMap<String, org.apache.axis2.client.Stub>()), serviceContext, ModuleConfig.getProperty(InvioErogatoConstants.CONFIG_INVIOEROGATO_SERVICE_ENDPOINT));
		setBasicAuthOverride(ModuleConfig.getPropertyAsBoolean(InvioErogatoConstants.CONFIG_INVIOEROGATO_HTTP_AUTH_BASIC_OVERRIDE, Boolean.FALSE));
		setBasicAuthFromDb(ModuleConfig.getPropertyAsBoolean(MwConstants.CONFIG_SAR_CREDENTIAL_FROM_IRIDE_DB, Boolean.FALSE));
	}

	public InvioErogatoRicevutaDocument invioErogato(InvioErogatoRichiestaDocument invioErogatoRichiesta) {
		DemInvioErogatoStub demInvioErogatoStub = null;
		MessageContext msgCtx = MessageContext.getCurrentMessageContext();
		HttpServletRequest obj = (HttpServletRequest) msgCtx.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
		String au = obj.getHeader(HTTPConstants.HEADER_AUTHORIZATION);
		ServiceMonitoringHelper serviceMonitoringHelper = new ServiceMonitoringHelper();
		serviceMonitoringHelper.setMwInputBean(invioErogatoRichiesta);
		InvioErogatoRicevutaDocument invioErogatoRicevutaDocument = Factory.newInstance();
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
				demInvioErogatoStub = (DemInvioErogatoStub) getStub(pair, DemInvioErogatoStub.class.getName());
			}
			serviceMonitoringHelper.setMwToServiceBean(invioErogatoRichiesta);
			invioErogatoRicevutaDocument = demInvioErogatoStub.invioErogato(invioErogatoRichiesta);

		} catch (AxisFault e) {
			logger.error(e.getMessage());
			setExceptionResponse(invioErogatoRicevutaDocument, e);
		} catch (RemoteException e) {
			logger.error(e.getMessage());
			setExceptionResponse(invioErogatoRicevutaDocument, e);
		} catch (ClientInitException e) {
			logger.error(e.getMessage());
			setExceptionResponse(invioErogatoRicevutaDocument, e);
		}
		serviceMonitoringHelper.setServiceToMwBean(invioErogatoRicevutaDocument);
		serviceMonitoringHelper.setMwOutputBean(invioErogatoRicevutaDocument);
		serviceMonitoringHelper.run();
		return invioErogatoRicevutaDocument;
	}

}
