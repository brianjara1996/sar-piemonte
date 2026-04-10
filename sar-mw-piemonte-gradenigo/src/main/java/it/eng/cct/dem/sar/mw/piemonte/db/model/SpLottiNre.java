package it.eng.cct.dem.sar.mw.piemonte.db.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;


/**
 * The persistent class for the "SP_LOTTINRE" database table.
 * 
 */
@Entity
@Table(name="\"SP_LOTTINRE\"", schema = "sarp_mw")
public class SpLottiNre implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name="SEQ_LOTTO_PK", sequenceName = "sarp_mw.\"SEQ_LOTTO_PK\"", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator = "SEQ_LOTTO_PK")
	@Column(name="\"ID\"")
	private Long id;


	@Column(name="\"CF_RICHIEDENTE\"")
	private String cfRichiedente;
	
	@Column(name="\"COD_LOTTO\"")
	private String codLotto;
	
	@Column(name="\"COD_RAG_LOTTO\"")
	private String codRagLotto;
	
	@Column(name="\"COD_REGIONE_LOTTO\"")
	private String codRegioneLotto;
	
	@Column(name="\"COD_ID_LOTTO\"")
	private String codIdLotto;
	
	@org.hibernate.annotations.Type(type="true_false")
	@Column(name="\"TERMINATO\"")
	private boolean terminato;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="\"DT_INSERT\"")
	private Date dtInsert;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="\"DT_FINE\"")
	private Date dtFine;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getDtInsert() {
		return dtInsert;
	}

	public void setDtInsert(Date dtInsert) {
		this.dtInsert = dtInsert;
	}

	public String getCfRichiedente() {
		return cfRichiedente;
	}

	public void setCfRichiedente(String cfRichiedente) {
		this.cfRichiedente = cfRichiedente;
	}

	public boolean isTerminato() {
		return terminato;
	}

	public void setTerminato(boolean terminato) {
		this.terminato = terminato;
	}

	public Date getDtFine() {
		return dtFine;
	}

	public void setDtFine(Date dtFine) {
		this.dtFine = dtFine;
	}

	public String getCodLotto() {
		return codLotto;
	}

	public void setCodLotto(String codLotto) {
		this.codLotto = codLotto;
	}

	public String getCodRagLotto() {
		return codRagLotto;
	}

	public void setCodRagLotto(String codRagLotto) {
		this.codRagLotto = codRagLotto;
	}

	public String getCodRegioneLotto() {
		return codRegioneLotto;
	}

	public void setCodRegioneLotto(String codRegioneLotto) {
		this.codRegioneLotto = codRegioneLotto;
	}

	public String getCodIdLotto() {
		return codIdLotto;
	}

	public void setCodIdLotto(String codIdLotto) {
		this.codIdLotto = codIdLotto;
	}

}