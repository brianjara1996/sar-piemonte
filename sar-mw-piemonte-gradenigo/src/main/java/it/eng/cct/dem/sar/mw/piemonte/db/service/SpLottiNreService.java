package it.eng.cct.dem.sar.mw.piemonte.db.service;

import it.eng.cct.dem.sar.mw.piemonte.db.model.SpLottiNre;
import it.eng.cct.dem.sar.mw.piemonte.exception.DatabaseException;

public interface SpLottiNreService {

	public Long getActualNre() throws DatabaseException;
	
	public SpLottiNre getLottoAttivo() throws DatabaseException;
	
	public void saveLotto(SpLottiNre spLottiNre) throws DatabaseException;
	
	public Long getNextNre() throws DatabaseException;
	
	public boolean saveLottoAndFillNres(SpLottiNre spLottiNre) throws DatabaseException;
}
