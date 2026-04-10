package it.eng.cct.dem.sar.mw.piemonte.db.dao.impl;

import java.math.BigInteger;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import it.eng.cct.dem.sar.generic.wsclient.commons.utils.ModuleConfig;
import it.eng.cct.dem.sar.mw.piemonte.constants.MwConstants;
import it.eng.cct.dem.sar.mw.piemonte.db.dao.SpLottiNreDao;
import it.eng.cct.dem.sar.mw.piemonte.db.model.SpLottiNre;
import it.eng.cct.dem.sar.mw.piemonte.exception.DatabaseException;

public class SpLottiNreDaoImpl extends HibernateDaoSupport implements SpLottiNreDao {

	private static final Logger logger = LoggerFactory.getLogger(SpLottiNreDaoImpl.class);
	
	public Long getNreSeqCurVal() throws DatabaseException{
		Long n = null;
		Session session = getSession();
		Transaction tx = session.beginTransaction();
		try {
			String q = "select SEQ_LOTTO_NRE.curval as num from dual";
			Query query = session.createSQLQuery(q).addScalar("num");
			n = ((BigInteger) query.uniqueResult()).longValue();
			tx.commit();
		} catch (Exception e) {
			tx.rollback();
			logger.error("Eccezione in SpLottiNreDaoImpl.getNreSeqCurVal: " + e.getMessage());
			throw new DatabaseException("Eccezione in SpLottiNreDaoImpl.getNreSeqCurVal: " + e.getMessage());
		}finally{
			session.close();
		}
		return n;
	}

	public Long getNreSeqNextVal() throws DatabaseException {
		Long n = null;
		Session session = getSession();
		Transaction tx = session.beginTransaction();
		try {
			String q = "select MYSEQ.nextval as num from dual";
			Query query = session.createSQLQuery(q).addScalar("num");
			n = ((BigInteger) query.uniqueResult()).longValue();
			tx.commit();
		} catch (Exception e) {
			tx.rollback();
			logger.error("Eccezione in SpLottiNreDaoImpl.getNreSeqNextVal: " + e.getMessage());
			throw new DatabaseException("Eccezione in SpLottiNreDaoImpl.getNreSeqNextVal: " + e.getMessage());
		}finally{
			session.close();
		}
		return n;
	}

	public SpLottiNre getLottoAttivo() throws DatabaseException {
		SpLottiNre lotto = null;
		Session session = getSession();
		Transaction tx = session.beginTransaction();
		try {
			Criteria criteria = session.createCriteria(SpLottiNre.class);
			criteria.add(Restrictions.eq("terminato", Boolean.FALSE));
			lotto = (SpLottiNre) criteria.uniqueResult();
			tx.commit();
		} catch (Exception e) {
			tx.rollback();
			logger.error("Eccezione in SpLottiNreDaoImpl.saveLottoAndFillNres: " + e.getMessage());
			throw new DatabaseException("Eccezione in SpLottiNreDaoImpl.saveLottoAndFillNres: " + e.getMessage());
		}finally{
			session.close();
		}
		return lotto;
	}
	
	public boolean saveLottoAndFillNres(SpLottiNre spLottiNre) throws DatabaseException{
		boolean esito = Boolean.FALSE;
		String sql ="";
		if(ModuleConfig.getProperty(MwConstants.CONFIG_DB_VENDOR).equals(MwConstants.DB_VENDOR_POSTGRES)){
			sql = "INSERT INTO sarp_mw.\"SP_NRE\" (\"ID_LOTTO\", \"NRE_PROGRESSIVO\") "
				+ "( SELECT :idLotto, x.num FROM ("
				+ "(SELECT (ones.n + 10*tens.n + 100*hundreds.n + 1000*thousands.n + 10000*millions.n) AS num "
				+ "FROM (VALUES(0),(1),(2),(3),(4),(5),(6),(7),(8),(9)) ones(n), "
				+ "(VALUES(0),(1),(2),(3),(4),(5),(6),(7),(8),(9)) tens(n), "
				+ "(VALUES(0),(1),(2),(3),(4),(5),(6),(7),(8),(9)) hundreds(n), "
				+ "(VALUES(0),(1),(2),(3),(4),(5),(6),(7),(8),(9)) thousands(n), "
				+ "(VALUES(0),(1),(2),(3),(4),(5),(6),(7),(8),(9)) millions(n) "
				+ "WHERE ones.n + 10*tens.n + 100*hundreds.n + 1000*thousands.n + 10000*millions.n BETWEEN 0 AND 99999)) x "
				+ "ORDER BY 2 ASC)";
		}
		else if (ModuleConfig.getProperty(MwConstants.CONFIG_DB_VENDOR).equals(MwConstants.DB_VENDOR_ORACLE)){
			sql = "INSERT INTO SP_NRE(ID_LOTTO, NRE_PROGRESSIVO) "
					+ "(SELECT :idLotto, x.num FROM (SELECT ((Level)-1) AS num from dual connect by Level < 100001 ORDER BY 1 )x)";
		}
		Session session = getSession();
		Transaction tx = session.beginTransaction();
		try {
			session.save(spLottiNre);
			Query query = session.createSQLQuery(sql);
			query.setLong("idLotto", spLottiNre.getId());
			query.executeUpdate();
			tx.commit();
			esito = Boolean.TRUE;
		} catch (Exception e) {
			tx.rollback();
			logger.error("Eccezione in SpLottiNreDaoImpl.saveLottoAndFillNres: " + e.getMessage());
			throw new DatabaseException("Eccezione in SpLottiNreDaoImpl.saveLottoAndFillNres: " + e.getMessage());
		}finally{
			session.close();
		}
		return esito;
	}

	public void save(SpLottiNre entity) throws DatabaseException {
		try {
			getHibernateTemplate().save(entity);
		}catch(Exception e){
			logger.error("Eccezione in SpLottiNreDaoImpl.save: " + e.getMessage());
			throw new DatabaseException("Eccezione in SpLottiNreDaoImpl.save: " + e.getMessage());
		}
		
	}

	public void update(SpLottiNre entity) throws DatabaseException {
		try {
			getHibernateTemplate().update(entity);
		}catch(Exception e){
			logger.error("Eccezione in SpLottiNreDaoImpl.update: " + e.getMessage());
			throw new DatabaseException("Eccezione in SpLottiNreDaoImpl.update: " + e.getMessage());
		}
		
	}


}