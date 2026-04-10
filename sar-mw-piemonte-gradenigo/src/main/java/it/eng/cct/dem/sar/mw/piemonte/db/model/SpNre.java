package it.eng.cct.dem.sar.mw.piemonte.db.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;


/**
 * The persistent class for the "SP_NRE" database table.
 * 
 */
@Entity
@Table(name="\"SP_NRE\"", schema = "sarp_mw")
public class SpNre implements Serializable {
	private static final long serialVersionUID = 1L;

	@EmbeddedId
	private SpNrePk id;

	@Column(name="\"CF_MEDICO\"")
	private String cfMedico;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="\"DT_INSERT\"", updatable=false)
	private Date dtInsert;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="\"DT_ASSEGNAZIONE\"")
	private Date dtAssegnazione;
	
	@Transient
	private Boolean last = Boolean.FALSE;

	public String getCfMedico() {
		return cfMedico;
	}

	public void setCfMedico(String cfMedico) {
		this.cfMedico = cfMedico;
	}

	public Date getDtInsert() {
		return dtInsert;
	}

	public void setDtInsert(Date dtInsert) {
		this.dtInsert = dtInsert;
	}

	public Date getDtAssegnazione() {
		return dtAssegnazione;
	}

	public void setDtAssegnazione(Date dtAssegnazione) {
		this.dtAssegnazione = dtAssegnazione;
	}

	public Boolean getLast() {
		return last;
	}

	public void setLast(Boolean last) {
		this.last = last;
	}

	public SpNrePk getId() {
		return id;
	}

	public void setId(SpNrePk id) {
		this.id = id;
	}
	
}