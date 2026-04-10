package it.eng.cct.dem.sar.mw.piemonte.constants;

import org.apache.commons.lang.StringUtils;

public interface DemInvioPrescrittoConstants extends CilConstants {
	public static final String CONFIG_CIL_DEMINVIOPRESCRITTO_OPERATION_NAME = "config.ws.target.cil.demInvioPrescritto.operationName";
	public static final String CONFIG_CIL_DEMINVIOPRESCRITTO_SERVICE_NAME = "config.ws.target.cil.demInvioPrescritto.serviceName";
	public static final String CONFIG_CIL_DEMINVIOPRESCRITTO_SERVICE_ENDPOINT = "config.ws.target.cil.demInvioPrescritto.serviceEndpoint";
	public static final String CONFIG_CIL_DEMINVIOPRESCRITTO_SECCONFIGID = "config.ws.target.cil.demInvioPrescritto.securityConfigId";
	public static final String CONFIG_CIL_DEMINVIOPRESCRITTO_ROOT = "config.ws.target.cil.demInvioPrescritto";

	public static enum TipoRic {
		EE, UE, NA, ND, NE, NX, ST;
		
		public static boolean isValidTipoRic(String tipoRic) {
			if (StringUtils.isEmpty(tipoRic)) {
				return false;
			}
			DemInvioPrescrittoConstants.TipoRic[] tipoRicList = DemInvioPrescrittoConstants.TipoRic.values();
			boolean enumExists = false;
			for (DemInvioPrescrittoConstants.TipoRic currTipoRic: tipoRicList) {
				if (currTipoRic.toString().equalsIgnoreCase(tipoRic)) {
					enumExists = true;
				}
			}
			return enumExists;
		}
	}
}
