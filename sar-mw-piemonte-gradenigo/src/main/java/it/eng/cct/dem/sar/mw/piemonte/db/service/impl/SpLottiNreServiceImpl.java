package it.eng.cct.dem.sar.mw.piemonte.db.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.cct.dem.sar.mw.piemonte.db.dao.SpLottiNreDao;
import it.eng.cct.dem.sar.mw.piemonte.db.model.SpLottiNre;
import it.eng.cct.dem.sar.mw.piemonte.db.service.SpLottiNreService;
import it.eng.cct.dem.sar.mw.piemonte.exception.DatabaseException;

public class SpLottiNreServiceImpl implements SpLottiNreService{

	private SpLottiNreDao spLottiNreDao;
	
	private static final Logger logger = LoggerFactory.getLogger(SpLottiNreServiceImpl.class);
	
	public Long getActualNre() throws DatabaseException{
		return this.spLottiNreDao.getNreSeqCurVal();
	}
	
	public Long getNextNre() throws DatabaseException{
		return this.spLottiNreDao.getNreSeqNextVal();
	}
	
	public SpLottiNre getLottoAttivo() throws DatabaseException{
		return this.spLottiNreDao.getLottoAttivo();
	}
	
	public void saveLotto(SpLottiNre spLottiNre) throws DatabaseException{
		this.spLottiNreDao.save(spLottiNre);
	}
	
	public boolean saveLottoAndFillNres(SpLottiNre spLottiNre) throws DatabaseException{
		return this.spLottiNreDao.saveLottoAndFillNres(spLottiNre);
	}

	public SpLottiNreDao getSpLottiNreDao() {
		return spLottiNreDao;
	}

	public void setSpLottiNreDao(SpLottiNreDao spLottiNreDao) {
		this.spLottiNreDao = spLottiNreDao;
	}

}
