package it.eng.cct.dem.sar.mw.piemonte.services;

import it.eng.cct.dem.sar.mw.exceptions.MwException;
import it.eng.cct.dem.sar.mw.exceptions.MwReqAdapterException;
import it.eng.cct.dem.sar.mw.piemonte.adapters.MessageAdapter;
import it.eng.cct.dem.sar.mw.piemonte.adapters.ServizioTrasmissioneResponseAdapter;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TEPrescription;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TErrore;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TErrori;
import it.eng.cct.dem.sar.mw.services.IServiceRegistry;
import it.eng.cct.dem.sar.piemonte.mw.schemas.SarMiddleware.ServiceFault;
import it.finanze.sanita.dem.dm.wsdl.richiestanre.ComunicazioneType;
import it.finanze.sanita.dem.dm.wsdl.richiestanre.ElencoComunicazioniType;
import it.finanze.sanita.dem.dm.wsdl.richiestanre.ElencoErroriRicetteType;
import it.finanze.sanita.dem.dm.wsdl.richiestanre.ErroreRicettaType;
import it.finanze.sanita.dem.xsd.invioprescrittoricevuta.InvioPrescrittoRicevutaDocument;
import it.finanze.sanita.dem.xsd.invioprescrittoricevuta.InvioPrescrittoRicevutaDocument.InvioPrescrittoRicevuta;
import it.finanze.sanita.dem.xsd.richiestanrericevuta.RichiestaNreRicevutaDocument;
import it.finanze.sanita.dem.xsd.richiestanrericevuta.RichiestaNreRicevutaDocument.RichiestaNreRicevuta;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServizioTrasmissioneExecutor extends ServizioEPrescription implements EPrescriptionServiceExecutor {

	private static final Logger logger = LoggerFactory.getLogger(ServizioTrasmissioneExecutor.class);

	private IServiceRegistry serviceRegistry;

	private DemInvioPrescrittoHelper demInvioPrescrittoHelper;
	private RichiestaNreHelper richiestaNreHelper;

	public ServizioTrasmissioneExecutor(IServiceRegistry serviceRegistry) {
		super();
		this.serviceRegistry = serviceRegistry;
	}

	public TEPrescription invoke(TEPrescription request) throws ServiceFault {
		this.setMappedDiagnosticContext(request);
		TEPrescription response = TEPrescription.Factory.newInstance();
		String nre = this.getNre(request, response);
		if (nre == null) {
			return response;
		}
		List<TErrore> erroriAndComunicazioni = this.getErroriAndComunicazioni(response);
		DemInvioPrescrittoHelper demInvioPrescrittoHelper = this.getDemInvioPrescrittoHelper();
		InvioPrescrittoRicevutaDocument invioPrescrittoRicevutaDocument = null;
		try {
			invioPrescrittoRicevutaDocument = demInvioPrescrittoHelper.invioPrescritto(request);
		} catch (MwException e) {
			if (e.isAxisFault()) {
				if (!e.getFaultCode().startsWith("-")) {
					return ServizioEPrescription.buildErrorMessage(request, e, erroriAndComunicazioni);
				}
			}
			// AnnullaPrescritto richiesta per annullare nre corrente, richiesta nuovo nre e restituzione FAULT
			TEPrescription annullamentoResponse = this.annullaNre(request);
			if (this.isErrorResponse(annullamentoResponse)) {
				// Se l'invio non è andato a buon fine, si verificherà l'errore 1125 - Visualizzazione non consentita - cf medico non valido, diverso da quello associato NRE
				if (!annullamentoResponse.getErrori().getErroreArray(0).getCodice().equals("1125")) {
					return annullamentoResponse;
				}
			}
			nre = this.getNre(request, response);
			if (nre != null) {
				response.setEsito("FAULT");
				TErrori errori = response.getErrori();
				if (errori == null) {
					errori = response.addNewErrori();
				}
				TErrore errore = TErrore.Factory.newInstance();
				errore.setCodice("9999");
				errore.setDescrizione("Servizio di trasmissione non disponibile. Procedere invio DPCM.");
				TErrore[] erroreArray = errori.getErroreArray();
				if (erroreArray == null || erroreArray.length == 0) {
					errori.setErroreArray(new TErrore[] {errore});
				} else {
					errori.setErroreArray(0, errore);
				}
				response.getRicetta().setCodiceRegionale(nre);
			}
			return response;
		}
		String codEsitoInserimento = this.getCodEsitoInserimento(invioPrescrittoRicevutaDocument);
		erroriAndComunicazioni = this.getErroriAndComunicazioni(invioPrescrittoRicevutaDocument);
		if (!("0000".equals(codEsitoInserimento) || "0001".equals(codEsitoInserimento))) {
			return ServizioEPrescription.buildErrorMessage(request, null, erroriAndComunicazioni);
		}
		ServizioTrasmissioneResponseAdapter responseAdapter = new ServizioTrasmissioneResponseAdapter(request, invioPrescrittoRicevutaDocument);
		try {
			response = responseAdapter.getEPrescriptionDocument();
		} catch (MwReqAdapterException e) {
			throw newServiceFault(request, e);
		}
		return response;
	}

	private String getCodEsitoRichiestaNre(RichiestaNreRicevutaDocument richiestaNreRicevutaDocument) {
		String result = null;
		if (richiestaNreRicevutaDocument != null) {
			RichiestaNreRicevuta richiestaNreRicevuta = richiestaNreRicevutaDocument.getRichiestaNreRicevuta();
			if (richiestaNreRicevuta != null) {
				result = richiestaNreRicevuta.getCodEsitoRichiestaNre();
			}
		}
		return result;
	}

	private String getCodEsitoInserimento(InvioPrescrittoRicevutaDocument invioPrescrittoRicevutaDocument) {
		String result = null;
		if (invioPrescrittoRicevutaDocument != null) {
			InvioPrescrittoRicevuta richiestaNreRicevuta = invioPrescrittoRicevutaDocument.getInvioPrescrittoRicevuta();
			if (richiestaNreRicevuta != null) {
				result = richiestaNreRicevuta.getCodEsitoInserimento();
			}
		}
		return result;
	}

	private List<TErrore> getErroriAndComunicazioni(InvioPrescrittoRicevutaDocument invioPrescrittoRicevutaDocument) {
		List<TErrore> results = new ArrayList<TErrore>();
		if (invioPrescrittoRicevutaDocument != null) {
			InvioPrescrittoRicevuta invioPrescrittoRicevuta = invioPrescrittoRicevutaDocument.getInvioPrescrittoRicevuta();
			if (invioPrescrittoRicevuta != null) {
				return MessageAdapter.getErroriAndComunicazioni(invioPrescrittoRicevuta.getElencoComunicazioni(), invioPrescrittoRicevuta.getElencoErroriRicette());
			}
		}
		return results;
	}

	private List<TErrore> getErroriAndComunicazioni(RichiestaNreRicevutaDocument richiestaNreRicevutaDocument) {
		List<TErrore> results = new ArrayList<TErrore>();
		if (richiestaNreRicevutaDocument != null) {
			RichiestaNreRicevuta richiestaNreRicevuta = richiestaNreRicevutaDocument.getRichiestaNreRicevuta();
			if (richiestaNreRicevuta != null) {
				ElencoErroriRicetteType elencoErroriRicette = richiestaNreRicevuta.getElencoErroriRicette();
				if (elencoErroriRicette != null) {
					ErroreRicettaType[] errori = elencoErroriRicette.getErroreRicettaArray();
					if (errori != null && errori.length > 0) {
						for (ErroreRicettaType errore : errori) {
							TErrore result = TErrore.Factory.newInstance();
							result.setCodice(errore.getCodEsito());
							result.setDescrizione(errore.getEsito());
							results.add(result);
						}
					}
				}
				ElencoComunicazioniType elencoComunicazioni = richiestaNreRicevuta.getElencoComunicazioni();
				if (elencoComunicazioni != null) {
					ComunicazioneType[] comunicazioni = elencoComunicazioni.getComunicazioneArray();
					if (comunicazioni != null && comunicazioni.length > 0) {
						for (ComunicazioneType comunicazione : comunicazioni) {
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

	private DemInvioPrescrittoHelper getDemInvioPrescrittoHelper() {
		if (this.demInvioPrescrittoHelper == null) {
			this.demInvioPrescrittoHelper = new DemInvioPrescrittoHelper(serviceRegistry);
		}
		return this.demInvioPrescrittoHelper;
	}

	private RichiestaNreHelper getRichiestaNreHelper() {
		if (this.richiestaNreHelper == null) {
			this.richiestaNreHelper = new RichiestaNreHelper(serviceRegistry);
		}
		return this.richiestaNreHelper;
	}

	private String getNre(TEPrescription request, TEPrescription response) throws ServiceFault {
		RichiestaNreHelper richiestaNreHelper = this.getRichiestaNreHelper();
		RichiestaNreRicevutaDocument richiestaNreRicevutaDocument = null;
		try {
			richiestaNreRicevutaDocument = richiestaNreHelper.richiestaNre(request);
		} catch (MwException e) {
			if (e.isAxisFault()) {
				TEPrescription errorResponse = ServizioEPrescription.buildErrorMessage(request, e, null);
				response.set(errorResponse);
				return null;
			}
			throw newServiceFault(request, e);
		}
		List<TErrore> erroriAndComunicazioni = this.getErroriAndComunicazioni(richiestaNreRicevutaDocument);
		erroriAndComunicazioni.addAll(this.getErroriAndComunicazioni(response));
		String codEsitoRichiestaNre = this.getCodEsitoRichiestaNre(richiestaNreRicevutaDocument);
		if (!codEsitoRichiestaNre.equals("0000") && !erroriAndComunicazioni.isEmpty()) {
			TEPrescription errorResponse = ServizioEPrescription.buildErrorMessage(request, null, erroriAndComunicazioni);
			response.set(errorResponse);
			return null;
		}
		String nre = richiestaNreRicevutaDocument.getRichiestaNreRicevuta().getNre();
		logger.debug("Ottenuto NRE " + nre + " per request " + request.getId());
		request.getRicetta().setNRE(nre);
		if (response == null) {
			response = TEPrescription.Factory.newInstance();
		}
		if (response.getRicetta() == null) {
			response.addNewRicetta();
		}
		response.getRicetta().setNRE(nre);
		response.addNewErrori().setErroreArray((TErrore[]) erroriAndComunicazioni.toArray(new TErrore[erroriAndComunicazioni.size()]));
		response.setEsito("0000");
		response.setId(request.getId());
		return nre;
	}

	private boolean isErrorResponse(TEPrescription response) {
		if (response == null) {
			return false;
		}
		String esito = response.getEsito();
		if (StringUtils.isNotEmpty(esito) && !esito.equals("0000")) {
			return true;
		}
		return false;
	}

	private List<TErrore> getErroriAndComunicazioni(TEPrescription message) {
		List<TErrore> result = new ArrayList<TErrore>();
		if (message == null) {
			return result;
		}
		TErrori errori = message.getErrori();
		if (errori == null) {
			return result;
		}
		TErrore[] erroreArray = errori.getErroreArray();
		if (erroreArray == null || erroreArray.length == 0) {
			return result;
		}
		for (TErrore current : erroreArray) {
			result.add(current);
		}
		return result;
	}

	private TEPrescription annullaNre(TEPrescription request) throws ServiceFault {
		ServizioAnnullamentoExecutor executor = new ServizioAnnullamentoExecutor(this.serviceRegistry);
		TEPrescription annullamentoResponse = executor.invoke(request);
		return annullamentoResponse;
	}
}
