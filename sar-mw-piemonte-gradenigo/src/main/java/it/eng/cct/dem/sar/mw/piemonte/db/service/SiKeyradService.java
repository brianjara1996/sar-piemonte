package it.eng.cct.dem.sar.mw.piemonte.db.service;

import it.eng.cct.dem.sar.mw.piemonte.db.model.IrideSiKeyrad;
import it.eng.cct.dem.sar.mw.piemonte.exception.DatabaseException;

public interface SiKeyradService {

	public IrideSiKeyrad getByKrNome(String krNome) throws DatabaseException;
}
