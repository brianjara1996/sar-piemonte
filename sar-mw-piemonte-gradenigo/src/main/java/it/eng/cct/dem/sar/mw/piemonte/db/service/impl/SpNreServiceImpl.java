package it.eng.cct.dem.sar.mw.piemonte.db.service.impl;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.cct.dem.sar.mw.piemonte.db.dao.SpLottiNreDao;
import it.eng.cct.dem.sar.mw.piemonte.db.dao.SpNreDao;
import it.eng.cct.dem.sar.mw.piemonte.db.model.SpLottiNre;
import it.eng.cct.dem.sar.mw.piemonte.db.model.SpNre;
import it.eng.cct.dem.sar.mw.piemonte.db.service.SpNreService;
import it.eng.cct.dem.sar.mw.piemonte.exception.DatabaseException;
import it.eng.cct.dem.sar.mw.piemonte.exception.NoLottoException;
import it.eng.cct.dem.sar.mw.piemonte.exception.NoMoreNreInLottoException;

public class SpNreServiceImpl implements SpNreService{
	
	private SpNreDao spNreDao;
	private SpLottiNreDao spLottiNreDao;
	private static final Logger logger = LoggerFactory.getLogger(SpNreServiceImpl.class);

	
	public synchronized String getNre(String cfMedico) throws NoLottoException, NoMoreNreInLottoException, DatabaseException{
		if(logger.isDebugEnabled())
			logger.debug("Inizio ricerca NRE per il medico " + cfMedico);
		String nre = "";
		SpLottiNre lottoNreDb = spLottiNreDao.getLottoAttivo();
		if(lottoNreDb == null){
			logger.error("Attenzione! Nessun lotto attivo disponibile!");
			throw new NoLottoException("Attenzione! Nessun lotto attivo disponibile!");
		}
		String lotto = lottoNreDb.getCodRegioneLotto() + lottoNreDb.getCodRagLotto() + lottoNreDb.getCodIdLotto() + lottoNreDb.getCodLotto();
		if(logger.isDebugEnabled())
			logger.debug("Recuperato Lotto " + lotto);
		SpNre nreDb = spNreDao.getNextNre(lottoNreDb);
		if(nreDb == null){
			logger.error("Attenzione! NRE del lotto "+ lotto +" terminati!");
			throw new NoMoreNreInLottoException("Attenzione! NRE del lotto "+ lotto +" terminati!");
		}
		nreDb.setCfMedico(cfMedico);
		nreDb.setDtAssegnazione(new Date());
		spNreDao.update(nreDb);
		if(nreDb.getLast()){
			lottoNreDb.setTerminato(Boolean.TRUE);
			lottoNreDb.setDtFine(new Date());
			spLottiNreDao.update(lottoNreDb);
		}
		nre = lotto + String.format("%05d", nreDb.getId().getNreProgressivo());
		return nre;
	}
	
	public boolean fillLottoNre(SpLottiNre spLottiNre) throws DatabaseException{
		return spNreDao.fillNres(spLottiNre.getId());
	}

	public SpNreDao getSpNreDao() {
		return spNreDao;
	}

	public void setSpNreDao(SpNreDao spNreDao) {
		this.spNreDao = spNreDao;
	}

	public SpLottiNreDao getSpLottiNreDao() {
		return spLottiNreDao;
	}

	public void setSpLottiNreDao(SpLottiNreDao spLottiNreDao) {
		this.spLottiNreDao = spLottiNreDao;
	}

}
