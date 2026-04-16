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
import it.eng.cct.dem.sar.mw.piemonte.constants.InvioTelematicoSanitaConstants;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TDettagli;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TDettaglio;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TEPrescription;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TEPrescriptions;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TMedico;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TPaziente;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TRicetta;
import it.eng.cct.dem.sar.mw.utils.CodiceFiscale;
import it.eng.cct.dem.sar.mw.utils.DateTimeHolder;
import it.eng.cct.dem.sar.mw.utils.exceptions.CodiceFiscaleException;
import it.finanze.sanita.dem.dpcm.wsdl.invioTelematicoSanita.service.InputBeanDocument;
import it.finanze.sanita.dem.dpcm.wsdl.invioTelematicoSanita.service.ParametriInvio;
import it.finanze.sanita.dpcm.xsd.ricettaMIR.CasellaType;
import it.finanze.sanita.dpcm.xsd.ricettaMIR.ClassePrioritaType;
import it.finanze.sanita.dpcm.xsd.ricettaMIR.DataType;
import it.finanze.sanita.dpcm.xsd.ricettaMIR.IndicazionePrescrType;
import it.finanze.sanita.dpcm.xsd.ricettaMIR.PrescrizioneDocument.Prescrizione;
import it.finanze.sanita.dpcm.xsd.ricettaMIR.RicettaCDocument.RicettaC;
import it.finanze.sanita.dpcm.xsd.ricettaMIR.RicettaIDocument.RicettaI;
import it.finanze.sanita.dpcm.xsd.ricettaMIR.RicetteMIRDocument;
import it.finanze.sanita.dpcm.xsd.ricettaMIR.RicetteMIRDocument.RicetteMIR;
import it.finanze.sanita.dpcm.xsd.ricettaMIR.TestataDocument.Testata;
import it.finanze.sanita.dpcm.xsd.ricettaMIR.TipoInvioType;
import it.finanze.sanita.dpcm.xsd.ricettaMIR.TipoPrescrizioneType;
import it.finanze.sanita.dpcm.xsd.ricettaMIR.TipoRicType;
import it.finanze.sanita.dpcm.xsd.ricettaMIR.TipoVisitaType;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.activation.DataSource;
import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * 
 * 
 */
public class InvioTelematicoSanitaRequestAdapter extends SistemaTiesseAdapter implements ServiceInputAdapter {

	private TEPrescriptions ePrescriptions;
	private String nomeFileAllegato;
	private DateTimeHolder dateHolder;
	private static final Logger logger = LoggerFactory.getLogger(InvioTelematicoSanitaRequestAdapter.class);
	
	public static final String TIPO_INVIO_RPS = "RPS";
	public static final String TIPO_INVIO_REL = "REL";

	public InvioTelematicoSanitaRequestAdapter(TEPrescriptions ePrescriptions) {
		super();
		this.ePrescriptions = ePrescriptions;
		this.dateHolder = new DateTimeHolder(new Date());
		this.nomeFileAllegato = dateHolder.getDate_yyyyMMddHHmmssSSS() + "-" + RandomStringUtils.randomAlphanumeric(4) + "_eng";
	}

	public IServiceInput getServiceInput() throws MwReqAdapterException {
		IServiceInput serviceInput = new ServiceInput();
		serviceInput.setAction(new Action(ModuleConfig.getProperty(InvioTelematicoSanitaConstants.CONFIG_CIL_INVIOTELEMATICOSANITA_SERVICE_NAME), ModuleConfig.getProperty(InvioTelematicoSanitaConstants.CONFIG_CIL_INVIOTELEMATICOSANITA_OPERATION_NAME), ModuleConfig.getProperty(InvioTelematicoSanitaConstants.CONFIG_CIL_INVIOTELEMATICOSANITA_SERVICE_ENDPOINT)));
		serviceInput.setSecurityConfigId(ModuleConfig.getProperty(InvioTelematicoSanitaConstants.CONFIG_CIL_INVIOTELEMATICOSANITA_SECCONFIGID));
		serviceInput.setRequestMessage(this.buildRequestMessage(this.ePrescriptions));
		if (!this.isOAuth2Request(this.ePrescriptions)) {
			serviceInput.setLogin(new Login(ModuleConfig.getProperty(ConfigKeys.CONFIG_AXIS2_CLIENT_HTTP_AUTH_BASIC_USERNAME), ModuleConfig.getProperty(ConfigKeys.CONFIG_AXIS2_CLIENT_HTTP_AUTH_BASIC_PASSWORD)));
		}
		serviceInput.setAttachment(this.buildAttachement(this.ePrescriptions));
		serviceInput.setUuid(StringUtils.left(this.nomeFileAllegato + ".zip", 17));
		serviceInput.setCustomProps(this.enrichCustomPropsWithTransportAuth(ModuleConfig.getPropertiesByPrefix(InvioTelematicoSanitaConstants.CONFIG_CIL_INVIOTELEMATICOSANITA_ROOT + ServiceInputAdapter.CUSTOM_PROPS_KEY, true), this.ePrescriptions));
		return serviceInput;
	}

