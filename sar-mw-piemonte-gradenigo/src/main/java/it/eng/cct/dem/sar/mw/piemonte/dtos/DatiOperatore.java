package it.eng.cct.dem.sar.mw.piemonte.dtos;

import java.io.Serializable;

/**
 * The Class DatiOperatore.
 */
public class DatiOperatore implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** Codice fiscale dell'operatore. */
	private String codiceFiscale;

	/** Ruolo istituzionale dell'operatore. */
	private String ruoloIstituzionale;

	/** Codice della struttura dell'operatore. */
	private String codStruttura;

	/**
	 * Instantiates a new dati operatore.
	 */
	public DatiOperatore() {
		super();
	}

	/**
	 * Gets the codice fiscale.
	 * 
	 * @return the codice fiscale
	 */
	public String getCodiceFiscale() {
		return codiceFiscale;
	}

	/**
	 * Sets the codice fiscale.
	 * 
	 * @param codiceFiscale
	 *            the new codice fiscale
	 */
	public void setCodiceFiscale(String codiceFiscale) {
		this.codiceFiscale = codiceFiscale;
	}

	/**
	 * Gets the ruolo istituzionale.
	 * 
	 * @return the ruolo istituzionale
	 */
	public String getRuoloIstituzionale() {
		return ruoloIstituzionale;
	}

	/**
	 * Sets the ruolo istituzionale.
	 * 
	 * @param ruoloIstituzionale
	 *            the new ruolo istituzionale
	 */
	public void setRuoloIstituzionale(String ruoloIstituzionale) {
		this.ruoloIstituzionale = ruoloIstituzionale;
	}

	/**
	 * Gets the cod struttura.
	 * 
	 * @return the cod struttura
	 */
	public String getCodStruttura() {
		return codStruttura;
	}

	/**
	 * Sets the cod struttura.
	 * 
	 * @param codStruttura
	 *            the new cod struttura
	 */
	public void setCodStruttura(String codStruttura) {
		this.codStruttura = codStruttura;
	}

	@Override
	public String toString() {
		return "DatiOperatore [codiceFiscale=" + codiceFiscale + ", ruoloIstituzionale=" + ruoloIstituzionale + ", codStruttura=" + codStruttura + "]";
	}

}
