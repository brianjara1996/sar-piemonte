package it.eng.cct.dem.sar.mw.piemonte.db.service;

import it.eng.cct.dem.sar.mw.piemonte.db.model.SpTransaction;
import it.eng.cct.dem.sar.mw.piemonte.exception.DatabaseException;

public interface SpTransactionService {

	public void save(SpTransaction spTransaction) throws DatabaseException;
}
