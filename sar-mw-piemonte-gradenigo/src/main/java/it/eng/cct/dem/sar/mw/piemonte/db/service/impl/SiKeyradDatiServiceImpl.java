package it.eng.cct.dem.sar.mw.piemonte.db.service.impl;

import it.eng.cct.dem.sar.mw.piemonte.db.dao.SiKeyradDatiDao;
import it.eng.cct.dem.sar.mw.piemonte.db.model.IrideSiKeyradDati;
import it.eng.cct.dem.sar.mw.piemonte.db.service.SiKeyradDatiService;
import it.eng.cct.dem.sar.mw.piemonte.exception.DatabaseException;

public class SiKeyradDatiServiceImpl implements SiKeyradDatiService{

	private SiKeyradDatiDao siKeyradDatiDao;

	public IrideSiKeyradDati getByKdKey(Long kdKey) throws DatabaseException {
		return this.siKeyradDatiDao.getByKdKey(kdKey);
	}

	public SiKeyradDatiDao getSiKeyradDatiDao() {
		return siKeyradDatiDao;
	}

	public void setSiKeyradDatiDao(SiKeyradDatiDao siKeyradDatiDao) {
		this.siKeyradDatiDao = siKeyradDatiDao;
	}

	

}