	private String buildRequestMessage(TEPrescriptions ePrescriptions) throws MwReqAdapterException {
		InputBeanDocument inputBeanDocument = InputBeanDocument.Factory.newInstance();
		ParametriInvio inputBean = inputBeanDocument.addNewInputBean();
		this.propagateSessionToken(inputBean, ePrescriptions);
		inputBean.setNomeFileAllegato(this.nomeFileAllegato + ".zip");
		inputBean.setTelematico1("");
		inputBean.setTelematico2("");
		inputBean.setTelematico3("");
		return inputBeanDocument.xmlText();
	}

	private DataSource buildAttachement(TEPrescriptions ePrescriptions) throws MwReqAdapterException {
		RicetteMIRDocument ricetteMIRDocument = RicetteMIRDocument.Factory.newInstance();
		RicetteMIR ricetteMIR = ricetteMIRDocument.addNewRicetteMIR();
		Testata testata = ricetteMIR.addNewTestata();
		testata.setPinCode(this.getPinCodeForRequest(ModuleConfig.getProperty(DemInvioPrescrittoConstants.CONFIG_CIL_PINCODE), ePrescriptions));

		TEPrescription[] ePrescriptionArray = ePrescriptions.getEPrescriptionArray();
		RicettaI[] ricettaIArray = new RicettaI[ePrescriptionArray.length];
		RicettaC[] ricettaCArray = new RicettaC[ePrescriptionArray.length];
		String interna = ePrescriptionArray[0].getRicetta().getInterna();
		boolean isInterna = StringUtils.isNotEmpty(interna) && interna.equals(WS_EPRESCRIPTION_BOOLEAN_YES);
		String codicePoligrafico = ePrescriptionArray[0].getRicetta().getCodicePoligrafico();
		boolean isRps = StringUtils.isNotEmpty(codicePoligrafico);
		if (isRps) {
			testata.setTipoInvio(TipoInvioType.Enum.forString(TIPO_INVIO_RPS)); // Ricette prescritte su ricettario del SSN
		} else {
			testata.setTipoInvio(TipoInvioType.Enum.forString(TIPO_INVIO_REL)); // Ricette elettroniche numerate con nre
		}
		boolean isAnnullamento = StringUtils.isNotEmpty(ePrescriptionArray[0].getRicetta().getNumeroDPCM2008()); // la presenza del parametro protocolloSAC nella ePrescription richiesta mi dice che si tratta di una richiesta di annullamento
		if (!isAnnullamento) {
			for (int i = 0; i < ePrescriptionArray.length; i++) {
				TEPrescription currentEPrescription = ePrescriptionArray[i];
				ricettaIArray[i] = this.getRicettaI(currentEPrescription, isInterna, isRps);
			}
			ricetteMIR.setRicettaIArray(ricettaIArray);
		} else {
			for (int i = 0; i < ePrescriptionArray.length; i++) {
				TEPrescription currentEPrescription = ePrescriptionArray[i];
				ricettaCArray[i] = this.getRicettaC(currentEPrescription, isInterna, isRps);
			}
			ricetteMIR.setRicettaCArray(ricettaCArray);
		}
		if (logger.isDebugEnabled()) {
			String output = ricetteMIRDocument.xmlText(new XmlOptions().setSavePrettyPrint());
			logger.debug("\r\n********************************* ATTACHMENT GENERATO BEGIN *********************************\r\n" + output + "\r\n********************************* ATTACHMENT GENERATO END *********************************");
		}
		return this.compress(ricetteMIRDocument);
	}
	
