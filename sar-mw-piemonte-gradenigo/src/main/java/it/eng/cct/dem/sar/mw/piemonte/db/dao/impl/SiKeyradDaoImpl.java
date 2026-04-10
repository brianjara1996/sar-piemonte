package it.eng.cct.dem.sar.mw.piemonte.db.dao.impl;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import it.eng.cct.dem.sar.mw.piemonte.db.dao.SiKeyradDao;
import it.eng.cct.dem.sar.mw.piemonte.db.model.IrideSiKeyrad;
import it.eng.cct.dem.sar.mw.piemonte.exception.DatabaseException;

public class SiKeyradDaoImpl extends HibernateDaoSupport implements SiKeyradDao {

	private static final Logger logger = LoggerFactory.getLogger(SiKeyradDaoImpl.class);
	
	public IrideSiKeyrad getByKrNome(String krNome) throws DatabaseException {
		IrideSiKeyrad irideSiKeyrad;
		Session session = getSession();
		Transaction tx = session.beginTransaction();
		try {
			Criteria criteria = session.createCriteria(IrideSiKeyrad.class);
			criteria.add(Restrictions.eq("krNome", krNome));
			irideSiKeyrad = (IrideSiKeyrad) criteria.uniqueResult();
			tx.commit();
		} catch (Exception e) {
			tx.rollback();
			logger.error("Eccezione in SiKeyradDaoImpl.getByKrNome: " + e.getMessage());
			throw new DatabaseException("Eccezione in SiKeyradDaoImpl.getByKrNome: " + e.getMessage());
		}finally{
			session.close();
		}
		return irideSiKeyrad;
	}


}