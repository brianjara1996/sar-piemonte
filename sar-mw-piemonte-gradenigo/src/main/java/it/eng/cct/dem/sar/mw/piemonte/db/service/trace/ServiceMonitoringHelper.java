package it.eng.cct.dem.sar.mw.piemonte.db.service.trace;

import org.apache.xmlbeans.XmlObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.cct.dem.sar.generic.wsclient.commons.utils.ModuleConfig;
import it.eng.cct.dem.sar.mw.piemonte.constants.MwConstants;
import it.eng.cct.dem.sar.mw.piemonte.xmlbeans.EPrescriptionsDocument;
import it.eng.cct.piemonte.eprescription.types.AnnullamentoDocument;
import it.eng.cct.piemonte.eprescription.types.InterrogaDocument;
import it.eng.cct.piemonte.eprescription.types.RecuperoDocument;
import it.eng.cct.piemonte.eprescription.types.TrasmissioneDocument;
import it.finanze.sanita.dem.dpcm.wsdl.invioTelematicoSanita.service.InputBeanDocument;
import it.finanze.sanita.dem.xsd.annullaerogatorichiesta.AnnullaErogatoRichiestaDocument;
import it.finanze.sanita.dem.xsd.annullaprescrittorichiesta.AnnullaPrescrittoRichiestaDocument;
import it.finanze.sanita.dem.xsd.invioerogatorichiesta.InvioErogatoRichiestaDocument;
import it.finanze.sanita.dem.xsd.invioprescrittorichiesta.InvioPrescrittoRichiestaDocument;
import it.finanze.sanita.dem.xsd.sospendierogatorichiesta.SospendiErogatoRichiestaDocument;
import it.finanze.sanita.dem.xsd.visualizzaerogatorichiesta.VisualizzaErogatoRichiestaDocument;

public class ServiceMonitoringHelper implements Runnable {
	
	private XmlObject mwInputBean;
	private XmlObject mwToServiceBean;
	private XmlObject serviceToMwBean;
	private XmlObject mwOutputBean;
	private Boolean trace = Boolean.FALSE;
	private Boolean traceInternalComs = Boolean.FALSE;
	private String operation = "";
	private static final Logger logger = LoggerFactory.getLogger(ServiceMonitoringHelper.class);

	
	public ServiceMonitoringHelper() {
		super();
		this.trace = ModuleConfig.getPropertyAsBoolean(MwConstants.CONFIG_STATS_ENABLED, false);
		if(this.trace)
			this.traceInternalComs = ModuleConfig.getPropertyAsBoolean(MwConstants.CONFIG_STATS_INTERNAL_MSG_ENABLED, false);
	}

	public ServiceMonitoringHelper(XmlObject mwInputBean, XmlObject mwToServiceBean, XmlObject serviceToMwBean, XmlObject mwOutputBean) {
		super();
		this.mwInputBean = mwInputBean;
		this.mwToServiceBean = mwToServiceBean;
		this.serviceToMwBean = serviceToMwBean;
		this.mwOutputBean = mwOutputBean;
		this.trace = ModuleConfig.getPropertyAsBoolean(MwConstants.CONFIG_STATS_ENABLED, false);
		if(this.trace)
			this.traceInternalComs = ModuleConfig.getPropertyAsBoolean(MwConstants.CONFIG_STATS_INTERNAL_MSG_ENABLED, false);
	}