	private RicettaC getRicettaC(TEPrescription ePrescription, boolean isInterna, boolean isRps) throws MwReqAdapterException {
		RicettaC currentRicettaC = RicettaC.Factory.newInstance();
		TRicetta ricetta = ePrescription.getRicetta();
		TMedico medico = ePrescription.getMedico();
		String nre = ricetta.getNRE();
		String codicePoligrafico = ricetta.getCodicePoligrafico();
		String codiceRegionale = ricetta.getCodiceRegionale();
		// TODO Verificare che l'identificativo da fornire per l'annullamento di una ricetta RPS sia il codice poligrafico invece che l'NRE.
		if (StringUtils.isNotEmpty(codicePoligrafico)) {
			currentRicettaC.setBar1(StringUtils.left(codicePoligrafico, 5));
			currentRicettaC.setBar2(StringUtils.right(codicePoligrafico, 10));
		} else if (StringUtils.isNotEmpty(nre)) {
			currentRicettaC.setBar1(StringUtils.left(nre, 5));
			currentRicettaC.setBar2(StringUtils.right(nre, 10));
		}
		this.setMandatory(currentRicettaC, "protocolloSAC", ricetta.getNumeroDPCM2008());
		String medicoCompilante = medico.getCodiceFiscaleCompilante();
		String medicoSostituito = medico.getCodiceFiscale();
		if (isRps) {
			this.setIfNotEmpty(currentRicettaC, "ricetta1", nre);
		} else {
			this.setIfNotEmpty(currentRicettaC, "ricetta1", codiceRegionale);
		}
		if (StringUtils.isNotEmpty(medicoCompilante)) {
			this.setIfNotEmpty(currentRicettaC, "ricetta2", medicoCompilante);
		} else {
			this.setIfNotEmpty(currentRicettaC, "ricetta2", medicoSostituito);
		}
		return currentRicettaC;
	}

