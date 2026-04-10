package it.eng.cct.dem.sar.mw.piemonte.db.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.cct.dem.sar.mw.piemonte.db.dao.SpTransactionDao;
import it.eng.cct.dem.sar.mw.piemonte.db.model.SpTransaction;
import it.eng.cct.dem.sar.mw.piemonte.db.service.SpTransactionService;
import it.eng.cct.dem.sar.mw.piemonte.exception.DatabaseException;


public class SpTransactionServiceImpl implements SpTransactionService {

	private SpTransactionDao spTransactionDao;
	private static final Logger logger = LoggerFactory.getLogger(SpTransactionServiceImpl.class);

	public void save(SpTransaction spTransaction) throws DatabaseException {
		spTransactionDao.save(spTransaction);
	}

	public SpTransactionDao getSpTransactionDao() {
		return spTransactionDao;
	}

	public void setSpTransactionDao(SpTransactionDao spTransactionDao) {
		this.spTransactionDao = spTransactionDao;
	}

}
