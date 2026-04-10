package it.eng.cct.dem.sar.mw.piemonte.db.dao;

import it.eng.cct.dem.sar.mw.piemonte.db.model.IrideSiKeyrad;
import it.eng.cct.dem.sar.mw.piemonte.exception.DatabaseException;

public interface SiKeyradDao {

	public IrideSiKeyrad getByKrNome(String krNome) throws DatabaseException;

	
}
