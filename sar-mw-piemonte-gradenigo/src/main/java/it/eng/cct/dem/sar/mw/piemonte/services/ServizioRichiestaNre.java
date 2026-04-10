package it.eng.cct.dem.sar.mw.piemonte.services;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.csi.rel.relbl.interfacews.frontend.other.nre.RichiestaNreImplServiceSkeletonInterface;
import it.eng.cct.dem.sar.generic.wsclient.commons.utils.ModuleConfig;
import it.eng.cct.dem.sar.mw.exceptions.MwException;
import it.eng.cct.dem.sar.mw.piemonte.constants.SarConstants;
import it.eng.cct.dem.sar.mw.piemonte.db.model.SpLottiNre;
import it.eng.cct.dem.sar.mw.piemonte.db.service.SpLottiNreService;
import it.eng.cct.dem.sar.mw.piemonte.db.service.SpNreService;
import it.eng.cct.dem.sar.mw.piemonte.exception.SarException;
import it.eng.cct.dem.sar.mw.services.IServiceRegistry;
import it.finanze.sanita.dem.dm.wsdl.richiestanre.ElencoErroriRicetteType;
import it.finanze.sanita.dem.xsd.richiestanrericevuta.RichiestaNreRicevutaDocument;
import it.finanze.sanita.dem.xsd.richiestanrerichiesta.RichiestaNreRichiestaDocument;
import it.finanze.sanita.mirsac.nre.xsd.lottoricevuta.LottoRicevutaNREDocument;
import it.finanze.sanita.mirsac.nre.xsd.lottorichiesta.LottoRichiestaNREDocument;
import it.finanze.sanita.mirsac.nre.xsd.lottorichiesta.LottoRichiestaNREDocument.LottoRichiestaNRE;

public class ServizioRichiestaNre implements RichiestaNreImplServiceSkeletonInterface{

	private IServiceRegistry serviceRegistry;
	private SpNreService spNreService;
	private SpLottiNreService spLottiNreService;
	private static final Logger logger = LoggerFactory.getLogger(ServizioRichiestaNre.class);
	
	
	public RichiestaNreRicevutaDocument richiestaNre(RichiestaNreRichiestaDocument richiestaNreRichiesta) {
//		1. Controllo se il lotto è attivo
		String nre = "";
		RichiestaNreRicevutaDocument richiestaNreRicevutaDocument = RichiestaNreRicevutaDocument.Factory.newInstance();
		richiestaNreRicevutaDocument.addNewRichiestaNreRicevuta();
		String cfMedico = richiestaNreRichiesta.getRichiestaNreRichiesta().getCfMedico();
		try {
			nre = spNreService.getNre(cfMedico);
			richiestaNreRicevutaDocument.getRichiestaNreRicevuta().setCodEsitoRichiestaNre("OK");
			richiestaNreRicevutaDocument.getRichiestaNreRicevuta().setNre(nre);
		} catch (SarException e) {
			if(logger.isDebugEnabled())
				logger.debug("Lotto esaurito / non disponibile: si richiede al SAR un nuovo lotto.");
			ServizioRichiestaNreHelper servizioRichiestaNreHelper = new ServizioRichiestaNreHelper(this.serviceRegistry);
			LottoRichiestaNREDocument lottoRichiestaNREDocument = LottoRichiestaNREDocument.Factory.newInstance();
			String cfLr = ModuleConfig.getProperty(SarConstants.CONFIG_SAR_RAPPRESENTANTE_LEGALE);
			LottoRicevutaNREDocument richiestaNuovoLotto = null;
			try {
				if(logger.isDebugEnabled())
					logger.debug("Inizio richiesta nuovo lotto.");
				LottoRichiestaNRE addNewRichiestaNreRichiesta = lottoRichiestaNREDocument.addNewLottoRichiestaNRE();
				addNewRichiestaNreRichiesta.setCFMedico(cfLr);
				addNewRichiestaNreRichiesta.setCodRegione(ModuleConfig.getProperty(SarConstants.CONFIG_SAR_COD_REG));
				addNewRichiestaNreRichiesta.setIdentificativoLotto(ModuleConfig.getProperty(SarConstants.CONFIG_SAR_COD_ID_LOTTO));
				richiestaNuovoLotto = servizioRichiestaNreHelper.richiestaNuovoLotto(lottoRichiestaNREDocument);
				if(richiestaNuovoLotto != null){
					SpLottiNre spLottiNre = new SpLottiNre();
					spLottiNre.setCodRegioneLotto(richiestaNuovoLotto.getLottoRicevutaNRE().getCodRegione());
					spLottiNre.setCodRagLotto(richiestaNuovoLotto.getLottoRicevutaNRE().getCodRagLotto());
					spLottiNre.setCodIdLotto(richiestaNuovoLotto.getLottoRicevutaNRE().getIdentificativoLotto());
					spLottiNre.setCodLotto(richiestaNuovoLotto.getLottoRicevutaNRE().getCodLotto());
					spLottiNre.setCfRichiedente(cfLr);
					spLottiNre.setDtInsert(new Date());
					spLottiNre.setTerminato(Boolean.FALSE);
					spLottiNreService.saveLottoAndFillNres(spLottiNre);
					nre = spNreService.getNre(cfMedico);
					richiestaNreRicevutaDocument.getRichiestaNreRicevuta().setCodEsitoRichiestaNre("OK");
					richiestaNreRicevutaDocument.getRichiestaNreRicevuta().setNre(nre);
				}
			} catch (MwException e1) {
				logger.error("Eccezione in ServizioRichiestaNre.richiestaNre: " + e1.getMessage());
				richiestaNreRicevutaDocument.getRichiestaNreRicevuta().setCodEsitoRichiestaNre("KO");
				ElencoErroriRicetteType elencoErroriRicette = ElencoErroriRicetteType.Factory.newInstance();
				elencoErroriRicette.addNewErroreRicetta().setTipoErrore(e1.getMessage());
				richiestaNreRicevutaDocument.getRichiestaNreRicevuta().setElencoErroriRicette(elencoErroriRicette);
			} catch (SarException e1) {
				logger.error("Eccezione in ServizioRichiestaNre.richiestaNre: " + e1.getMessage());
				richiestaNreRicevutaDocument.getRichiestaNreRicevuta().setCodEsitoRichiestaNre("KO");
				ElencoErroriRicetteType elencoErroriRicette = ElencoErroriRicetteType.Factory.newInstance();
				elencoErroriRicette.addNewErroreRicetta().setTipoErrore(e1.getMessage());
				richiestaNreRicevutaDocument.getRichiestaNreRicevuta().setElencoErroriRicette(elencoErroriRicette);
			} 
			if(richiestaNuovoLotto!= null){
				if(logger.isDebugEnabled())
					logger.debug("Nuovo lotto ricevuto: " + richiestaNuovoLotto.getLottoRicevutaNRE().getCodLotto());
			}
		}
		return richiestaNreRicevutaDocument;
	}
	
	public void setServices(IServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

	public SpNreService getSpNreService() {
		return spNreService;
	}

	public void setSpNreService(SpNreService spNreService) {
		this.spNreService = spNreService;
	}

	public SpLottiNreService getSpLottiNreService() {
		return spLottiNreService;
	}

	public void setSpLottiNreService(SpLottiNreService spLottiNreService) {
		this.spLottiNreService = spLottiNreService;
	}

}
