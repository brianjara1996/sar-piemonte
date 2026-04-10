package it.eng.cct.dem.sar.mw.piemonte.db.dao;

import it.eng.cct.dem.sar.mw.piemonte.db.model.SpTransactionError;
import it.eng.cct.dem.sar.mw.piemonte.exception.DatabaseException;

public interface SpTransactionErrorDao {

	public void save(SpTransactionError spTransactionError) throws DatabaseException;
	
	public void update(SpTransactionError spTransactionError) throws DatabaseException;
}
