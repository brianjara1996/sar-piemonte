package it.eng.cct.dem.sar.mw.piemonte.db.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.cct.dem.sar.mw.piemonte.db.dao.SpTransactionErrorDao;
import it.eng.cct.dem.sar.mw.piemonte.db.model.SpTransactionError;
import it.eng.cct.dem.sar.mw.piemonte.db.service.SpTransactionErrorService;
import it.eng.cct.dem.sar.mw.piemonte.exception.DatabaseException;

public class SpTransactionErrorServiceImpl implements SpTransactionErrorService{

	private SpTransactionErrorDao spTransactionErrorDao;
	private static final Logger logger = LoggerFactory.getLogger(SpTransactionErrorServiceImpl.class);
	
	
	public void save(SpTransactionError spTransactionError) throws DatabaseException {
		spTransactionErrorDao.save(spTransactionError);
	}


	public SpTransactionErrorDao getSpTransactionErrorDao() {
		return spTransactionErrorDao;
	}


	public void setSpTransactionErrorDao(SpTransactionErrorDao spTransactionErrorDao) {
		this.spTransactionErrorDao = spTransactionErrorDao;
	}

}
