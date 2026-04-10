package it.eng.cct.dem.sar.mw.piemonte.adapters;

import it.eng.cct.dem.sar.mw.exceptions.MwReqAdapterException;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.TErrore;
import it.finanze.sanita.dem.xsd.tipodati.ComunicazioneType;
import it.finanze.sanita.dem.xsd.tipodati.ElencoComunicazioniType;
import it.finanze.sanita.dem.xsd.tipodati.ElencoErroriRicetteType;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MessageAdapter {

	private static final Logger logger = LoggerFactory.getLogger(MessageAdapter.class);
	public final static String WS_EPRESCRIPTION_ACCESS_TYPE_FIRST = "P";
	public final static String WS_EPRESCRIPTION_ACCESS_TYPE_OTHER = "S";

	protected void setIfNotEmpty(Object bean, String property, String value) {
		if (StringUtils.isNotEmpty(value)) {
			this.setProperty(bean, property, value);
		}
	}

	protected void setProperty(Object bean, String property, String value) {
		try {
			BeanUtils.setProperty(bean, property, value);
		} catch (IllegalAccessException e) {
			logger.error("Cannot set property " + property + " with value " + value + " on bean " + bean.getClass().getCanonicalName() + ". " + e.getMessage(), e);
		} catch (InvocationTargetException e) {
			logger.error("Cannot set property " + property + " with value " + value + " on bean " + bean.getClass().getCanonicalName() + ". " + e.getMessage(), e);
		}
	}

	protected void setMandatory(Object bean, String property, String value) throws MwReqAdapterException {
		if (value == null) {
			throw new MwReqAdapterException("Cannot set value null for " + property + ".");
		}
		this.setIfNotEmpty(bean, property, value);
	}

	public static List<TErrore> getErroriAndComunicazioni(ElencoComunicazioniType elencoComunicazioni, ElencoErroriRicetteType elencoErroriRicette) {
		List<TErrore> results = new ArrayList<TErrore>();
		if (elencoErroriRicette != null) {
			it.finanze.sanita.dem.xsd.tipodati.ErroreRicettaType[] errori = elencoErroriRicette.getErroreRicettaArray();
			if (errori != null && errori.length > 0) {
				for (it.finanze.sanita.dem.xsd.tipodati.ErroreRicettaType errore : errori) {
					String tipoErrore = errore.getTipoErrore();
					String codEsito = errore.getCodEsito();
					if (!"0000".equals(codEsito)) {
						if ("Avviso".equals(tipoErrore)) {
							logger.info("Skipping: " + errore.getCodEsito() + ", " + errore.getEsito() + ", " + errore.getTipoErrore());
						} else {
							logger.info("Adding errore to response: " + errore.getCodEsito() + ", " + errore.getEsito() + ", " + errore.getTipoErrore());
							TErrore result = TErrore.Factory.newInstance();
							result.setCodice(errore.getCodEsito());
							result.setDescrizione(errore.getEsito());
							results.add(result);
						}
					}
				}
			}
		}
		//TODO Eliminata gestione equiparata di errori e comunicazioni
		return results;
	}

	public static TErrore[] getErroriAndComunicazioniArray(ElencoComunicazioniType elencoComunicazioni, ElencoErroriRicetteType elencoErroriRicette) {
		List<TErrore> erroriAndCom = MessageAdapter.getErroriAndComunicazioni(elencoComunicazioni, elencoErroriRicette);
		if (erroriAndCom == null || erroriAndCom.isEmpty()) {
			return null;
		}
		TErrore[] result = new TErrore[erroriAndCom.size()];
		for (int i = 0; i < erroriAndCom.size(); i++) {
			result[i] = erroriAndCom.get(i);
		}
		return result;
	}
}
