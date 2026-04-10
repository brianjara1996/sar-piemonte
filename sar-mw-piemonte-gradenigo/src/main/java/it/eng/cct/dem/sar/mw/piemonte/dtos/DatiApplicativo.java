package it.eng.cct.dem.sar.mw.piemonte.dtos;

import java.io.Serializable;

/**
 * The Class DatiApplicativo.
 */
public class DatiApplicativo implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** Timestamp nel formato aaaa-mm-ggthh:mm:ss+01 (es. 2009-08-16t12:07:00+01). */
	private String created;

	/** Nome dell'applicativo. */
	private String nome;

	/** Stringa casuale di 20 caratteri alfanumerici. */
	private String nonce;

	/** Produttore dell'applicativo. */
	private String produttore;

	/** Versione dell'applicativo. */
	private String versione;

	/** Stringa di 20 caratteri ottenuta come trasformazione in base64 di (SHA-1(nonce+created+codiceapplicativo)). */
	private String applDigest;

	/**
	 * Instantiates a new dati applicativo.
	 */
	public DatiApplicativo() {
		super();
	}

	/**
	 * Gets the created.
	 * 
	 * @return the created
	 */
	public String getCreated() {
		return created;
	}

	/**
	 * Sets the created.
	 * 
	 * @param created
	 *            the new created
	 */
	public void setCreated(String created) {
		this.created = created;
	}

	/**
	 * Gets the nome.
	 * 
	 * @return the nome
	 */
	public String getNome() {
		return nome;
	}

	/**
	 * Sets the nome.
	 * 
	 * @param nome
	 *            the new nome
	 */
	public void setNome(String nome) {
		this.nome = nome;
	}

	/**
	 * Gets the nonce.
	 * 
	 * @return the nonce
	 */
	public String getNonce() {
		return nonce;
	}

	/**
	 * Sets the nonce.
	 * 
	 * @param nonce
	 *            the new nonce
	 */
	public void setNonce(String nonce) {
		this.nonce = nonce;
	}

	/**
	 * Gets the produttore.
	 * 
	 * @return the produttore
	 */
	public String getProduttore() {
		return produttore;
	}

	/**
	 * Sets the produttore.
	 * 
	 * @param produttore
	 *            the new produttore
	 */
	public void setProduttore(String produttore) {
		this.produttore = produttore;
	}

	/**
	 * Gets the versione.
	 * 
	 * @return the versione
	 */
	public String getVersione() {
		return versione;
	}

	/**
	 * Sets the versione.
	 * 
	 * @param versione
	 *            the new versione
	 */
	public void setVersione(String versione) {
		this.versione = versione;
	}

	/**
	 * Gets the appl digest.
	 * 
	 * @return the appl digest
	 */
	public String getApplDigest() {
		return applDigest;
	}

	/**
	 * Sets the appl digest.
	 * 
	 * @param applDigest
	 *            the new appl digest
	 */
	public void setApplDigest(String applDigest) {
		this.applDigest = applDigest;
	}

	/**
	 * Gets the serialversionuid.
	 * 
	 * @return the serialversionuid
	 */
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	@Override
	public String toString() {
		return "DatiApplicativo [created=" + created + ", nome=" + nome + ", nonce=" + nonce + ", produttore=" + produttore + ", versione=" + versione + ", applDigest=" + applDigest + "]";
	}

}
