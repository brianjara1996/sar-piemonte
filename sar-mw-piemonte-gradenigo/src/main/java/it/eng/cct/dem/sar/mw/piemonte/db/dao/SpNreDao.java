package it.eng.cct.dem.sar.mw.piemonte.db.dao;

import it.eng.cct.dem.sar.mw.piemonte.db.model.SpLottiNre;
import it.eng.cct.dem.sar.mw.piemonte.db.model.SpNre;
import it.eng.cct.dem.sar.mw.piemonte.exception.DatabaseException;

public interface SpNreDao {

	public SpNre getNextNre(SpLottiNre lottoNreDb) throws DatabaseException;
	
	public boolean fillNres(Long idLotto) throws DatabaseException;
	
	public void save(SpNre spNre) throws DatabaseException;
	
	public void update(SpNre spNre) throws DatabaseException;
}
