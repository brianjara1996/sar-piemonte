package it.eng.cct.dem.sar.mw.piemonte.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.cct.dem.sar.generic.wsclient.commons.utils.ModuleConfig;
import it.eng.cct.dem.sar.mw.exceptions.MwException;
import it.eng.cct.dem.sar.mw.piemonte.adapters.SistemaTiesseAdapter;
import it.eng.cct.dem.sar.mw.piemonte.constants.EPrescriptionConstants;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.EPrescriptionsDocument;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TEPrescription;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TEPrescriptions;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TErrore;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TErrori;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TListaMessaggi;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TMessageObj;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TRicetta;
import it.eng.cct.dem.sar.mw.services.DemService;
import it.eng.cct.dem.sar.mw.services.IServiceRegistry;
import it.eng.cct.dem.sar.piemonte.mw.schemas.SarMiddleware.ServiceFault;
import it.eng.cct.piemonte.eprescription.types.AnnullamentoDocument;
import it.eng.cct.piemonte.eprescription.types.EsitiDocument;
import it.eng.cct.piemonte.eprescription.types.FaultDocument;
import it.eng.cct.piemonte.eprescription.types.InterrogaDocument;
import it.eng.cct.piemonte.eprescription.types.RecuperoDocument;
import it.eng.cct.piemonte.eprescription.types.TFaultEPrescription;
import it.eng.cct.piemonte.eprescription.types.TrasmissioneDocument;

public abstract class ServizioEPrescription extends DemService {

	public enum TipoTrasmissione {
		DM02112011, DPCM26032008
	}

	public enum TipoEsito {
		SINTETICO, ANALITICO
	}

	private static final Logger logger = LoggerFactory.getLogger(ServizioEPrescription.class);

	protected IServiceRegistry serviceRegistry;

	protected static TEPrescriptions buildErrorMessage(TEPrescriptions request, MwException e, List<TErrore> erroriAndComunicazioni) {
		return ServizioEPrescription.buildErrorMessage(request, e, erroriAndComunicazioni, null);
	}

	protected EPrescriptionsDocument iterate(EPrescriptionsDocument request, EPrescriptionServiceExecutor executor) {
		this.prettyPrintInput(request);
		TEPrescription[] inBoundEPrescriptions = request.getEPrescriptions().getEPrescriptionArray();
		EPrescriptionsDocument response = EPrescriptionsDocument.Factory.newInstance();
		TEPrescriptions outBoundEpList = response.addNewEPrescriptions();
		TEPrescription[] outBoundEPrescriptions = new TEPrescription[inBoundEPrescriptions.length];
		for (int i = 0; i < inBoundEPrescriptions.length; i++) {
			TEPrescription inBoundEPrescription = inBoundEPrescriptions[i];
			try {
				outBoundEPrescriptions[i] = executor.invoke(inBoundEPrescription);
			} catch (ServiceFault e) {
				logger.error(e.getMessage(), e);
				if (e.getCause() instanceof MwException) {
					outBoundEPrescriptions[i] = ServizioEPrescription.buildErrorMessage(inBoundEPrescription, (MwException) e.getCause(), null);
				} else {
					outBoundEPrescriptions[i] = ServizioEPrescription.buildErrorMessage(inBoundEPrescription, null, null);
				}
			} catch (Throwable e) {
				logger.error(e.getMessage(), e);
				outBoundEPrescriptions[i] = ServizioEPrescription.buildErrorMessage(inBoundEPrescription, null, null);
			}
		}
		outBoundEpList.setEPrescriptionArray(outBoundEPrescriptions);
		this.prettyPrintOutput(response);
		return response;
	}

	protected static TEPrescriptions buildErrorMessage(TEPrescriptions request, MwException e, List<TErrore> erroriAndComunicazioni, String codEsito) {
		TEPrescriptions response = TEPrescriptions.Factory.newInstance();
		TEPrescription[] requestedEPrescriptionArray = request.getEPrescriptionArray();
		boolean isRichiestaEsitiDpcm = StringUtils.isNotEmpty(request.getTipoEsito());
		if (isRichiestaEsitiDpcm) {
			return ServizioEPrescription.buildErrorMessageEsitiDpcm(request, e);
		}
		TEPrescription[] responseEPrescriptionArray = new TEPrescription[requestedEPrescriptionArray.length];
		for (int i = 0; i < requestedEPrescriptionArray.length; i++) {
			responseEPrescriptionArray[i] = ServizioEPrescription.buildErrorMessage(requestedEPrescriptionArray[i], e, erroriAndComunicazioni, codEsito);
		}
		response.setEPrescriptionArray(responseEPrescriptionArray);
		return response;
	}

