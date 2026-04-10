package it.eng.cct.dem.sar.mw.piemonte.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.cct.dem.sar.mw.piemonte.db.service.trace.ServiceMonitoringHelper;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.EPrescriptionsDocument;
import it.eng.cct.dem.sar.piemonte.mw.schemas.SarMiddleware.EPrescriptionServiceSkeletonInterface;
import it.eng.cct.dem.sar.piemonte.mw.schemas.SarMiddleware.ServiceFault;
import it.eng.cct.piemonte.eprescription.types.AnnullamentoDocument;
import it.eng.cct.piemonte.eprescription.types.EsitiDocument;
import it.eng.cct.piemonte.eprescription.types.InterrogaDocument;
import it.eng.cct.piemonte.eprescription.types.RecuperoDocument;
import it.eng.cct.piemonte.eprescription.types.TrasmissioneDocument;

public class ServizioEPrescriptionImpl extends ServizioEPrescription implements EPrescriptionServiceSkeletonInterface {

	private static final Logger logger = LoggerFactory.getLogger(ServizioEPrescriptionImpl.class);

	public ServizioEPrescriptionImpl() {
	}

	public AnnullamentoDocument annullamento(AnnullamentoDocument request) throws ServiceFault {
		ServiceMonitoringHelper serviceMonitoringHelper = new ServiceMonitoringHelper();
		serviceMonitoringHelper.setMwInputBean(request);
		logger.info("Serving ServizioTrasmissione");
		EPrescriptionsDocument ePrescriptionsResponse = null;
		EPrescriptionsDocument ePrescriptionsRequest = this.parseEprescriptions(request);
		AnnullamentoDocument response = AnnullamentoDocument.Factory.newInstance();
		TipoTrasmissione tipoTrasmissione = this.getTipoTrasmissione(ePrescriptionsRequest);
		switch (tipoTrasmissione) {
		case DM02112011:
			ServizioAnnullamentoExecutor executor = new ServizioAnnullamentoExecutor(this.serviceRegistry, serviceMonitoringHelper);
			ePrescriptionsResponse = this.iterate(ePrescriptionsRequest, executor);
			break;
		case DPCM26032008:
			this.prettyPrintInput(ePrescriptionsRequest);
			InvioTelematicoSanitaExecutor dpcmExecutor = new InvioTelematicoSanitaExecutor(this.serviceRegistry, serviceMonitoringHelper);
			ePrescriptionsResponse = dpcmExecutor.annulla(ePrescriptionsRequest);
			this.prettyPrintOutput(ePrescriptionsResponse);
			break;
		}
		response.addNewAnnullamento().setXml(ePrescriptionsResponse.xmlText(this.getXmlOptions()));
		serviceMonitoringHelper.setMwOutputBean(response);
		serviceMonitoringHelper.setMwInputBean(request);
		serviceMonitoringHelper.run();
		return response;
	}

	public RecuperoDocument recupero(RecuperoDocument request) throws ServiceFault {
		logger.info("Serving ServizioTrasmissione");
		ServiceMonitoringHelper serviceMonitoringHelper = new ServiceMonitoringHelper();
		serviceMonitoringHelper.setMwInputBean(request);
		EPrescriptionsDocument ePrescriptionsRequest = this.parseEprescriptions(request);
		ServizioRecuperoExecutor executor = new ServizioRecuperoExecutor(this.serviceRegistry, serviceMonitoringHelper);
		EPrescriptionsDocument ePrescriptionsResponse = this.iterate(ePrescriptionsRequest, executor);
		RecuperoDocument response = RecuperoDocument.Factory.newInstance();
		response.addNewRecupero().setXml(ePrescriptionsResponse.xmlText(this.getXmlOptions()));
		serviceMonitoringHelper.setMwOutputBean(response);
		return response;
	}