	public void run() {
		if(this.trace){
//			Servizio Invio Erogato
			if(mwInputBean instanceof InvioErogatoRichiestaDocument){
				InvioErogatoTrace invioErogatoService = new InvioErogatoTrace(mwInputBean, mwOutputBean, this.traceInternalComs);
				invioErogatoService.trace();
			}
//			Servizio Annulla Erogato
			else if(mwInputBean instanceof AnnullaErogatoRichiestaDocument){
				AnnullaErogatoTrace annullaErogatoTrace = new AnnullaErogatoTrace(mwInputBean, mwOutputBean, this.traceInternalComs);
				annullaErogatoTrace.trace();
			}else if(mwInputBean instanceof SospendiErogatoRichiestaDocument){
				SospendiErogatoTrace sospendiErogatoTrace = new SospendiErogatoTrace(mwInputBean, mwOutputBean, this.traceInternalComs);
				sospendiErogatoTrace.trace();
			}else if(mwInputBean instanceof InvioErogatoRichiestaDocument){
				InvioErogatoTrace invioErogatoTrace = new InvioErogatoTrace(mwInputBean, mwOutputBean, this.traceInternalComs);
				invioErogatoTrace.trace();
			}else if(mwInputBean instanceof VisualizzaErogatoRichiestaDocument){
				VisualizzaErogatoTrace visualizzaErogatoTrace = new VisualizzaErogatoTrace(mwInputBean, mwOutputBean, this.traceInternalComs);
				visualizzaErogatoTrace.trace();
			}else if(mwInputBean instanceof AnnullamentoDocument && mwToServiceBean instanceof AnnullaPrescrittoRichiestaDocument){
				EPrescAnnullaPrescrittoTrace ePrescAnnullaPrescritto = new EPrescAnnullaPrescrittoTrace(mwInputBean, mwOutputBean, mwToServiceBean, serviceToMwBean, this.traceInternalComs);
				ePrescAnnullaPrescritto.trace();
			}else if(mwInputBean instanceof AnnullamentoDocument && mwToServiceBean instanceof InputBeanDocument){
				InvioTelematicoSanitaAnnullamento annullaTelematicoPrescrittoTrace = new InvioTelematicoSanitaAnnullamento(mwInputBean, mwOutputBean, mwToServiceBean, serviceToMwBean, this.traceInternalComs);
				annullaTelematicoPrescrittoTrace.trace();
			}else if(mwInputBean instanceof TrasmissioneDocument && mwToServiceBean instanceof EPrescriptionsDocument){
				InvioTelematicoSanitaTrasmissione ePrescTrasmissionePrescrittoTrace = new InvioTelematicoSanitaTrasmissione(mwInputBean, mwOutputBean, mwToServiceBean, serviceToMwBean, this.traceInternalComs);
				ePrescTrasmissionePrescrittoTrace.trace();
			}else if(mwInputBean instanceof TrasmissioneDocument && mwToServiceBean instanceof InvioPrescrittoRichiestaDocument){
				EPrescInvioPrescrittoTrace invioTelematicoPrescrittoTrace = new EPrescInvioPrescrittoTrace(mwInputBean, mwOutputBean, mwToServiceBean, serviceToMwBean, this.traceInternalComs);
				invioTelematicoPrescrittoTrace.trace();
			}else if(mwInputBean instanceof RecuperoDocument){
				EPrescVisualizzaPrescrittoTrace recuperoPrescrittoTrace = new EPrescVisualizzaPrescrittoTrace(mwInputBean, mwOutputBean, mwToServiceBean, serviceToMwBean, this.traceInternalComs);
				recuperoPrescrittoTrace.trace();
			}else if(mwInputBean instanceof InterrogaDocument){
				EPrescInterrogaNreUtilTrace interrogaTrace = new EPrescInterrogaNreUtilTrace(mwInputBean, mwOutputBean, mwToServiceBean, serviceToMwBean, this.traceInternalComs);
				interrogaTrace.trace();
			}
		}

	}
	
	
	
	
	
	
	




	public XmlObject getMwInputBean() {
		return mwInputBean;
	}



	public void setMwInputBean(XmlObject mwInputBean) {
		this.mwInputBean = mwInputBean;
	}



	public XmlObject getMwToServiceBean() {
		return mwToServiceBean;
	}



	public void setMwToServiceBean(XmlObject mwToServiceBean) {
		this.mwToServiceBean = mwToServiceBean;
	}



	public XmlObject getServiceToMwBean() {
		return serviceToMwBean;
	}



	public void setServiceToMwBean(XmlObject serviceToMwBean) {
		this.serviceToMwBean = serviceToMwBean;
	}



	public XmlObject getMwOutputBean() {
		return mwOutputBean;
	}



	public void setMwOutputBean(XmlObject mwOutputBean) {
		this.mwOutputBean = mwOutputBean;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}


}