	protected static TEPrescriptions buildErrorMessageEsitiDpcm(TEPrescriptions request, MwException e) {
		TEPrescriptions response = TEPrescriptions.Factory.newInstance();
		String protocolloSAC = request.getProtocolloSAC();
		if (StringUtils.isNotEmpty(protocolloSAC)) {
			response.setProtocolloSAC(protocolloSAC);
		}
		String dataIniRange = request.getDataIniRange();
		if (StringUtils.isNotEmpty(dataIniRange)) {
			response.setDataIniRange(dataIniRange);
		}
		String dataFineRange = request.getDataFineRange();
		if (StringUtils.isNotEmpty(dataFineRange)) {
			response.setDataFineRange(dataFineRange);
		}
		String tipoEsito = request.getTipoEsito();
		if (StringUtils.isNotEmpty(tipoEsito)) {
			response.setTipoEsito(tipoEsito);
		}
		if (e != null) {
			TListaMessaggi listaMessaggi = response.addNewListaMessaggi();
			TMessageObj errore = listaMessaggi.addNewMessageObj();
			errore.setCodiceMessaggio(e.getFaultCode());
			errore.setDescrizioneMessaggio(e.getFaultReason());
		}
		return response;

	}

	protected static TEPrescription buildErrorMessage(TEPrescription request, MwException e, List<TErrore> erroriAndComunicazioni) {
		return ServizioEPrescription.buildErrorMessage(request, e, erroriAndComunicazioni, null);
	}

	protected static TEPrescription buildErrorMessage(TEPrescription request, MwException e, List<TErrore> erroriAndComunicazioni, String codEsito) {
		if (logger.isDebugEnabled()) {
			logger.debug("Building error message...");
		}
		TEPrescription response = TEPrescription.Factory.newInstance();
		response.setId(request.getId());
		if (StringUtils.isNotEmpty(codEsito)) {
			response.setEsito(codEsito);
		} else {
			response.setEsito(ModuleConfig.getProperty(EPrescriptionConstants.CONFIG_WS_EPRESCRIPTION_ESITO_NEGATIVO_CODE));
		}
		if (request.getRicetta() != null) {
			TRicetta ricetta = response.addNewRicetta();
			String nre = request.getRicetta().getNRE();
			if (StringUtils.isNotEmpty(nre)) {
				ricetta.setNRE(nre);
			}
		}
		TErrori errori = response.addNewErrori();
		if (erroriAndComunicazioni == null) {
			erroriAndComunicazioni = new ArrayList<TErrore>();
		}
		if (e != null && e.isAxisFault()) {
			if (logger.isDebugEnabled()) {
				logger.debug("Exception " + e.getClass().getCanonicalName() + " is a coded axis fault.");
			}
			TErrore errore = TErrore.Factory.newInstance();
			errore.setCodice(e.getFaultCode());
			errore.setDescrizione(e.getFaultReason());
			erroriAndComunicazioni.add(0, errore);
		}
		if (erroriAndComunicazioni.isEmpty()) {
			if (logger.isDebugEnabled()) {
				logger.debug("Response does not contains any coded errors.");
			}
			TErrore errore = errori.addNewErrore();
			errore.setCodice("9999");
			errore.setDescrizione("Errore generico di sistema.");
			erroriAndComunicazioni.add(errore);
		}
		TErrore[] erroreArray = new TErrore[erroriAndComunicazioni.size()];
		for (int i = 0; i < erroriAndComunicazioni.size(); i++) {
			erroreArray[i] = erroriAndComunicazioni.get(i);
		}
		errori.setErroreArray(erroreArray);
		return response;
	}

	protected void setMappedDiagnosticContext(TEPrescription ePrescription) {
		if (ePrescription != null) {
			super.setMappedDiagnosticContext(ePrescription.getId());
		}
	}

	protected static ServiceFault newServiceFault(MwException exception) {
		return ServizioEPrescription.newServiceFault(TEPrescriptions.Factory.newInstance(), exception);
	}

	protected static ServiceFault newServiceFault(TEPrescriptions ePrescriptions, MwException exception) {
		ServiceFault serviceFault = new ServiceFault(exception.getMessage(), exception);
		FaultDocument faultDocument = FaultDocument.Factory.newInstance();
		TFaultEPrescription fault = faultDocument.addNewFault();
		if (ePrescriptions != null && ePrescriptions.getEPrescriptionArray() != null) {
			String message = ServizioEPrescription.buildErrorMessage(ePrescriptions, null, null).xmlText();
			fault.setXml(StringEscapeUtils.escapeXml(message));
		} else {
			fault.setXml("Impossibile interpretare la risposta del servizio.");
		}
		serviceFault.setFaultMessage(faultDocument);
		return serviceFault;
	}

	protected static ServiceFault newServiceFault(TEPrescription ePrescription, MwException exception) {
		ServiceFault serviceFault = new ServiceFault(exception.getMessage(), exception);
		FaultDocument faultDocument = FaultDocument.Factory.newInstance();
		TFaultEPrescription fault = faultDocument.addNewFault();
		if (ePrescription != null) {
			String message = ServizioEPrescription.buildErrorMessage(ePrescription, null, null).xmlText();
			fault.setXml(StringEscapeUtils.escapeXml(message));
		} else {
			fault.setXml("Impossibile interpretare la risposta del servizio.");
		}
		serviceFault.setFaultMessage(faultDocument);
		return serviceFault;
	}