	public TrasmissioneDocument trasmissione(TrasmissioneDocument request) throws ServiceFault {
		logger.info("Serving ServizioTrasmissione");
		ServiceMonitoringHelper serviceMonitoringHelper = new ServiceMonitoringHelper();
		serviceMonitoringHelper.setMwInputBean(request);
		EPrescriptionsDocument ePrescriptionsResponse = null;
		EPrescriptionsDocument ePrescriptionsRequest = this.parseEprescriptions(request);
		TrasmissioneDocument response = TrasmissioneDocument.Factory.newInstance();
		TipoTrasmissione tipoTrasmissione = this.getTipoTrasmissione(ePrescriptionsRequest);
		switch (tipoTrasmissione) {
		case DM02112011:
			ServizioTrasmissioneExecutor dmExecutor = new ServizioTrasmissioneExecutor(this.serviceRegistry, serviceMonitoringHelper);
			ePrescriptionsResponse = this.iterate(ePrescriptionsRequest, dmExecutor);
			break;
		case DPCM26032008:
			this.prettyPrintInput(ePrescriptionsRequest);
			InvioTelematicoSanitaExecutor dpcmExecutor = new InvioTelematicoSanitaExecutor(this.serviceRegistry, serviceMonitoringHelper);
			ePrescriptionsResponse = dpcmExecutor.trasmetti(ePrescriptionsRequest);
			this.prettyPrintOutput(ePrescriptionsResponse);
			break;
		}
		response.addNewTrasmissione().setXml(ePrescriptionsResponse.xmlText(this.getXmlOptions()));
		serviceMonitoringHelper.setMwOutputBean(response);
		serviceMonitoringHelper.run();
		return response;
	}

	public InterrogaDocument interroga(InterrogaDocument request) throws ServiceFault {
		logger.info("Serving ServizioTrasmissione");
		ServiceMonitoringHelper serviceMonitoringHelper = new ServiceMonitoringHelper();
		serviceMonitoringHelper.setMwInputBean(request);
		EPrescriptionsDocument ePrescriptionsRequest = this.parseEprescriptions(request);
		this.prettyPrintInput(ePrescriptionsRequest);
		ServizioInterrogaExecutor executor = new ServizioInterrogaExecutor(this.serviceRegistry, serviceMonitoringHelper);
		EPrescriptionsDocument ePrescriptionsResponse = executor.invoke(ePrescriptionsRequest);
		InterrogaDocument response = InterrogaDocument.Factory.newInstance();
		response.addNewInterroga().setXml(ePrescriptionsResponse.xmlText(this.getXmlOptions()));
		this.prettyPrintOutput(ePrescriptionsResponse);
		serviceMonitoringHelper.setMwOutputBean(response);
		return response;
	}

	public EsitiDocument esiti(EsitiDocument request) throws ServiceFault {
		logger.info("Serving ServizioTrasmissione");
		ServiceMonitoringHelper serviceMonitoringHelper = new ServiceMonitoringHelper();
		serviceMonitoringHelper.setMwInputBean(request);
		EPrescriptionsDocument ePrescriptionsResponse = null;
		EPrescriptionsDocument ePrescriptionsRequest = this.parseEprescriptions(request);
		EsitiDocument response = EsitiDocument.Factory.newInstance();
		TipoEsito tipoEsito = this.getTipoEsito(ePrescriptionsRequest);
		switch (tipoEsito) {
		case SINTETICO:
			this.prettyPrintInput(ePrescriptionsRequest);
			ElencoSinteticoStatoInviiExecutor esExecutor = new ElencoSinteticoStatoInviiExecutor(this.serviceRegistry, serviceMonitoringHelper);
			ePrescriptionsResponse = esExecutor.invoke(ePrescriptionsRequest);
			this.prettyPrintOutput(ePrescriptionsResponse);
			break;
		case ANALITICO:
			this.prettyPrintInput(ePrescriptionsRequest);
			ElencoAnaliticoEsitoRicetteExecutor eaExecutor = new ElencoAnaliticoEsitoRicetteExecutor(this.serviceRegistry, serviceMonitoringHelper);
			ePrescriptionsResponse = eaExecutor.invoke(ePrescriptionsRequest);
			this.prettyPrintOutput(ePrescriptionsResponse);
			break;
		}
		response.addNewEsiti().setXml(ePrescriptionsResponse.xmlText(this.getXmlOptions()));
		serviceMonitoringHelper.setMwOutputBean(response);
		return response;
	}

}
