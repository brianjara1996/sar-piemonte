package it.eng.cct.test;

import it.eng.cct.dem.sar.mw.piemonte.db.service.SpLottiNreService;
import it.eng.cct.dem.sar.mw.piemonte.db.service.impl.SpLottiNreServiceImpl;
import it.eng.cct.dem.sar.mw.piemonte.exception.DatabaseException;

public class Test {
	

	@org.junit.Test
	public void testDb() throws DatabaseException{
		SpLottiNreService service = new SpLottiNreServiceImpl();
		Long pippo = service.getActualNre();
	}
}
