package it.eng.cct.dem.sar.mw.piemonte.services;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.cct.dem.sar.mw.exceptions.MwException;
import it.eng.cct.dem.sar.mw.exceptions.MwReqAdapterException;
import it.eng.cct.dem.sar.mw.piemonte.adapters.ServizioRecuperoResponseAdapter;
import it.eng.cct.dem.sar.mw.piemonte.db.service.trace.ServiceMonitoringHelper;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TEPrescription;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TErrore;
import it.eng.cct.dem.sar.mw.services.IServiceRegistry;
import it.eng.cct.dem.sar.piemonte.mw.schemas.SarMiddleware.ServiceFault;
import it.finanze.sanita.dem.xsd.tipodati.ComunicazioneType;
import it.finanze.sanita.dem.xsd.tipodati.ElencoComunicazioniType;
import it.finanze.sanita.dem.xsd.tipodati.ElencoErroriRicetteType;
import it.finanze.sanita.dem.xsd.visualizzaprescrittoricevuta.VisualizzaPrescrittoRicevutaDocument;
import it.finanze.sanita.dem.xsd.visualizzaprescrittoricevuta.VisualizzaPrescrittoRicevutaDocument.VisualizzaPrescrittoRicevuta;

public class ServizioRecuperoExecutor extends ServizioEPrescription implements EPrescriptionServiceExecutor {
	private static final Logger logger = LoggerFactory.getLogger(ServizioRecuperoExecutor.class);

	private IServiceRegistry serviceRegistry;

	private DemVisualizzaPrescrittoHelper demVisualizzaPrescrittoHelper;
	
	private ServiceMonitoringHelper serviceMonitoringHelper;

	public ServizioRecuperoExecutor(IServiceRegistry serviceRegistry, ServiceMonitoringHelper serviceMonitoringHelper) {
		super();
		this.serviceRegistry = serviceRegistry;
		this.serviceMonitoringHelper = serviceMonitoringHelper;
	}

	public TEPrescription invoke(TEPrescription request) throws ServiceFault {
		this.setMappedDiagnosticContext(request);
		logger.info("Serving ServizioTrasmissione");
		DemVisualizzaPrescrittoHelper demVisualizzaPrescrittoHelper = this.getDemVisualizzaPrescrittoHelper();
		List<TErrore> erroriAndComunicazioni;
		VisualizzaPrescrittoRicevutaDocument visualizzaPrescrittoRicevutaDocument = null;
		try {
			visualizzaPrescrittoRicevutaDocument = demVisualizzaPrescrittoHelper.visualizzaPrescritto(request);
		} catch (MwException e) {
			if (e.isAxisFault()) {
				return ServizioEPrescription.buildErrorMessage(request, e, null);
			}
			throw newServiceFault(request, e);
		}
		String codEsitoVisualizzazione = this.getCodEsitoVisualizzazione(visualizzaPrescrittoRicevutaDocument);
		erroriAndComunicazioni = this.getErroriAndComunicazioni(visualizzaPrescrittoRicevutaDocument);
		if (!codEsitoVisualizzazione.equals("0000")) {
			return ServizioEPrescription.buildErrorMessage(request, null, erroriAndComunicazioni);
		}
		ServizioRecuperoResponseAdapter responseAdapter = new ServizioRecuperoResponseAdapter(request, visualizzaPrescrittoRicevutaDocument);
		TEPrescription response = null;
		try {
			response = responseAdapter.getEPrescriptionDocument();
		} catch (MwReqAdapterException e) {
			throw newServiceFault(request, e);
		}
		return response;
	}

	private String getCodEsitoVisualizzazione(VisualizzaPrescrittoRicevutaDocument visualizzaPrescrittoRicevutaDocument) {
		String result = null;
		if (visualizzaPrescrittoRicevutaDocument != null) {
			VisualizzaPrescrittoRicevuta visualizzaPrescrittoRicevuta = visualizzaPrescrittoRicevutaDocument.getVisualizzaPrescrittoRicevuta();
			if (visualizzaPrescrittoRicevuta != null) {
				result = visualizzaPrescrittoRicevuta.getCodEsitoVisualizzazione();
			}
		}
		return result;
	}

	private List<TErrore> getErroriAndComunicazioni(VisualizzaPrescrittoRicevutaDocument visualizzaPrescrittoRicevutaDocument) {
		List<TErrore> results = new ArrayList<TErrore>();
		if (visualizzaPrescrittoRicevutaDocument != null) {
			VisualizzaPrescrittoRicevuta visualizzaPrescrittoRicevuta = visualizzaPrescrittoRicevutaDocument.getVisualizzaPrescrittoRicevuta();
			if (visualizzaPrescrittoRicevuta != null) {
				ElencoErroriRicetteType elencoErroriRicette = visualizzaPrescrittoRicevuta.getElencoErroriRicette();
				if (elencoErroriRicette != null) {
					it.finanze.sanita.dem.xsd.tipodati.ErroreRicettaType[] errori = elencoErroriRicette.getErroreRicettaArray();
					if (errori != null && errori.length > 0) {
						for (it.finanze.sanita.dem.xsd.tipodati.ErroreRicettaType errore : errori) {
							TErrore result = TErrore.Factory.newInstance();
							result.setCodice(errore.getCodEsito());
							result.setDescrizione(errore.getEsito());
							results.add(result);
						}
					}
				}
				ElencoComunicazioniType elencoComunicazioni = visualizzaPrescrittoRicevuta.getElencoComunicazioni();
				if (elencoComunicazioni != null) {
					ComunicazioneType[] comunicazioni = elencoComunicazioni.getComunicazioneArray();
					if (comunicazioni != null && comunicazioni.length > 0) {
						for (it.finanze.sanita.dem.xsd.tipodati.ComunicazioneType comunicazione : comunicazioni) {
							TErrore result = TErrore.Factory.newInstance();
							result.setCodice(comunicazione.getCodice());
							result.setDescrizione(comunicazione.getMessaggio());
							results.add(result);
						}
					}
				}
			}
		}
		return results;
	}

	public void setServices(IServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

	private DemVisualizzaPrescrittoHelper getDemVisualizzaPrescrittoHelper() {
		if (this.demVisualizzaPrescrittoHelper == null) {
			this.demVisualizzaPrescrittoHelper = new DemVisualizzaPrescrittoHelper(serviceRegistry, this.serviceMonitoringHelper);
		}
		return this.demVisualizzaPrescrittoHelper;
	}

}
