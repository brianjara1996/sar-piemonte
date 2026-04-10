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
import it.eng.cct.dem.sar.mw.piemonte.constants.MwConstants;
import it.eng.cct.dem.sar.mw.piemonte.constants.VisualizzaErogatoConstants;
import it.eng.cct.dem.sar.mw.piemonte.db.service.trace.ServiceMonitoringHelper;
import it.finanze.sanita.dem.dpcm.wsdl.visualizzaErogato.service.DemVisualizzaErogatoStub;
import it.finanze.sanita.dem.wsdl.visualizzaerogato.DemVisualizzaErogatoSkeletonInterface;
import it.finanze.sanita.dem.xsd.visualizzaerogatoricevuta.VisualizzaErogatoRicevutaDocument;
import it.finanze.sanita.dem.xsd.visualizzaerogatoricevuta.VisualizzaErogatoRicevutaDocument.Factory;
import it.finanze.sanita.dem.xsd.visualizzaerogatorichiesta.VisualizzaErogatoRichiestaDocument;

public class ServizioVisualizzaErogato extends ServizioErogato implements DemVisualizzaErogatoSkeletonInterface {
	
	private static final Logger logger = LoggerFactory.getLogger(ServizioVisualizzaErogato.class);

	public ServizioVisualizzaErogato(SimpleServiceContext serviceContext) throws AxisFault, ClientInitException {
		super(Collections.synchronizedMap(new HashMap<String, org.apache.axis2.client.Stub>()), serviceContext, ModuleConfig.getProperty(VisualizzaErogatoConstants.CONFIG_VISUALIZZAEROGATO_SERVICE_ENDPOINT));
		setBasicAuthOverride(ModuleConfig.getPropertyAsBoolean(VisualizzaErogatoConstants.CONFIG_VISUALIZZAEROGATO_HTTP_AUTH_BASIC_OVERRIDE, Boolean.FALSE));
		setBasicAuthFromDb(ModuleConfig.getPropertyAsBoolean(MwConstants.CONFIG_SAR_CREDENTIAL_FROM_IRIDE_DB, Boolean.FALSE));
	}

	public VisualizzaErogatoRicevutaDocument visualizzaErogato(VisualizzaErogatoRichiestaDocument visualizzaErogatoRichiesta) {
		DemVisualizzaErogatoStub demVisualizzaErogatoStub = null;
		MessageContext msgCtx = MessageContext.getCurrentMessageContext();
		HttpServletRequest obj = (HttpServletRequest) msgCtx.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
		String au = obj.getHeader(HTTPConstants.HEADER_AUTHORIZATION);
		ServiceMonitoringHelper serviceMonitoringHelper = new ServiceMonitoringHelper();
		serviceMonitoringHelper.setMwInputBean(visualizzaErogatoRichiesta);
		VisualizzaErogatoRicevutaDocument visualizzaErogatoRicevutaDocument = Factory.newInstance();
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
				demVisualizzaErogatoStub = (DemVisualizzaErogatoStub) getStub(pair, DemVisualizzaErogatoStub.class.getName());
			}
			serviceMonitoringHelper.setMwToServiceBean(visualizzaErogatoRichiesta);
			visualizzaErogatoRicevutaDocument = demVisualizzaErogatoStub.visualizzaErogato(visualizzaErogatoRichiesta);

		} catch (AxisFault e) {
			logger.error(e.getMessage());
			setExceptionResponse(visualizzaErogatoRicevutaDocument, e);
		} catch (RemoteException e) {
			setExceptionResponse(visualizzaErogatoRicevutaDocument, e);
			logger.error(e.getMessage());
		} catch (ClientInitException e) {
			logger.error(e.getMessage());
			setExceptionResponse(visualizzaErogatoRicevutaDocument, e);
		}
		serviceMonitoringHelper.setServiceToMwBean(visualizzaErogatoRicevutaDocument);
		serviceMonitoringHelper.setMwOutputBean(visualizzaErogatoRicevutaDocument);
		serviceMonitoringHelper.run();
		return visualizzaErogatoRicevutaDocument;
	}
	
}
