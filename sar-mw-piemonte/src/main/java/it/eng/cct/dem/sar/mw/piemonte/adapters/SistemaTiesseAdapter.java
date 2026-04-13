package it.eng.cct.dem.sar.mw.piemonte.adapters;

import java.text.ParseException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	public final static String WS_TIESSE_ACCESS_TYPE_FIRST = "1";
	public final static String WS_TIESSE_ACCESS_TYPE_OTHER = "0";
	public final static String WS_TIESSE_PROMEMORIA_YES = "0";
	public final static String WS_TIESSE_PROMEMORIA_NO = "1";
	private static final String HEADER_ID_SESSIONE = "x-idsessione";
	private static final String HEADER_OAUTH2_AUTHORIZATION = "x-OAuth2-Authorization";
	private static final String HEADER_GESTIONALE = "x-gestionale";

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

	protected void propagateSessionToken(Object targetBean, Object sourceBean) {
		String sessionToken = getSessionToken(sourceBean);
		if (StringUtils.isEmpty(sessionToken)) {
			return;
		}
		setPropertyIfSupported(targetBean, "idSessione", sessionToken);
		setPropertyIfSupported(targetBean, "token", sessionToken);
	}

	protected boolean isOAuth2Request(Object sourceBean) {
		return StringUtils.isNotEmpty(getStringProperty(sourceBean, "getToken"));
	}

	protected String getPinCodeForRequest(String pinCode, Object sourceBean) {
		if (this.isOAuth2Request(sourceBean)) {
			return "";
		}
		return pinCode;
	}

	protected Map<String, String> enrichCustomPropsWithTransportAuth(Map<String, String> customProps, Object sourceBean) {
		Map<String, String> enrichedProps = customProps == null ? new HashMap<String, String>() : new HashMap<String, String>(customProps);
		String oauth2Token = getStringProperty(sourceBean, "getToken");
		String idSessione = getStringProperty(sourceBean, "getIdSessione");
		if (StringUtils.isNotEmpty(oauth2Token)) {
			enrichedProps.put(HEADER_OAUTH2_AUTHORIZATION, oauth2Token);
		} else if (StringUtils.isNotEmpty(idSessione)) {
			enrichedProps.put(HEADER_ID_SESSIONE, idSessione);
			String gestionale = getGestionaleValue(sourceBean);
			if (StringUtils.isNotEmpty(gestionale)) {
				enrichedProps.put(HEADER_GESTIONALE, gestionale);
			}
		}
		return enrichedProps;
	}

	private String getGestionaleValue(Object sourceBean) {
		String gestionale = getStringProperty(sourceBean, "getGestionale");
		if (StringUtils.isNotEmpty(gestionale)) {
			return gestionale;
		}
		gestionale = getStringProperty(sourceBean, "getCodiceApplicativo");
		if (StringUtils.isNotEmpty(gestionale)) {
			return gestionale;
		}
		gestionale = getNestedStringProperty(sourceBean, "getDatiApplicativo", "getNome");
		if (StringUtils.isNotEmpty(gestionale)) {
			return gestionale;
		}
		return getNestedStringProperty(sourceBean, "getDatiApplicativo", "getCodiceApplicativo");
	}

	private String getSessionToken(Object sourceBean) {
		String idSessione = getStringProperty(sourceBean, "getIdSessione");
		if (StringUtils.isNotEmpty(idSessione)) {
			return idSessione;
		}
		return getStringProperty(sourceBean, "getToken");
	}

	private String getStringProperty(Object sourceBean, String getterName) {
		try {
			Method getter = sourceBean.getClass().getMethod(getterName);
			Object value = getter.invoke(sourceBean);
			if (value instanceof String) {
				return (String) value;
			}
		} catch (NoSuchMethodException e) {
			// NOP
		} catch (IllegalAccessException e) {
			// NOP
		} catch (InvocationTargetException e) {
			// NOP
		}
		return null;
	}

	private String getNestedStringProperty(Object sourceBean, String nestedGetterName, String leafGetterName) {
		try {
			Method nestedGetter = sourceBean.getClass().getMethod(nestedGetterName);
			Object nestedBean = nestedGetter.invoke(sourceBean);
			if (nestedBean == null) {
				return null;
			}
			return getStringProperty(nestedBean, leafGetterName);
		} catch (NoSuchMethodException e) {
			// NOP
		} catch (IllegalAccessException e) {
			// NOP
		} catch (InvocationTargetException e) {
			// NOP
		}
		return null;
	}

	private void setPropertyIfSupported(Object targetBean, String propertyName, String value) {
		String setterName = "set" + StringUtils.capitalize(propertyName);
		for (Method method : targetBean.getClass().getMethods()) {
			if (method.getName().equals(setterName) && method.getParameterTypes().length == 1) {
				this.setIfNotEmpty(targetBean, propertyName, value);
				return;
			}
		}
	}
}
