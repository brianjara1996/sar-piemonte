package it.eng.cct.dem.sar.mw.piemonte.adapters;

import java.text.ParseException;

import org.apache.commons.lang.StringUtils;

import it.eng.cct.dem.sar.mw.exceptions.MwReqAdapterException;
import it.eng.cct.dem.sar.mw.utils.DateTimeHolder;

public class AreasEPrescriptionAdapter extends MessageAdapter {

	public final static String DATE_PATTERN = "yyyyMMdd";
	public final static String DPCM_ESITI_DATE_PATTERN = "dd/MM/yyyy";
	public final static String DATETIME_PATTERN = "yyyyMMddHHmmss";
	
	protected void setDate(Object bean, String property, String value, String pattern) throws MwReqAdapterException {
		if (StringUtils.isEmpty(value)) {
			return;
		}
		DateTimeHolder holder = null;
		try {
			holder = new DateTimeHolder(value);
		} catch (ParseException e) {
			throw new MwReqAdapterException(e.getMessage(), e);
		}
		this.setIfNotEmpty(bean, property, holder.formatDate(DATE_PATTERN));
	}
	
	protected void setDate(Object bean, String property, String value) throws MwReqAdapterException {
		this.setDate(bean, property, value, DATE_PATTERN);
	}
	
	protected void setDateTime(Object bean, String property, String value) throws MwReqAdapterException {
		this.setDate(bean, property, value, DATETIME_PATTERN);
	}

	
	protected void transcodeAndSet1Null(Object bean, String property, String value) {
		if (StringUtils.isEmpty(value)) {
			this.setIfNotEmpty(bean, property, SistemaTiesseAdapter.WS_EPRESCRIPTION_BOOLEAN_NO);
			return;
		}
		if (value.equals(SistemaTiesseAdapter.WS_TIESSE_BOOLEAN_YES)) {
			this.setIfNotEmpty(bean, property, SistemaTiesseAdapter.WS_EPRESCRIPTION_BOOLEAN_YES);
			return;
		}
		this.setIfNotEmpty(bean, property, SistemaTiesseAdapter.WS_EPRESCRIPTION_BOOLEAN_NO);
	}
	
	protected void transcodeAndSetAccessType(Object bean, String property, String value) {
		if (StringUtils.isEmpty(value)) {
			return;
		}
		if (value.equals(SistemaTiesseAdapter.WS_TIESSE_ACCESS_TYPE_FIRST)) {
			this.setIfNotEmpty(bean, property, SistemaTiesseAdapter.WS_EPRESCRIPTION_ACCESS_TYPE_FIRST);
		} else if (value.equals(SistemaTiesseAdapter.WS_TIESSE_ACCESS_TYPE_OTHER)) {
			this.setIfNotEmpty(bean, property, WS_EPRESCRIPTION_ACCESS_TYPE_OTHER);
		} else {
			//Eventuale altro valore, non ammesso
			this.setIfNotEmpty(bean, property, WS_EPRESCRIPTION_ACCESS_TYPE_OTHER);
		}
	}
}
