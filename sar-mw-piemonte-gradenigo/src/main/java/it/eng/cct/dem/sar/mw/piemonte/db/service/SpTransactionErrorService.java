package it.eng.cct.dem.sar.mw.piemonte.db.service;

import it.eng.cct.dem.sar.mw.piemonte.db.model.SpTransactionError;
import it.eng.cct.dem.sar.mw.piemonte.exception.DatabaseException;

public interface SpTransactionErrorService {

	public void save(SpTransactionError spTransactionsError) throws DatabaseException;
}
