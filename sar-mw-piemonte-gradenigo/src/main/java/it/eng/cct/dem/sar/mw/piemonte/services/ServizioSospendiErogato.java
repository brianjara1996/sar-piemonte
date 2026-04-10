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
import it.eng.cct.dem.sar.mw.piemonte.constants.SospendiErogatoConstants;
import it.eng.cct.dem.sar.mw.piemonte.db.service.trace.ServiceMonitoringHelper;
import it.finanze.sanita.dem.wsdl.sospendierogato.DemSospendiErogatoSkeletonInterface;
import it.finanze.sanita.dem.wsdl.sospendierogato.DemSospendiErogatoStub;
import it.finanze.sanita.dem.xsd.sospendierogatoricevuta.SospendiErogatoRicevutaDocument;
import it.finanze.sanita.dem.xsd.sospendierogatoricevuta.SospendiErogatoRicevutaDocument.Factory;
import it.finanze.sanita.dem.xsd.sospendierogatorichiesta.SospendiErogatoRichiestaDocument;

public class ServizioSospendiErogato extends ServizioErogato implements DemSospendiErogatoSkeletonInterface {
	
	private static final Logger logger = LoggerFactory.getLogger(ServizioSospendiErogato.class);

	public ServizioSospendiErogato(SimpleServiceContext serviceContext) throws AxisFault, ClientInitException {
		super(Collections.synchronizedMap(new HashMap<String, org.apache.axis2.client.Stub>()), serviceContext, ModuleConfig.getProperty(SospendiErogatoConstants.CONFIG_SOSPENDIEROGATO_SERVICE_ENDPOINT));
		setBasicAuthOverride(ModuleConfig.getPropertyAsBoolean(SospendiErogatoConstants.CONFIG_SOSPENDIEROGATO_HTTP_AUTH_BASIC_OVERRIDE, Boolean.FALSE));
		setBasicAuthFromDb(ModuleConfig.getPropertyAsBoolean(MwConstants.CONFIG_SAR_CREDENTIAL_FROM_IRIDE_DB, Boolean.FALSE));
	}

	public SospendiErogatoRicevutaDocument sospendiErogato(SospendiErogatoRichiestaDocument sospendiErogatoRichiesta) {
		DemSospendiErogatoStub demSospendiErogatoStub = null;
		MessageContext msgCtx = MessageContext.getCurrentMessageContext();
		HttpServletRequest obj = (HttpServletRequest) msgCtx.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
		String au = obj.getHeader(HTTPConstants.HEADER_AUTHORIZATION);
		ServiceMonitoringHelper serviceMonitoringHelper = new ServiceMonitoringHelper();
		serviceMonitoringHelper.setMwInputBean(sospendiErogatoRichiesta);
		SospendiErogatoRicevutaDocument sospendiErogatoRicevutaDocument = Factory.newInstance();
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
				demSospendiErogatoStub = (DemSospendiErogatoStub) getStub(pair, DemSospendiErogatoStub.class.getName());
			}
			serviceMonitoringHelper.setMwToServiceBean(sospendiErogatoRichiesta);
			sospendiErogatoRicevutaDocument = demSospendiErogatoStub.sospendiErogato(sospendiErogatoRichiesta);

		} catch (AxisFault e) {
			logger.error(e.getMessage());
			setExceptionResponse(sospendiErogatoRicevutaDocument, e);
		} catch (RemoteException e) {
			logger.error(e.getMessage());
			setExceptionResponse(sospendiErogatoRicevutaDocument, e);
		} catch (ClientInitException e) {
			logger.error(e.getMessage());
			setExceptionResponse(sospendiErogatoRicevutaDocument, e);
		}
		serviceMonitoringHelper.setServiceToMwBean(sospendiErogatoRicevutaDocument);
		serviceMonitoringHelper.setMwOutputBean(sospendiErogatoRicevutaDocument);
		serviceMonitoringHelper.run();
		return sospendiErogatoRicevutaDocument;
	}

}
