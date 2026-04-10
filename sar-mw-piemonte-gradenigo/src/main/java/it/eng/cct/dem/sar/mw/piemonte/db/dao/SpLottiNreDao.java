package it.eng.cct.dem.sar.mw.piemonte.db.dao;

import it.eng.cct.dem.sar.mw.piemonte.db.model.SpLottiNre;
import it.eng.cct.dem.sar.mw.piemonte.exception.DatabaseException;

public interface SpLottiNreDao {

	public Long getNreSeqCurVal() throws DatabaseException;
	
	public Long getNreSeqNextVal() throws DatabaseException;
	
	public SpLottiNre getLottoAttivo() throws DatabaseException;
	
	public boolean saveLottoAndFillNres(SpLottiNre spLottiNre) throws DatabaseException;
	
	public void save(SpLottiNre spLottiNre) throws DatabaseException;
	
	public void update(SpLottiNre spLottiNre) throws DatabaseException;
	
}
