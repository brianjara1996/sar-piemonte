package it.eng.cct.dem.sar.mw.piemonte.db.service.impl;

import it.eng.cct.dem.sar.mw.piemonte.db.dao.SiKeyradDao;
import it.eng.cct.dem.sar.mw.piemonte.db.model.IrideSiKeyrad;
import it.eng.cct.dem.sar.mw.piemonte.db.service.SiKeyradService;
import it.eng.cct.dem.sar.mw.piemonte.exception.DatabaseException;

public class SiKeyradServiceImpl implements SiKeyradService{

	private SiKeyradDao siKeyradDao;

	public IrideSiKeyrad getByKrNome(String krNome) throws DatabaseException {
		return this.siKeyradDao.getByKrNome(krNome);
	}

	public SiKeyradDao getSiKeyradDao() {
		return siKeyradDao;
	}

	public void setSiKeyradDao(SiKeyradDao siKeyradDao) {
		this.siKeyradDao = siKeyradDao;
	}
	

}
