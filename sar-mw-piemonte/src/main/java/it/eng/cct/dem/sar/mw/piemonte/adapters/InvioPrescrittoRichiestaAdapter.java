package it.eng.cct.dem.sar.mw.piemonte.adapters;

import it.eng.cct.dem.sar.generic.wsclient.commons.utils.ConfigKeys;
import it.eng.cct.dem.sar.generic.wsclient.commons.utils.ModuleConfig;
import it.eng.cct.dem.sar.generic.wsclient.params.IServiceInput;
import it.eng.cct.dem.sar.generic.wsclient.params.impl.Action;
import it.eng.cct.dem.sar.generic.wsclient.params.impl.Login;
import it.eng.cct.dem.sar.generic.wsclient.params.impl.ServiceInput;
import it.eng.cct.dem.sar.mw.adapters.ServiceInputAdapter;
import it.eng.cct.dem.sar.mw.exceptions.MwReqAdapterException;
import it.eng.cct.dem.sar.mw.piemonte.constants.DemInvioPrescrittoConstants;
import it.eng.cct.dem.sar.mw.piemonte.constants.EPrescriptionConstants;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TDettagli;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TDettaglio;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TEPrescription;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TMedico;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TPaziente;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TRicetta;
import it.eng.cct.dem.sar.mw.utils.CodiceFiscale;
import it.eng.cct.dem.sar.mw.utils.exceptions.CodiceFiscaleException;
import it.finanze.sanita.dem.xsd.invioprescrittorichiesta.InvioPrescrittoRichiestaDocument;
import it.finanze.sanita.dem.xsd.invioprescrittorichiesta.InvioPrescrittoRichiestaDocument.InvioPrescrittoRichiesta;
import it.finanze.sanita.dem.xsd.tipodati.DettaglioPrescrizioneType;
import it.finanze.sanita.dem.xsd.tipodati.ElencoDettagliPrescrizioniType;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * 
 * 
 */
public class InvioPrescrittoRichiestaAdapter extends SistemaTiesseAdapter implements ServiceInputAdapter {

	private TEPrescription request;
	private static final Logger logger = LoggerFactory.getLogger(InvioPrescrittoRichiestaAdapter.class);

	public InvioPrescrittoRichiestaAdapter(TEPrescription request) {
		super();
		this.request = request;
	}

	public IServiceInput getServiceInput() throws MwReqAdapterException {
		IServiceInput serviceInput = new ServiceInput();
		serviceInput.setAction(new Action(ModuleConfig.getProperty(DemInvioPrescrittoConstants.CONFIG_CIL_DEMINVIOPRESCRITTO_SERVICE_NAME), ModuleConfig.getProperty(DemInvioPrescrittoConstants.CONFIG_CIL_DEMINVIOPRESCRITTO_OPERATION_NAME), ModuleConfig.getProperty(DemInvioPrescrittoConstants.CONFIG_CIL_DEMINVIOPRESCRITTO_SERVICE_ENDPOINT)));
		serviceInput.setSecurityConfigId(ModuleConfig.getProperty(DemInvioPrescrittoConstants.CONFIG_CIL_DEMINVIOPRESCRITTO_SECCONFIGID));
		serviceInput.setRequestMessage(this.toInputMessage(request));
		serviceInput.setLogin(new Login(ModuleConfig.getProperty(ConfigKeys.CONFIG_AXIS2_CLIENT_HTTP_AUTH_BASIC_USERNAME), ModuleConfig.getProperty(ConfigKeys.CONFIG_AXIS2_CLIENT_HTTP_AUTH_BASIC_PASSWORD)));
		serviceInput.setCustomProps(ModuleConfig.getPropertiesByPrefix(DemInvioPrescrittoConstants.CONFIG_CIL_DEMINVIOPRESCRITTO_ROOT + ServiceInputAdapter.CUSTOM_PROPS_KEY, true));
		return serviceInput;
	}