	protected XmlOptions getXmlOptions() {
		XmlOptions xmlOptions = new XmlOptions();
		// Map<String,String> substNamespaces = new HashMap<String, String>();
		// substNamespaces.put("",
		// "http://cct.eng.it/piemonte/ePrescription/types");
		// xmlOptions.setLoadSubstituteNamespaces(substNamespaces);
		Map<String, String> suggestedPrefixes = new HashMap<String, String>();
		suggestedPrefixes.put("pollo", "http://cct.eng.it/piemonte/ePrescription/types");
		xmlOptions.setUseDefaultNamespace();
		// xmlOptions.setSaveSuggestedPrefixes(suggestedPrefixes);
		// xmlOptions.setSaveAggressiveNamespaces();
		return xmlOptions;
	}

	public EPrescriptionsDocument parse(String xml) throws ServiceFault {
		EPrescriptionsDocument result = null;
		try {
			result = EPrescriptionsDocument.Factory.parse(xml, this.getXmlOptions());
		} catch (XmlException e) {
			throw newServiceFault(new MwException(e.getMessage(), e));
		}
		return result;
	}

	protected EPrescriptionsDocument parseEprescriptions(AnnullamentoDocument request) throws ServiceFault {
		return this.parse(StringEscapeUtils.unescapeXml(request.getAnnullamento().getXml()));
	}

	protected EPrescriptionsDocument parseEprescriptions(TrasmissioneDocument request) throws ServiceFault {
		return this.parse(StringEscapeUtils.unescapeXml(request.getTrasmissione().getXml()));
	}

	protected EPrescriptionsDocument parseEprescriptions(InterrogaDocument request) throws ServiceFault {
		return this.parse(StringEscapeUtils.unescapeXml(request.getInterroga().getXml()));
	}

	protected EPrescriptionsDocument parseEprescriptions(RecuperoDocument request) throws ServiceFault {
		return this.parse(StringEscapeUtils.unescapeXml(request.getRecupero().getXml()));
	}

	protected EPrescriptionsDocument parseEprescriptions(EsitiDocument request) throws ServiceFault {
		return this.parse(StringEscapeUtils.unescapeXml(request.getEsiti().getXml()));
	}

	protected void prettyPrintInput(EPrescriptionsDocument eprescriptions) {
		if (!logger.isDebugEnabled()) {
			return;
		}
		logger.debug("\r\n********************** INPUT MESSAGE BEGIN **********************\r\n" + this.prettyPrintEprescription(eprescriptions) + "\r\n*********************** INPUT MESSAGE END **********************\r\n");

	}

	protected void prettyPrintOutput(EPrescriptionsDocument eprescriptions) {
		if (!logger.isDebugEnabled()) {
			return;
		}
		logger.debug("\r\n********************** OUTPUT MESSAGE BEGIN **********************\r\n" + this.prettyPrintEprescription(eprescriptions) + "\r\n********************** OUTPUT MESSAGE END **********************\r\n");
	}

	protected String prettyPrintEprescription(EPrescriptionsDocument eprescriptions) {
		XmlOptions xmlOptions = new XmlOptions();
		xmlOptions.setSavePrettyPrint();
		String msgText = eprescriptions.xmlText(xmlOptions);
		return msgText;
	}

	public void setServices(IServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

	protected TipoEsito getTipoEsito(EPrescriptionsDocument ePrescriptionsRequest) {
		TEPrescriptions ePrescriptions = ePrescriptionsRequest.getEPrescriptions();
		String tipoEsito = ePrescriptions.getTipoEsito();
		if (tipoEsito.equalsIgnoreCase("A")) {
			return TipoEsito.ANALITICO;
		} else if (tipoEsito.equalsIgnoreCase("S")) {
			return TipoEsito.SINTETICO;
		}
		return null;
	}

	protected TipoTrasmissione getTipoTrasmissione(EPrescriptionsDocument ePrescriptionsRequest) {
		TEPrescriptions ePrescriptions = ePrescriptionsRequest.getEPrescriptions();
		TEPrescription firstEPrescription = ePrescriptions.getEPrescriptionArray(0);
		String trasmissioneDPCM2008 = firstEPrescription.getTrasmissioneDPCM2008();
		if (StringUtils.isEmpty(trasmissioneDPCM2008)) {
			return TipoTrasmissione.DM02112011;
		}
		if (trasmissioneDPCM2008.equals(SistemaTiesseAdapter.WS_EPRESCRIPTION_BOOLEAN_YES)) {
			return TipoTrasmissione.DPCM26032008;
		}
		return TipoTrasmissione.DM02112011;
	}
}
