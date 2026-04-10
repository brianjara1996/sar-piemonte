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
 * The persistent class for the "SP_TRANSACTION_ERROR" database table.
 * 
 */
@Entity
@Table(name="\"SP_TRANSACTION_ERROR\"", schema = "sarp_mw")
public class SpTransactionError implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name="SEQ_TRANS_ERR_PK", sequenceName = "sarp_mw.\"SEQ_TRANS_ERR_PK\"", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator = "SEQ_TRANS_ERR_PK")
	@Column(name="\"ID\"")
	private Long id;

	@Column(name="\"COD_ERROR\"")
	private String codError;
	
	@Column(name="\"DESCR_ERROR\"")
	private String descrError;
	
	@Column(name="\"OPERATION\"")
	private String operation;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="\"DT_INSERT\"")
	private Date dtInsert;
	
	@Column(name="\"ERROR_ORDER\"")
	private Long order;
	
	@Column(name="\"TRANS_ID\"")
	private Long idTrans;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCodError() {
		return codError;
	}

	public void setCodError(String codError) {
		this.codError = codError;
	}

	public String getDescrError() {
		return descrError;
	}

	public void setDescrError(String descrError) {
		this.descrError = descrError;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public Date getDtInsert() {
		return dtInsert;
	}

	public void setDtInsert(Date dtInsert) {
		this.dtInsert = dtInsert;
	}

	public Long getOrder() {
		return order;
	}

	public void setOrder(Long order) {
		this.order = order;
	}

	public Long getIdTrans() {
		return idTrans;
	}

	public void setIdTrans(Long idTrans) {
		this.idTrans = idTrans;
	}


	
}