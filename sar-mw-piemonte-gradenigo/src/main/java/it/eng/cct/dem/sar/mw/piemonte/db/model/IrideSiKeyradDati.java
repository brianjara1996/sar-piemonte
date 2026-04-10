package it.eng.cct.dem.sar.mw.piemonte.db.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


/**
 * The persistent class for the "SI_KEYRAD_DATI" database table.
 * 
 */
@Entity
@Table(name="\"SI_KEYRAD_DATI\"", schema = "\"IRIDE\"")
public class IrideSiKeyradDati implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="\"KD_ID\"")
	private Long kdId;

	@Column(name="\"KD_KEY\"")
	private Long kdKey;
	
	@Column(name="\"KD_VALUE\"")
	private String kdValue;

	public Long getKdId() {
		return kdId;
	}

	public void setKdId(Long kdId) {
		this.kdId = kdId;
	}

	public String getKdValue() {
		return kdValue;
	}

	public void setKdValue(String kdValue) {
		this.kdValue = kdValue;
	}

	public Long getKdKey() {
		return kdKey;
	}

	public void setKdKey(Long kdKey) {
		this.kdKey = kdKey;
	}

}