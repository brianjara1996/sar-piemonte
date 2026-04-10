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

import org.hibernate.annotations.Type;


/**
 * The persistent class for the "SP_TRANSACTION" database table.
 * 
 */
@Entity
@Table(name="\"SP_TRANSACTION\"", schema = "sarp_mw")
public class SpTransaction implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name="SEQ_TRANS_PK", sequenceName = "sarp_mw.\"SEQ_TRANS_PK\"", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator = "SEQ_TRANS_PK")
	@Column(name="\"ID\"")
	private Long id;

	@Column(name="\"CF_MEDICO\"")
	private String cfMedico;
	
	@Column(name="\"CF_PAZIENTE\"")
	private String cfPaziente;
	
	@Column(name="\"NRE\"")
	private String nre;
	
	@Column(name="\"PROTOCOLLO_SAC\"")
	private String protocolloSac;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="\"DT_INSERT\"")
	private Date dtInsert;
	
	@Column(name="\"MSG_MW_IN\"")
	@Type(type="org.hibernate.type.BinaryType") 
	private byte[] msgMwIn;
	
	@Column(name="\"MSG_MW_SRV\"")
	@Type(type="org.hibernate.type.BinaryType") 
	private byte[] msgMwSrv;
	
	@Column(name="\"MSG_SRV_MW\"")
	@Type(type="org.hibernate.type.BinaryType") 
	private byte[] msgSrvMw;
	
	@Column(name="\"MSG_MW_OUT\"")
	@Type(type="org.hibernate.type.BinaryType") 
	private byte[] msgMwOut;
	
	@Column(name="\"ESITO\"")
	private String esito;
	
	@Column(name="\"TIPO\"")
	private String tipo;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCfMedico() {
		return cfMedico;
	}

	public void setCfMedico(String cfMedico) {
		this.cfMedico = cfMedico;
	}

	public String getCfPaziente() {
		return cfPaziente;
	}

	public void setCfPaziente(String cfPaziente) {
		this.cfPaziente = cfPaziente;
	}

	public String getNre() {
		return nre;
	}

	public void setNre(String nre) {
		this.nre = nre;
	}

	public Date getDtInsert() {
		return dtInsert;
	}

	public void setDtInsert(Date dtInsert) {
		this.dtInsert = dtInsert;
	}

	public String getEsito() {
		return esito;
	}

	public void setEsito(String esito) {
		this.esito = esito;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public byte[] getMsgMwIn() {
		return msgMwIn;
	}

	public void setMsgMwIn(byte[] msgMwIn) {
		this.msgMwIn = msgMwIn;
	}

	public byte[] getMsgMwSrv() {
		return msgMwSrv;
	}

	public void setMsgMwSrv(byte[] msgMwSrv) {
		this.msgMwSrv = msgMwSrv;
	}

	public byte[] getMsgSrvMw() {
		return msgSrvMw;
	}

	public void setMsgSrvMw(byte[] msgSrvMw) {
		this.msgSrvMw = msgSrvMw;
	}

	public byte[] getMsgMwOut() {
		return msgMwOut;
	}

	public void setMsgMwOut(byte[] msgMwOut) {
		this.msgMwOut = msgMwOut;
	}

	public String getProtocolloSac() {
		return protocolloSac;
	}

	public void setProtocolloSac(String protocolloSac) {
		this.protocolloSac = protocolloSac;
	}
	
}