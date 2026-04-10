package it.eng.cct.dem.sar.mw.piemonte.services;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.cct.dem.sar.mw.exceptions.MwException;
import it.eng.cct.dem.sar.mw.exceptions.MwReqAdapterException;
import it.eng.cct.dem.sar.mw.piemonte.adapters.ServizioAnnullamentoResponseAdapter;
import it.eng.cct.dem.sar.mw.piemonte.db.service.trace.ServiceMonitoringHelper;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TEPrescription;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TErrore;
import it.eng.cct.dem.sar.mw.services.IServiceRegistry;
import it.eng.cct.dem.sar.piemonte.mw.schemas.SarMiddleware.ServiceFault;
import it.finanze.sanita.dem.xsd.annullaprescrittoricevuta.AnnullaPrescrittoRicevutaDocument;
import it.finanze.sanita.dem.xsd.annullaprescrittoricevuta.AnnullaPrescrittoRicevutaDocument.AnnullaPrescrittoRicevuta;
import it.finanze.sanita.dem.xsd.tipodati.ComunicazioneType;
import it.finanze.sanita.dem.xsd.tipodati.ElencoComunicazioniType;
import it.finanze.sanita.dem.xsd.tipodati.ElencoErroriRicetteType;

public class ServizioAnnullamentoExecutor extends ServizioEPrescription implements EPrescriptionServiceExecutor {

	private static final Logger logger = LoggerFactory.getLogger(ServizioAnnullamentoExecutor.class);

	private IServiceRegistry serviceRegistry;

	private DemAnnullaPrescrittoHelper demAnnullaPrescrittoHelper;
	
	private ServiceMonitoringHelper serviceMonitoringHelper;

	public ServizioAnnullamentoExecutor(IServiceRegistry serviceRegistry, ServiceMonitoringHelper serviceMonitoringHelper) {
		super();
		this.serviceRegistry = serviceRegistry;
		this.serviceMonitoringHelper = serviceMonitoringHelper;
	}

	public TEPrescription invoke(TEPrescription request) throws ServiceFault {
		this.setMappedDiagnosticContext(request);
		logger.info("Serving ServizioTrasmissione");
		DemAnnullaPrescrittoHelper demAnnullaPrescrittoHelper = this.getDemAnnullaPrescrittoHelper();
		List<TErrore> erroriAndComunicazioni;
		AnnullaPrescrittoRicevutaDocument annullaPrescrittoRicevutaDocument = null;
		try {
			annullaPrescrittoRicevutaDocument = demAnnullaPrescrittoHelper.annullaPrescritto(request);
		} catch (MwException e) {
			if (e.isAxisFault()) {
				return ServizioEPrescription.buildErrorMessage(request, e, null);
			}
			throw newServiceFault(request, e);
		}
		String codEsitoAnnullamento = this.getCodEsitoAnnullamento(annullaPrescrittoRicevutaDocument);
		erroriAndComunicazioni = this.getErroriAndComunicazioni(annullaPrescrittoRicevutaDocument);
		if (!codEsitoAnnullamento.equals("0000")) {
			return ServizioEPrescription.buildErrorMessage(request, null, erroriAndComunicazioni);
		}
		ServizioAnnullamentoResponseAdapter responseAdapter = new ServizioAnnullamentoResponseAdapter(request, annullaPrescrittoRicevutaDocument);
		TEPrescription response = null;
		try {
			response = responseAdapter.getEPrescriptionDocument();
		} catch (MwReqAdapterException e) {
			throw newServiceFault(request, e);
		}
		return response;
	}

	public void setServices(IServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

	private DemAnnullaPrescrittoHelper getDemAnnullaPrescrittoHelper() {
		if (this.demAnnullaPrescrittoHelper == null) {
			this.demAnnullaPrescrittoHelper = new DemAnnullaPrescrittoHelper(serviceRegistry, this.serviceMonitoringHelper);
		}
		return this.demAnnullaPrescrittoHelper;
	}

	private String getCodEsitoAnnullamento(AnnullaPrescrittoRicevutaDocument annullaPrescrittoRicevutaDocument) {
		String result = null;
		if (annullaPrescrittoRicevutaDocument != null) {
			AnnullaPrescrittoRicevuta annullaPrescrittoRicevuta = annullaPrescrittoRicevutaDocument.getAnnullaPrescrittoRicevuta();
			if (annullaPrescrittoRicevuta != null) {
				result = annullaPrescrittoRicevuta.getCodEsitoAnnullamento();
			}
		}
		return result;
	}

	private List<TErrore> getErroriAndComunicazioni(AnnullaPrescrittoRicevutaDocument annullaPrescrittoRicevutaDocument) {
		List<TErrore> results = new ArrayList<TErrore>();
		if (annullaPrescrittoRicevutaDocument != null) {
			AnnullaPrescrittoRicevuta annullaPrescrittoRicevuta = annullaPrescrittoRicevutaDocument.getAnnullaPrescrittoRicevuta();
			if (annullaPrescrittoRicevuta != null) {
				ElencoErroriRicetteType elencoErroriRicette = annullaPrescrittoRicevuta.getElencoErroriRicette();
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
				ElencoComunicazioniType elencoComunicazioni = annullaPrescrittoRicevuta.getElencoComunicazioni();
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

}
