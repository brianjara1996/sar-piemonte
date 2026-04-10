package it.eng.cct.dem.sar.mw.piemonte.db.dao.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import it.eng.cct.dem.sar.mw.piemonte.db.dao.SpTransactionErrorDao;
import it.eng.cct.dem.sar.mw.piemonte.db.model.SpTransactionError;
import it.eng.cct.dem.sar.mw.piemonte.exception.DatabaseException;

public class SpTransactionErrorDaoImpl extends HibernateDaoSupport implements SpTransactionErrorDao {

	private static final Logger logger = LoggerFactory.getLogger(SpTransactionErrorDaoImpl.class);

	public void save(SpTransactionError entity) throws DatabaseException {
		try {
			getHibernateTemplate().save(entity);
		}catch(Exception e){
			logger.error("Eccezione in SpTransactionErrorDaoImpl.save: " + e.getMessage());
			throw new DatabaseException("Eccezione in SpTransactionErrorDaoImpl.save: " + e.getMessage());
		}
	}

	public void update(SpTransactionError entity) throws DatabaseException {
		try {
			getHibernateTemplate().update(entity);
		}catch(Exception e){
			logger.error("Eccezione in SpTransactionErrorDaoImpl.update: " + e.getMessage());
			throw new DatabaseException("Eccezione in SpTransactionErrorDaoImpl.update: " + e.getMessage());
		}
	}
}