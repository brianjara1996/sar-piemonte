package it.eng.cct.dem.sar.mw.piemonte.db.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;


/**
 * 
 */
@Embeddable
public class SpNrePk implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Column(name="\"ID_LOTTO\"")
	private Long idLotto;
	
	@Column(name="\"NRE_PROGRESSIVO\"")
	private Long nreProgressivo;

	public Long getIdLotto() {
		return idLotto;
	}

	public void setIdLotto(Long idLotto) {
		this.idLotto = idLotto;
	}

	public Long getNreProgressivo() {
		return nreProgressivo;
	}

	public void setNreProgressivo(Long nreProgressivo) {
		this.nreProgressivo = nreProgressivo;
	}

}