	private RicettaI getRicettaI(TEPrescription ePrescription, boolean isInterna, boolean isRps) throws MwReqAdapterException {
		RicettaI currentRicettaI = RicettaI.Factory.newInstance();
		TRicetta ricetta = ePrescription.getRicetta();
		TMedico medico = ePrescription.getMedico();
		TPaziente paziente = ePrescription.getPaziente();
		this.setIfNotEmpty(currentRicettaI, "altro", ricetta.getAltro());
		currentRicettaI.setAslAssistito(StringUtils.right(paziente.getAslResidenza(), 3));

		String codicePoligrafico = ricetta.getCodicePoligrafico();
		String nre = ricetta.getNRE();
		String codiceRegionale = ricetta.getCodiceRegionale();
		/*
		In particolare, la CIL richiede che:
			- In caso di ricetta rossa:
				o il tag TipoInvio sia valorizzato con "RPS";
				o i tag Bar1 e Bar2 contengano il numero del Poligrafico;
				o il tag Ricetta1 contenga l'NRE;
			- In caso di autoimpegnativa:
				o il tag TipoInvio sia valorizzato con "REL";
				o i tag Bar1 e Bar2 contengano l'NRE;
				o il tag Ricetta1 contenga il numero dell'auto impegnativa;
		*/
		// Una autoimpegnativa è una ricetta interna
		if (isRps) {
			if (StringUtils.isNotEmpty(codicePoligrafico)) {
				currentRicettaI.setBar1(StringUtils.left(codicePoligrafico, 5));
				currentRicettaI.setBar2(StringUtils.right(codicePoligrafico, 10));
			}
			if (StringUtils.isNotEmpty(nre)) {
				currentRicettaI.setRicetta1(nre);
			}
		} else {
			if (isInterna) {
				if (StringUtils.isNotEmpty(nre)) {
					currentRicettaI.setBar1(StringUtils.left(nre, 5));
					currentRicettaI.setBar2(StringUtils.right(nre, 10));
				}
				if (StringUtils.isNotEmpty(codiceRegionale)) {
					// Il campo codiceRegionale è valorizzato col numero di autoimpegnativa
					currentRicettaI.setRicetta1(codiceRegionale);
				}
			} else {
				if (StringUtils.isNotEmpty(nre)) {
					currentRicettaI.setBar1(StringUtils.left(nre, 5));
					currentRicettaI.setBar2(StringUtils.right(nre, 10));
				}
				if (StringUtils.isNotEmpty(nre)) {
					// Il campo codiceRegionale è valorizzato col numero di autoimpegnativa
					currentRicettaI.setRicetta1(nre);
				}
			}
		}

		currentRicettaI.setClassePriorita(ClassePrioritaType.Enum.forString(ricetta.getPriorita()));
		String codiceEsenzione = paziente.getCodiceEsenzione();
		this.setIfNotEmpty(currentRicettaI, "codEsenzione", codiceEsenzione);
		
		String codiceFiscale = paziente.getCodiceFiscale();// CF, STP, ENI o altro
		boolean isCodiceFiscale = false;
		try {
			isCodiceFiscale = CodiceFiscale.verifica(codiceFiscale);
		} catch (CodiceFiscaleException e) {
			throw new MwReqAdapterException(e.getMessage(), e);
		}
		this.setIfNotEmpty(currentRicettaI, "codiceAss", codiceFiscale);
		this.setIfNotEmpty(currentRicettaI, "codiceDiagnosi", ricetta.getCodiceDiagnosi());
		String dataCompilazioneRicetta = ricetta.getDataCompilazione();
		DataType dtCompilazione = DataType.Factory.newInstance();
		try {
			dtCompilazione.setStringValue(DateTimeHolder.formatDate(dataCompilazioneRicetta, WS_TIESSE_DATE_PATTERN));
		} catch (ParseException e) {
			logger.error(e.getMessage(), e);
		}
		currentRicettaI.xsetDataCompilazione(dtCompilazione);
		String numIdentPers = paziente.getIdentificazioneEstero();
		if (StringUtils.isNotEmpty(numIdentPers)) {
			this.setMandatory(currentRicettaI, "numIdentPers", numIdentPers);
			this.setDateIfNotEmpty(currentRicettaI, "dataScadTessera", paziente.getDataScadenzaTS());
			this.setMandatory(currentRicettaI, "istituzCompetente", paziente.getIstituzioneEstero());
			this.setMandatory(currentRicettaI, "numIdentTess", paziente.getTesseraEstero());
			this.setMandatory(currentRicettaI, "statoEstero", paziente.getStatoEstero());
			this.setDateIfNotEmpty(currentRicettaI, "dataNascitaEstero", paziente.getDataNascita());
		}
		String descrizioneDiagnosi = ricetta.getQuesito();
		String codiceDiagnosi = ricetta.getCodiceDiagnosi();
		if (StringUtils.isEmpty(codiceDiagnosi) && StringUtils.isNotEmpty(descrizioneDiagnosi)) {
			currentRicettaI.setDescrizioneDiagnosi(descrizioneDiagnosi);
		}
		if (StringUtils.isNotEmpty(codiceDiagnosi)) {
			currentRicettaI.setCodiceDiagnosi(codiceDiagnosi);
		}
		this.setIfNotEmpty(currentRicettaI, "dispReg", ricetta.getDisposizioniRegionali());
		String indicazionePrescr = ricetta.getIndicazione();
		if (StringUtils.isNotEmpty(indicazionePrescr)) {
			currentRicettaI.setIndicazionePrescr(IndicazionePrescrType.Enum.forString(indicazionePrescr));
		}
		if (StringUtils.isEmpty(codiceEsenzione)) {
			currentRicettaI.setNonEsente(CasellaType.Enum.forString("1"));
		}
		//currentRicettaI.setNoteInvio(String); // Campo destinato ad informazioni aggiuntive
		this.setIfNotEmpty(currentRicettaI, "provAssistito", paziente.getProvinciaResidenza());
		String reddito = paziente.getReddito();
		if (StringUtils.isNotEmpty(reddito)) {
			currentRicettaI.setReddito(CasellaType.Enum.forString(reddito));
		}
		// currentRicettaI.setRicetta1(String);
		String medicoCompilante = medico.getCodiceFiscaleCompilante();
		String medicoSostituito = medico.getCodiceFiscale();
		if (StringUtils.isNotEmpty(medicoCompilante)) {
			this.setIfNotEmpty(currentRicettaI, "ricetta2", medicoCompilante);
		} else {
			this.setIfNotEmpty(currentRicettaI, "ricetta2", medicoSostituito);
		}
		// currentRicettaI.setRicetta2(String);
		String tipoPrescrizione = ricetta.getTipoPrescrizione();
		if (StringUtils.isNotEmpty(tipoPrescrizione)) {
			currentRicettaI.setTipoPrescrizione(TipoPrescrizioneType.Enum.forString(tipoPrescrizione));
		}
		String tipoRic = ricetta.getTipoRicetta();
		if (!isCodiceFiscale && StringUtils.isNotEmpty(tipoRic)) {
			currentRicettaI.setTipoRic(TipoRicType.Enum.forString(tipoRic));
		}
		String tipoVisita = ricetta.getTipoVisita();
		if (StringUtils.isNotEmpty(tipoVisita)) {
			currentRicettaI.setTipoVisita(TipoVisitaType.Enum.forString(tipoVisita));
		}
		this.setIfNotEmpty(currentRicettaI, "totPezzi", ricetta.getQuantita());
		TDettagli dettagliRicetta = ricetta.getDettagli();
		TDettaglio[] dettaglioArray = dettagliRicetta.getDettaglioArray();
		int numPrescrizioni = dettagliRicetta.getDettaglioArray().length;
		Prescrizione[] prescrizioneArray = new Prescrizione[numPrescrizioni];
		for (int j = 0; j < numPrescrizioni; j++) {
			TDettaglio currentDettaglio = dettaglioArray[j];
			Prescrizione currentPrescrizione = Prescrizione.Factory.newInstance();
			this.setIfNotEmpty(currentPrescrizione, "codProdPrest", currentDettaglio.getCodice());
			this.setIfNotEmpty(currentPrescrizione, "descrProdPrest", currentDettaglio.getDescrizione());
			this.setIfNotEmpty(currentPrescrizione, "notaProd", currentDettaglio.getNotaAIFA());
			//this.setIfNotEmpty(currentPrescrizione, "prescrizione1", value);
			//this.setIfNotEmpty(currentPrescrizione, "prescrizione2", value);
			this.setIfNotEmpty(currentPrescrizione, "quantita", currentDettaglio.getQuantita());
			this.setIfNotEmpty(currentPrescrizione, "numsedute", currentDettaglio.getNumsedute());
			prescrizioneArray[j] = currentPrescrizione;
		}
		currentRicettaI.setPrescrizioneArray(prescrizioneArray);
		return currentRicettaI;
	}

