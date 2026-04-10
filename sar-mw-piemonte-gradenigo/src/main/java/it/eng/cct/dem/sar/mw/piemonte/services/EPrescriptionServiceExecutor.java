package it.eng.cct.dem.sar.mw.piemonte.services;

import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TEPrescription;
import it.eng.cct.dem.sar.mw.services.ServiceExecutor;
import it.eng.cct.dem.sar.piemonte.mw.schemas.SarMiddleware.ServiceFault;

public interface EPrescriptionServiceExecutor extends ServiceExecutor {

	public TEPrescription invoke(TEPrescription request) throws ServiceFault;
}
