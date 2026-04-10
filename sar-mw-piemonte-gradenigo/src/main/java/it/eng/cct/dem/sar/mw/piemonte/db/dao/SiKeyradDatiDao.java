package it.eng.cct.dem.sar.mw.piemonte.db.dao;

import it.eng.cct.dem.sar.mw.piemonte.db.model.IrideSiKeyradDati;
import it.eng.cct.dem.sar.mw.piemonte.exception.DatabaseException;

public interface SiKeyradDatiDao {

	public IrideSiKeyradDati getByKdKey(Long kdKey) throws DatabaseException;

	
}
