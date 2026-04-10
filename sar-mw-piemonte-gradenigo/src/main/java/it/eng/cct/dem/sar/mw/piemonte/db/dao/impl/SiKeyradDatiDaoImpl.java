package it.eng.cct.dem.sar.mw.piemonte.db.dao.impl;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import it.eng.cct.dem.sar.mw.piemonte.db.dao.SiKeyradDatiDao;
import it.eng.cct.dem.sar.mw.piemonte.db.model.IrideSiKeyradDati;
import it.eng.cct.dem.sar.mw.piemonte.exception.DatabaseException;

public class SiKeyradDatiDaoImpl extends HibernateDaoSupport implements SiKeyradDatiDao {

	private static final Logger logger = LoggerFactory.getLogger(SiKeyradDatiDaoImpl.class);
	
	public IrideSiKeyradDati getByKdKey(Long kdKey) throws DatabaseException {
		IrideSiKeyradDati irideSiKeyradDati;
		Session session = getSession();
		Transaction tx = session.beginTransaction();
		try {
			Criteria criteria = session.createCriteria(IrideSiKeyradDati.class);
			criteria.add(Restrictions.eq("kdKey", kdKey));
			irideSiKeyradDati = (IrideSiKeyradDati) criteria.uniqueResult();
			tx.commit();
		} catch (Exception e) {
			tx.rollback();
			logger.error("Eccezione in SiKeyradDatiDaoImpl.getByKdKey: " + e.getMessage());
			throw new DatabaseException("Eccezione in SiKeyradDatiDaoImpl.getByKdKey: " + e.getMessage());
		}
		finally{
			session.close();
		}
		return irideSiKeyradDati;
	}


}