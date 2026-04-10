package it.eng.cct.dem.sar.mw.piemonte.db.dao.impl;

import java.math.BigDecimal;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import it.eng.cct.dem.sar.generic.wsclient.commons.utils.ModuleConfig;
import it.eng.cct.dem.sar.mw.piemonte.constants.MwConstants;
import it.eng.cct.dem.sar.mw.piemonte.db.dao.SpNreDao;
import it.eng.cct.dem.sar.mw.piemonte.db.model.SpLottiNre;
import it.eng.cct.dem.sar.mw.piemonte.db.model.SpNre;
import it.eng.cct.dem.sar.mw.piemonte.db.model.SpNrePk;
import it.eng.cct.dem.sar.mw.piemonte.exception.DatabaseException;

public class SpNreDaoImpl extends HibernateDaoSupport implements SpNreDao {

	private static final Logger logger = LoggerFactory.getLogger(SpNreDaoImpl.class);

	public SpNre getNextNre(SpLottiNre lottoNreDb) throws DatabaseException {
		SpNre spNre = null;
		String sql = "SELECT MIN(\"NRE_PROGRESSIVO\") FROM sarp_mw.\"SP_NRE\" WHERE \"CF_MEDICO\" IS NULL AND \"DT_ASSEGNAZIONE\" IS NULL AND \"ID_LOTTO\" = :idLotto";
		Session session = getSession();
		Transaction tx = session.beginTransaction();
		try {
			Query query = getSession().createSQLQuery(sql);
			query.setLong("idLotto", lottoNreDb.getId());
			Object x = query.uniqueResult();
			BigDecimal n = null;
			if(x != null && x instanceof String)
				n = new BigDecimal((String)x);
			else if(x != null && x instanceof BigDecimal)
				n = (BigDecimal) x;
			spNre = new SpNre();
			SpNrePk pk = new SpNrePk();
			pk.setNreProgressivo(n.longValueExact());
			pk.setIdLotto(lottoNreDb.getId());
			spNre.setId(pk);
			if(spNre.getId().getNreProgressivo().equals(new Long(99999)))
				spNre.setLast(Boolean.TRUE);
			tx.commit();
		} catch (Exception e) {
			tx.rollback();
			logger.error("Eccezione in SpNreDaoImpl.getNextNre: " + e.getMessage());
			throw new DatabaseException("Eccezione in SpNreDaoImpl.getNextNre: " + e.getMessage());
		}finally{
			session.close();
		}
		return spNre;
	}

	public boolean fillNres(Long idLotto) throws DatabaseException{
		boolean esito = Boolean.FALSE;
		String sql = "";
		if(ModuleConfig.getProperty(MwConstants.CONFIG_DB_VENDOR).equals(MwConstants.DB_VENDOR_POSTGRES))
			sql = "INSERT sarp_mw.\"SP_NRE\" (\"ID_LOTTO\", \"NRE_PROGRESSIVO\") "
				+ "( SELECT :idLotto, x.num FROM ("
				+ "(SELECT (ones.n + 10*tens.n + 100*hundreds.n + 1000*thousands.n + 10000*millions.n) AS num "
				+ "FROM (VALUES(0),(1),(2),(3),(4),(5),(6),(7),(8),(9)) ones(n), "
				+ "(VALUES(0),(1),(2),(3),(4),(5),(6),(7),(8),(9)) tens(n), "
				+ "(VALUES(0),(1),(2),(3),(4),(5),(6),(7),(8),(9)) hundreds(n), "
				+ "(VALUES(0),(1),(2),(3),(4),(5),(6),(7),(8),(9)) thousands(n), "
				+ "(VALUES(0),(1),(2),(3),(4),(5),(6),(7),(8),(9)) millions(n) "
				+ "WHERE ones.n + 10*tens.n + 100*hundreds.n + 1000*thousands.n + 10000*millions.n BETWEEN 0 AND 99999)) x "
				+ "ORDER BY 2 ASC)";
		else if(ModuleConfig.getProperty(MwConstants.CONFIG_DB_VENDOR).equals(MwConstants.DB_VENDOR_ORACLE)){
			sql = "INSERT INTO SP_NRE(ID_LOTTO, NRE_PROGRESSIVO) "
					+ "(SELECT :idLotto, x.num FROM "
					+ "(SELECT ((Level)-1) AS num from dual connect by Level < 100001 ORDER BY 1)x)";
		}
		Session session = getSession();
		Transaction tx = session.beginTransaction();
		try {
			Query query = getSession().createSQLQuery(sql);
			query.setLong("idLotto", idLotto);
			query.executeUpdate();
			tx.commit();
			esito = Boolean.TRUE;
		} catch (Exception e) {
			tx.rollback();
			logger.error("Eccezione in SpNreDaoImpl.fillNres: " + e.getMessage());
			throw new DatabaseException("Eccezione in SpNreDaoImpl.fillNres: " + e.getMessage());
		}finally{
			session.close();
		}
		return esito;
	}
	
	public void save(SpNre entity) throws DatabaseException {
		try {
			getHibernateTemplate().save(entity);
		}catch(Exception e){
			logger.error("Eccezione in SpNreDaoImpl.save: " + e.getMessage());
			throw new DatabaseException("Eccezione in SpNreDaoImpl.save: " + e.getMessage());
		}
	}

	public void update(SpNre entity) throws DatabaseException {
		try {
			getHibernateTemplate().update(entity);
		}catch(Exception e){
			logger.error("Eccezione in SpNreDaoImpl.update: " + e.getMessage());
			throw new DatabaseException("Eccezione in SpNreDaoImpl.update: " + e.getMessage());
		}
	}
}