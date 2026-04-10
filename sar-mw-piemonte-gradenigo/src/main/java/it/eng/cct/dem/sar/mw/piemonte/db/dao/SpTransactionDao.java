package it.eng.cct.dem.sar.mw.piemonte.db.dao;

import it.eng.cct.dem.sar.mw.piemonte.db.model.SpTransaction;
import it.eng.cct.dem.sar.mw.piemonte.exception.DatabaseException;

public interface SpTransactionDao {

	public void save(SpTransaction spTransaction) throws DatabaseException;
	
	public void update(SpTransaction spTransaction) throws DatabaseException;
}
