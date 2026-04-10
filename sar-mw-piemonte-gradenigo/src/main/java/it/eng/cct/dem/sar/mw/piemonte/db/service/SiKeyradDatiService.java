package it.eng.cct.dem.sar.mw.piemonte.db.service;

import it.eng.cct.dem.sar.mw.piemonte.db.model.IrideSiKeyradDati;
import it.eng.cct.dem.sar.mw.piemonte.exception.DatabaseException;

public interface SiKeyradDatiService {

	public IrideSiKeyradDati getByKdKey(Long kdKey) throws DatabaseException;
}