	private ByteArrayDataSource compress(XmlObject xmlObject) throws MwReqAdapterException {
		if (xmlObject == null) {
			return null;
		}
		InputStream xmlInputStream = xmlObject.newInputStream();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BufferedOutputStream bos = new BufferedOutputStream(baos);
		ZipOutputStream zos = new ZipOutputStream(bos);
		try {
			zos.putNextEntry(new ZipEntry(this.nomeFileAllegato + ".xml"));
			IOUtils.copy(xmlInputStream, zos);
			zos.closeEntry();
			zos.close();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw new MwReqAdapterException(e.getMessage(), e);
		}
		byte[] byteArray = baos.toByteArray();
		String savePath = ModuleConfig.getProperty(InvioTelematicoSanitaConstants.CONFIG_CIL_INVIOTELEMATICOSANITA_ZIP_SAVE_PATH, "");
		if (StringUtils.isNotEmpty(savePath)) {
			File file = new File(savePath + File.separatorChar + this.nomeFileAllegato + ".zip");
			try {
				file.createNewFile();
				FileUtils.writeByteArrayToFile(file, byteArray);
			} catch (IOException e) {
				logger.error("Can't write " + file.getPath());
			}
		}
		return new ByteArrayDataSource(byteArray, "application/zip");
	}

}
