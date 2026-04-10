package it.eng.cct.dem.sar.mw.piemonte.db.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


/**
 * The persistent class for the "SI_KEYRAD" database table.
 * 
 */
@Entity
@Table(name="\"SI_KEYRAD\"", schema = "\"IRIDE\"")
public class IrideSiKeyrad implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="\"KR_KEY\"")
	private Long krKey;

	@Column(name="\"KR_PARENT_KEY\"")
	private Long KrParentKey;
	
	@Column(name="\"KR_NOME\"")
	private String krNome;

	public Long getKrKey() {
		return krKey;
	}

	public void setKrKey(Long krKey) {
		this.krKey = krKey;
	}

	public String getKrNome() {
		return krNome;
	}

	public void setKrNome(String krNome) {
		this.krNome = krNome;
	}

	public Long getKrParentKey() {
		return KrParentKey;
	}

	public void setKrParentKey(Long krParentKey) {
		KrParentKey = krParentKey;
	}

}