	private String toInputMessage(TEPrescription ePrescription) throws MwReqAdapterException {
		InvioPrescrittoRichiestaDocument invioPrescrittoRichiestaDocument = InvioPrescrittoRichiestaDocument.Factory.newInstance();
		InvioPrescrittoRichiesta invioPrescrittoRichiesta = invioPrescrittoRichiestaDocument.addNewInvioPrescrittoRichiesta();
		this.setMandatory(invioPrescrittoRichiesta, "pinCode", ModuleConfig.getProperty(DemInvioPrescrittoConstants.CONFIG_CIL_PINCODE));
		TMedico medico = ePrescription.getMedico();
		String cfMedico1 = medico.getCodiceFiscale();
		this.setMandatory(invioPrescrittoRichiesta, "cfMedico1", cfMedico1);
		String cfMedico2 = medico.getCodiceFiscaleCompilante();
		if (StringUtils.isNotEmpty(cfMedico2) && !cfMedico2.equals(cfMedico1)) {
			this.setIfNotEmpty(invioPrescrittoRichiesta, "cfMedico2", cfMedico2);
		}
		this.setIfNotEmpty(invioPrescrittoRichiesta, "codRegione", medico.getRegione());
		String codAslAoOverwrite = ModuleConfig.getProperty(EPrescriptionConstants.CONFIG_WS_EPRESCRIPTION_CODICEASLAO_OVERWRITE, "");
		if (StringUtils.isEmpty(codAslAoOverwrite)) {
			this.setIfNotEmpty(invioPrescrittoRichiesta, "codASLAo", StringUtils.right(medico.getStruttura(), 3));
		} else {
			this.setIfNotEmpty(invioPrescrittoRichiesta, "codASLAo", codAslAoOverwrite);
		}
		//this.setIfNotEmpty(invioPrescrittoRichiesta, "codStruttura", medico.getStruttura());
		String rapporto = medico.getRapporto();
		if (StringUtils.isEmpty(rapporto)) {
			this.setIfNotEmpty(invioPrescrittoRichiesta, "codSpecializzazione", "H");
		} else {
			this.setIfNotEmpty(invioPrescrittoRichiesta, "codSpecializzazione", rapporto);
		}
		TPaziente paziente = ePrescription.getPaziente();
		String codiceAss = paziente.getCodiceFiscale();
		boolean isCodiceFiscale = false;
		TRicetta ricetta = ePrescription.getRicetta();
		try {
			// Per i pazienti stranieri (ma non STP) viene valorizzato il campo SA_PAZIENTE.PZ_CFIS in modo fittizio
			isCodiceFiscale = (CodiceFiscale.verifica(codiceAss) && (StringUtils.isEmpty(ricetta.getTipoRicetta()) || !DemInvioPrescrittoConstants.TipoRic.isValidTipoRic(ricetta.getTipoRicetta())));
		} catch (CodiceFiscaleException e) {
			logger.error(e.getMessage(), e);
		}
		this.setIfNotEmpty(invioPrescrittoRichiesta, "codiceAss", codiceAss);
		this.setIfNotEmpty(invioPrescrittoRichiesta, "cognNome", paziente.getCognome() + " " + paziente.getNome());
		this.setIfNotEmpty(invioPrescrittoRichiesta, "indirizzo", paziente.getIndirizzoResidenza());
		this.setIfNotEmpty(invioPrescrittoRichiesta, "numTessSasn", paziente.getNumeroSASN());
		this.setIfNotEmpty(invioPrescrittoRichiesta, "socNavigaz", paziente.getSocietaSASN());
		String codiceEsenzione = paziente.getCodiceEsenzione();
		if (StringUtils.isEmpty(codiceEsenzione)) {
			this.setIfNotEmpty(invioPrescrittoRichiesta, "nonEsente", "1");
		}
		int dotIndex = StringUtils.indexOf(codiceEsenzione, ".");
		if (dotIndex > 0) {
			codiceEsenzione = StringUtils.left(codiceEsenzione, dotIndex);
		}
		this.setIfNotEmpty(invioPrescrittoRichiesta, "codEsenzione", codiceEsenzione);
		if (this.isEsentePerReddito(codiceEsenzione)) {
			this.setIfNotEmpty(invioPrescrittoRichiesta, "reddito", "1");
		}
		String provAssistito = paziente.getProvinciaResidenza();
		String aslAssistito = StringUtils.right(paziente.getAslResidenza(), 3);
		if (isCodiceFiscale && StringUtils.isNotEmpty(aslAssistito) && StringUtils.isNotEmpty(provAssistito)) {
			this.setIfNotEmpty(invioPrescrittoRichiesta, "provAssistito", provAssistito);
			this.setIfNotEmpty(invioPrescrittoRichiesta, "aslAssistito", aslAssistito);
		}
		if (StringUtils.isNotEmpty(paziente.getIdentificazioneEstero()) && StringUtils.isEmpty(codiceAss)) {
			logger.info("Prescrizione per soggetto assicurato da istituzioni estere.");
			this.setDateIfNotEmpty(invioPrescrittoRichiesta, "dataNascitaEstero", paziente.getDataNascita());
			this.setDateIfNotEmpty(invioPrescrittoRichiesta, "dataScadTessera", paziente.getDataScadenzaTS());
			this.setIfNotEmpty(invioPrescrittoRichiesta, "statoEstero", paziente.getStatoEstero());
			this.setIfNotEmpty(invioPrescrittoRichiesta, "istituzCompetente", paziente.getIstituzioneEstero());
			this.setIfNotEmpty(invioPrescrittoRichiesta, "numIdentPers", paziente.getIdentificazioneEstero());
			this.setIfNotEmpty(invioPrescrittoRichiesta, "numIdentTess", paziente.getTesseraEstero());
		}

		String nre = ricetta.getNRE();
		this.setMandatory(invioPrescrittoRichiesta, "nre", nre);
		this.setMandatoryDateTime(invioPrescrittoRichiesta, "dataCompilazione", ricetta.getDataCompilazione());
		String codiceDiagnosi = ricetta.getCodiceDiagnosi();
		this.setIfNotEmpty(invioPrescrittoRichiesta, "codDiagnosi", codiceDiagnosi);
		if (StringUtils.isEmpty(codiceDiagnosi)) {
			this.setIfNotEmpty(invioPrescrittoRichiesta, "descrizioneDiagnosi", ricetta.getQuesito());
		}

		/*
		Tipologia della ricetta compilata. Il campo Codice assistito è compilato o meno in funzione del Tipo Ricetta.
		Valori ammessi:
			EE = Assicurati extra-europei in temporaneo soggiorno
			UE = Assicurati europei in temporaneo soggiorno
			NA = Assistiti SASN con visita ambulatoriale
			ND = Assistiti SASN con visita domiciliare
			NE = Assistiti da istituzioni europee
			NX = Assistiti SASN extraeuropei
			ST = Stranieri in temporaneo soggiorno
		*/
		if (DemInvioPrescrittoConstants.TipoRic.isValidTipoRic(ricetta.getTipoRicetta())) {
			this.setIfNotEmpty(invioPrescrittoRichiesta, "tipoRic", ricetta.getTipoRicetta());
		}
		this.transcodeAndSetSN(invioPrescrittoRichiesta, "oscuramDati", ricetta.getOscuramento());
		this.setIfNotEmpty(invioPrescrittoRichiesta, "tipoPrescrizione", ricetta.getTipoPrescrizione());
		String interna = ricetta.getInterna();
		this.transcodeAndSetSN(invioPrescrittoRichiesta, "ricettaInterna", interna);
		String codicePoligrafico = ricetta.getCodicePoligrafico();
		String codiceRegionale = ricetta.getCodiceRegionale();
		// TODO Determinare modalità di inserimento identificativo della ricetta madre in caso di autoimpegnativa
		TRicetta ricettaOrigine = ricetta.getRicettaOrigine();
		if (StringUtils.isNotEmpty(interna) && interna.equals(WS_EPRESCRIPTION_BOOLEAN_YES) && ricettaOrigine != null) {
			String testata2 = null;
			// nre e codicePoligrafico sono lunghi 15 caratteri, il codiceRegionale (numero dell'autoimpegnativa) è lungo 16 caratteri
			if (StringUtils.isNotEmpty(ricettaOrigine.getNRE())) {
				testata2 = "R_ORIG_NRE=" + ricettaOrigine.getNRE() + "$";
			} else if (StringUtils.isNotEmpty(ricettaOrigine.getCodicePoligrafico())) {
				testata2 = "R_ORIG_POLI=" + ricettaOrigine.getCodicePoligrafico() + "$";
			} else if (StringUtils.isNotEmpty(ricettaOrigine.getCodiceRegionale())) {
				testata2 = "R_ORIG_AUTO=" + ricettaOrigine.getCodiceRegionale() + "$";
			}
			this.setIfNotEmpty(invioPrescrittoRichiesta, "testata2", testata2);
		}
		this.setIfNotEmpty(invioPrescrittoRichiesta, "tipoVisita", ricetta.getTipoVisita());
		this.setIfNotEmpty(invioPrescrittoRichiesta, "dispReg", ricetta.getDisposizioniRegionali());
		this.setIfNotEmpty(invioPrescrittoRichiesta, "indicazionePrescr", ricetta.getIndicazione());
		this.setIfNotEmpty(invioPrescrittoRichiesta, "altro", ricetta.getAltro());
		this.setIfNotEmpty(invioPrescrittoRichiesta, "classePriorita", ricetta.getPriorita());
		// this.setIfNotEmpty(invioPrescrittoRichiesta, "testata1", "");
		// this.setIfNotEmpty(invioPrescrittoRichiesta, "testata2", "");
		// TODO this.setDateTime(invioPrescrittoRichiesta, "dataCompilazione",
		// ricetta.getDataCompilazione());
		TDettagli listaDdettagli = ricetta.getDettagli();
		if (listaDdettagli != null) {
			TDettaglio[] dettagli = listaDdettagli.getDettaglioArray();
			if (dettagli != null && dettagli.length > 0) {
				ElencoDettagliPrescrizioniType elencoDettagliPrescrizioni = invioPrescrittoRichiesta.addNewElencoDettagliPrescrizioni();
				Map<String, DettaglioPrescrizioneType> resume = new HashMap<String, DettaglioPrescrizioneType>();
				for (int i = 0; i < dettagli.length; i++) {
					TDettaglio dettaglio = dettagli[i];
					String codProdPrest = dettaglio.getCodice();
					String quantita = dettaglio.getQuantita();
					String idInterno = dettaglio.getIdInterno();
					if (resume.get(idInterno) != null) {
						DettaglioPrescrizioneType summary = resume.get(idInterno);
						int previousQuantity = Integer.parseInt(summary.getQuantita());
						int total = previousQuantity + Integer.parseInt(quantita);
						summary.setQuantita(Integer.toString(total));
					} else {
						DettaglioPrescrizioneType dettaglioPrescrizione = elencoDettagliPrescrizioni.addNewDettaglioPrescrizione();
						this.setMandatory(dettaglioPrescrizione, "codProdPrest", codProdPrest);
						this.setIfNotEmpty(dettaglioPrescrizione, "descrProdPrest", dettaglio.getDescrizione());
						// codCatalogoPrescr non è ancora presente sul wsdl della CIL
						//this.setIfNotEmpty(dettaglioPrescrizione, "codCatalogoPrescr", dettaglio.getCodiceCatalogoRegionale());
						if ("P".equals(ricetta.getTipoPrescrizione())) {
							if (StringUtils.isEmpty(dettaglio.getTestoLibero())) {
								this.setProperty(dettaglioPrescrizione, "testoLibero", "");
							} else {
								this.transcodeAndSetSN(dettaglioPrescrizione, "testoLibero", dettaglio.getTestoLibero());
							}
							if (StringUtils.isNotEmpty(dettaglio.getDescrizioneTestoLibero())) {
								this.setIfNotEmpty(dettaglioPrescrizione, "descrTestoLiberoNote", dettaglio.getDescrizioneTestoLibero());
							} else if (StringUtils.isNotEmpty(dettaglio.getNote())) {
								this.setIfNotEmpty(dettaglioPrescrizione, "descrTestoLiberoNote", dettaglio.getNote());
							}
							//XXX Valori aggiunti per l'adeguamento alle nuove specifiche tecniche SAC
							this.setIfNotEmpty(dettaglioPrescrizione, "codCatalogoPrescr", dettaglio.getCodiceCatalogoRegionale());
							this.transcodeAndSetAccessType(dettaglioPrescrizione, "tipoAccesso", dettaglio.getTipoAccesso());
							//XXX Valori aggiunti per l'adeguamento alle nuove specifiche tecniche SAC 01/03/2016 DM 9 dic 2015 Lorenzin
							//this.setIfNotEmpty(dettaglioPrescrizione, "numeroNota", dettaglio.getNumeroNota());
							//this.setIfNotEmpty(dettaglioPrescrizione, "condErogabilita", dettaglio.getCondErogabilita());
							//this.setIfNotEmpty(dettaglioPrescrizione, "approprPrescrittiva", dettaglio.getApproprPrescrittiva());
							//this.setIfNotEmpty(dettaglioPrescrizione, "patologia", dettaglio.getPatologia());
							//XXX Fine valori aggiunti per l'adeguamento alle nuove specifiche tecniche SAC 01/03/2016 DM 9 dic 2015 Lorenzin
						}
						if ("F".equals(ricetta.getTipoPrescrizione())) {
							this.setIfNotEmpty(dettaglioPrescrizione, "codGruppoEquival", dettaglio.getCodiceGruppo());
							this.setIfNotEmpty(dettaglioPrescrizione, "descrGruppoEquival", dettaglio.getDescrizioneGruppo());
							this.setIfNotEmpty(dettaglioPrescrizione, "motivazNote", dettaglio.getNote());
							this.setIfNotEmpty(dettaglioPrescrizione, "codMotivazione", dettaglio.getMotivazione());
							this.setIfNotEmpty(dettaglioPrescrizione, "notaProd", dettaglio.getNotaAIFA());
							this.transcodeAndSetSN(dettaglioPrescrizione, "nonSost", dettaglio.getNonSostituibile());
						}
						this.setIfNotEmpty(dettaglioPrescrizione, "quantita", quantita);
						this.setIfNotEmpty(dettaglioPrescrizione,"numsedute", dettaglio.getNumsedute());
						// this.setIfNotEmpty(dettaglioPrescrizione,"prescrizione1", "");
						// this.setIfNotEmpty(dettaglioPrescrizione,"prescrizione2", "");
						resume.put(idInterno, dettaglioPrescrizione);
					}
				}
			}
		}
		return invioPrescrittoRichiestaDocument.xmlText();
	}

	

}
