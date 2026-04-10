package it.eng.cct.dem.sar.mw.piemonte.db.service;

import it.eng.cct.dem.sar.mw.piemonte.db.model.SpLottiNre;
import it.eng.cct.dem.sar.mw.piemonte.exception.DatabaseException;
import it.eng.cct.dem.sar.mw.piemonte.exception.NoLottoException;
import it.eng.cct.dem.sar.mw.piemonte.exception.NoMoreNreInLottoException;

public interface SpNreService {

	public String getNre(String cfMedico) throws NoLottoException, NoMoreNreInLottoException, DatabaseException;
	
	public boolean fillLottoNre(SpLottiNre spLottiNre) throws DatabaseException;
}
