package it.eng.cct.dem.sar.mw.piemonte.adapters;

import java.text.ParseException;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import it.eng.cct.dem.sar.generic.wsclient.commons.utils.ModuleConfig;
import it.eng.cct.dem.sar.mw.exceptions.MwReqAdapterException;
import it.eng.cct.dem.sar.mw.piemonte.constants.EPrescriptionConstants;
import it.eng.cct.dem.sar.mw.utils.DateTimeHolder;

public class SistemaTiesseAdapter extends MessageAdapter {

	public static final String WS_EPRESCRIPTION_BOOLEAN_YES = ModuleConfig.getProperty(EPrescriptionConstants.CONFIG_WS_EPRESCRIPTION_BOOLEAN_YES);
	public static final String WS_EPRESCRIPTION_BOOLEAN_NO = ModuleConfig.getProperty(EPrescriptionConstants.CONFIG_WS_EPRESCRIPTION_BOOLEAN_NO);
	public static final List<String> WS_EPRESCRIPTION_ESENZIONI_PER_REDDITO = ModuleConfig.getListOfValues(EPrescriptionConstants.CONFIG_WS_EPRESCRIPTION_LIST_ESENZIONI_REDDITO, ",");

	public static final String WS_TIESSE_BOOLEAN_YES = "1";
	public final static String WS_TIESSE_DATE_PATTERN = "yyyy-MM-dd";
	public final static String WS_TIESSE_DPCM_DATE_PATTERN = "dd/MM/yyyy";
	public final static String WS_TIESSE_DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

	protected void setMandatoryDate(Object bean, String property, String value, String datePattern) throws MwReqAdapterException {
		DateTimeHolder holder = null;
		try {
			holder = new DateTimeHolder(value);
		} catch (ParseException e) {
			throw new MwReqAdapterException(e.getMessage(), e);
		}
		this.setIfNotEmpty(bean, property, holder.formatDate(datePattern));
	}

	protected void setDateIfNotEmpty(Object bean, String property, String value, String datePattern) throws MwReqAdapterException {
		if (StringUtils.isNotEmpty(value)) {
			this.setMandatoryDate(bean, property, value, datePattern);
		}
	}

	protected void setDateIfNotEmpty(Object bean, String property, String value) throws MwReqAdapterException {
		this.setDateIfNotEmpty(bean, property, value, WS_TIESSE_DATE_PATTERN);
	}

	protected void setDateTimeIfNotEmpty(Object bean, String property, String value) throws MwReqAdapterException {
		this.setDateIfNotEmpty(bean, property, value, WS_TIESSE_DATETIME_PATTERN);
	}

	protected void setMandatoryDate(Object bean, String property, String value) throws MwReqAdapterException {
		this.setMandatoryDate(bean, property, value, WS_TIESSE_DATE_PATTERN);
	}

	protected void setMandatoryDateTime(Object bean, String property, String value) throws MwReqAdapterException {
		this.setMandatoryDate(bean, property, value, WS_TIESSE_DATETIME_PATTERN);
	}

	protected void transcodeAndSetSN(Object bean, String property, String value) {
		if (StringUtils.isEmpty(value)) {
			return;
		}
		if (value.equals(WS_EPRESCRIPTION_BOOLEAN_YES)) {
			this.setIfNotEmpty(bean, property, WS_TIESSE_BOOLEAN_YES);
		}
		if (value.equals(WS_EPRESCRIPTION_BOOLEAN_NO)) {
			// NOP
		}
	}
	
	protected void transcodeAndSetAccessType(Object bean, String property, String value) {
		if (StringUtils.isEmpty(value)) {
			return;
		}
		if (value.equals(WS_EPRESCRIPTION_ACCESS_TYPE_FIRST)) {
			this.setIfNotEmpty(bean, property, WS_TIESSE_ACCESS_TYPE_FIRST);
		} else if (value.equals(WS_EPRESCRIPTION_ACCESS_TYPE_OTHER)) {
			this.setIfNotEmpty(bean, property, WS_TIESSE_ACCESS_TYPE_OTHER);
		} else {
			//Eventuale altro valore, non ammesso
			this.setIfNotEmpty(bean, property, WS_TIESSE_ACCESS_TYPE_OTHER);
		}
	}

	public boolean isEsentePerReddito(String value) {
		if (StringUtils.isEmpty(value)) {
			return false;
		}
		return WS_EPRESCRIPTION_ESENZIONI_PER_REDDITO.contains(value);
	}
}
