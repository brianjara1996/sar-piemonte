package it.eng.cct.dem.sar.mw.piemonte.db.dao.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import it.eng.cct.dem.sar.mw.piemonte.db.dao.SpTransactionDao;
import it.eng.cct.dem.sar.mw.piemonte.db.model.SpTransaction;
import it.eng.cct.dem.sar.mw.piemonte.exception.DatabaseException;

public class SpTransactionDaoImpl extends HibernateDaoSupport implements SpTransactionDao {

	private static final Logger logger = LoggerFactory.getLogger(SpTransactionDaoImpl.class);

	public void save(SpTransaction entity) throws DatabaseException {
		try {
			getHibernateTemplate().save(entity);
		}catch(Exception e){
			logger.error("Eccezione in SpTransactionDaoImpl.save: " + e.getMessage());
			throw new DatabaseException("Eccezione in SpTransactionDaoImpl.save: " + e.getMessage());
		}
	}

	public void update(SpTransaction entity) throws DatabaseException {
		try {
			getHibernateTemplate().update(entity);
		}catch(Exception e){
			logger.error("Eccezione in SpTransactionDaoImpl.update: " + e.getMessage());
			throw new DatabaseException("Eccezione in SpTransactionDaoImpl.update: " + e.getMessage());
		}
	}
	
}