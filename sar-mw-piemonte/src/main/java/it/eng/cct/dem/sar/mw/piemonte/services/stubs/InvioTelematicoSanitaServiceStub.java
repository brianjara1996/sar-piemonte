package it.eng.cct.dem.sar.mw.piemonte.services.stubs;

import it.finanze.sanita.dem.dpcm.wsdl.invioTelematicoSanita.service.InvioTelematicoSanitaServiceCallbackHandler;

import org.apache.axiom.attachments.ConfigurableDataHandler;
import org.apache.axis2.Constants;

/*
*  InvioTelematicoSanitaServiceStub java implementation
*/

public class InvioTelematicoSanitaServiceStub extends org.apache.axis2.client.Stub {
	protected org.apache.axis2.description.AxisOperation[] _operations;

	//hashmaps to keep the fault mapping
	private java.util.HashMap faultExceptionNameMap = new java.util.HashMap();
	private java.util.HashMap faultExceptionClassNameMap = new java.util.HashMap();
	private java.util.HashMap faultMessageMap = new java.util.HashMap();

	private static int counter = 0;

	private static synchronized java.lang.String getUniqueSuffix() {
		// reset the counter if it is greater than 99999
		if (counter > 99999) {
			counter = 0;
		}
		counter = counter + 1;
		return java.lang.Long.toString(java.lang.System.currentTimeMillis()) + "_" + counter;
	}

	private void populateAxisService() throws org.apache.axis2.AxisFault {

		//creating the Service with a unique name
		_service = new org.apache.axis2.description.AxisService("InvioTelematicoSanitaService" + getUniqueSuffix());
		addAnonymousOperations();

		//creating the operations
		org.apache.axis2.description.AxisOperation __operation;

		_operations = new org.apache.axis2.description.AxisOperation[1];

		__operation = new org.apache.axis2.description.OutInAxisOperation();

		__operation.setName(new javax.xml.namespace.QName("urn:attachment.ws.it", "inviaFileSanita"));
		_service.addOperation(__operation);

		_operations[0] = __operation;

	}

	//populates the faults
	private void populateFaults() {

	}

	/**
	  *Constructor that takes in a configContext
	  */

	public InvioTelematicoSanitaServiceStub(org.apache.axis2.context.ConfigurationContext configurationContext, java.lang.String targetEndpoint) throws org.apache.axis2.AxisFault {
		this(configurationContext, targetEndpoint, false);
	}

	/**
	  * Constructor that takes in a configContext  and useseperate listner
	  */
	public InvioTelematicoSanitaServiceStub(org.apache.axis2.context.ConfigurationContext configurationContext, java.lang.String targetEndpoint, boolean useSeparateListener) throws org.apache.axis2.AxisFault {
		//To populate AxisService
		populateAxisService();
		populateFaults();

		_serviceClient = new org.apache.axis2.client.ServiceClient(configurationContext, _service);

		_serviceClient.getOptions().setTo(new org.apache.axis2.addressing.EndpointReference(targetEndpoint));
		_serviceClient.getOptions().setUseSeparateListener(useSeparateListener);

	}

	/**
	 * Default Constructor
	 */
	public InvioTelematicoSanitaServiceStub(org.apache.axis2.context.ConfigurationContext configurationContext) throws org.apache.axis2.AxisFault {

		this(configurationContext, "https://mirsac.sanita.finanze.it/invioTelematicoSwaWeb/services/invioTelematicoSanita");

	}

	/**
	 * Default Constructor
	 */
	public InvioTelematicoSanitaServiceStub() throws org.apache.axis2.AxisFault {

		this("https://mirsac.sanita.finanze.it/invioTelematicoSwaWeb/services/invioTelematicoSanita");

	}

	/**
	 * Constructor taking the target endpoint
	 */
	public InvioTelematicoSanitaServiceStub(java.lang.String targetEndpoint) throws org.apache.axis2.AxisFault {
		this(null, targetEndpoint);
	}

	/**
	 * Auto generated method signature
	 * 
	 * @see it.finanze.sanita.dpcm.wsdl.invioTelematicoSanita.service.InvioTelematicoSanitaService#inviaFileSanita
	 * @param inputBean10
	
	 */

	public it.finanze.sanita.dem.dpcm.wsdl.invioTelematicoSanita.service.RicevutaDocument inviaFileSanita(

	it.finanze.sanita.dem.dpcm.wsdl.invioTelematicoSanita.service.InputBeanDocument inputBean10, ConfigurableDataHandler attachment, String uuid)

	throws java.rmi.RemoteException

	{
		org.apache.axis2.context.MessageContext _messageContext = null;
		try {
			org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[0].getName());
			_operationClient.getOptions().setAction("invio");
			_operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);
			_operationClient.getOptions().setProperty(Constants.Configuration.ENABLE_SWA, Constants.VALUE_TRUE);
			addPropertyToOperationClient(_operationClient, org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR, "&");

			// create a message context
			_messageContext = new org.apache.axis2.context.MessageContext();

			// create SOAP envelope with that payload
			org.apache.axiom.soap.SOAPEnvelope env = null;

			env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()), inputBean10, optimizeContent(new javax.xml.namespace.QName("urn:attachment.ws.it", "inviaFileSanita")), new javax.xml.namespace.QName("urn:attachment.ws.it", "inviaFileSanita"));

			//adding SOAP soap_headers
			_serviceClient.addHeadersToEnvelope(env);
			// set the message context with that soap envelope
			_messageContext.setEnvelope(env);
			attachment.setTransferEncoding("binary");
			attachment.setContentType("application/zip");
			_messageContext.addAttachment("zipAttachment=" + uuid, attachment);
			// add the message contxt to the operation client
			_operationClient.addMessageContext(_messageContext);

			//execute the operation client
			_operationClient.execute(true);

			org.apache.axis2.context.MessageContext _returnMessageContext = _operationClient.getMessageContext(org.apache.axis2.wsdl.WSDLConstants.MESSAGE_LABEL_IN_VALUE);
			org.apache.axiom.soap.SOAPEnvelope _returnEnv = _returnMessageContext.getEnvelope();

			java.lang.Object object = fromOM(_returnEnv.getBody().getFirstElement(), it.finanze.sanita.dem.dpcm.wsdl.invioTelematicoSanita.service.RicevutaDocument.class, getEnvelopeNamespaces(_returnEnv));

			return (it.finanze.sanita.dem.dpcm.wsdl.invioTelematicoSanita.service.RicevutaDocument) object;

		} catch (org.apache.axis2.AxisFault f) {

			org.apache.axiom.om.OMElement faultElt = f.getDetail();
			if (faultElt != null) {
				if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(), "inviaFileSanita"))) {
					//make the fault by reflection
					try {
						java.lang.String exceptionClassName = (java.lang.String) faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(), "inviaFileSanita"));
						java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
						java.lang.reflect.Constructor constructor = exceptionClass.getConstructor(String.class);
						java.lang.Exception ex = (java.lang.Exception) constructor.newInstance(f.getMessage());
						//message class
						java.lang.String messageClassName = (java.lang.String) faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(), "inviaFileSanita"));
						java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
						java.lang.Object messageObject = fromOM(faultElt, messageClass, null);
						java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage", new java.lang.Class[] { messageClass });
						m.invoke(ex, new java.lang.Object[] { messageObject });

						throw new java.rmi.RemoteException(ex.getMessage(), ex);
					} catch (java.lang.ClassCastException e) {
						// we cannot intantiate the class - throw the original Axis fault
						throw f;
					} catch (java.lang.ClassNotFoundException e) {
						// we cannot intantiate the class - throw the original Axis fault
						throw f;
					} catch (java.lang.NoSuchMethodException e) {
						// we cannot intantiate the class - throw the original Axis fault
						throw f;
					} catch (java.lang.reflect.InvocationTargetException e) {
						// we cannot intantiate the class - throw the original Axis fault
						throw f;
					} catch (java.lang.IllegalAccessException e) {
						// we cannot intantiate the class - throw the original Axis fault
						throw f;
					} catch (java.lang.InstantiationException e) {
						// we cannot intantiate the class - throw the original Axis fault
						throw f;
					}
				} else {
					throw f;
				}
			} else {
				throw f;
			}
		} finally {
			if (_messageContext.getTransportOut() != null) {
				_messageContext.getTransportOut().getSender().cleanup(_messageContext);
			}
		}
	}

	/**
	* Auto generated method signature for Asynchronous Invocations
	* 
	* @see it.finanze.sanita.dpcm.wsdl.invioTelematicoSanita.service.InvioTelematicoSanitaService#startinviaFileSanita
	    * @param inputBean10
	
	*/
	public void startinviaFileSanita(

	it.finanze.sanita.dem.dpcm.wsdl.invioTelematicoSanita.service.InputBeanDocument inputBean10,

	final InvioTelematicoSanitaServiceCallbackHandler callback)

	throws java.rmi.RemoteException {

		org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[0].getName());
		_operationClient.getOptions().setAction("invio");
		_operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

		addPropertyToOperationClient(_operationClient, org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR, "&");

		// create SOAP envelope with that payload
		org.apache.axiom.soap.SOAPEnvelope env = null;
		final org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext();

		//Style is Doc.

		env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()), inputBean10, optimizeContent(new javax.xml.namespace.QName("urn:attachment.ws.it", "inviaFileSanita")), new javax.xml.namespace.QName("urn:attachment.ws.it", "inviaFileSanita"));

		// adding SOAP soap_headers
		_serviceClient.addHeadersToEnvelope(env);
		// create message context with that soap envelope
		_messageContext.setEnvelope(env);

		// add the message context to the operation client
		_operationClient.addMessageContext(_messageContext);

		_operationClient.setCallback(new org.apache.axis2.client.async.AxisCallback() {
			public void onMessage(org.apache.axis2.context.MessageContext resultContext) {
				try {
					org.apache.axiom.soap.SOAPEnvelope resultEnv = resultContext.getEnvelope();

					java.lang.Object object = fromOM(resultEnv.getBody().getFirstElement(), it.finanze.sanita.dem.dpcm.wsdl.invioTelematicoSanita.service.RicevutaDocument.class, getEnvelopeNamespaces(resultEnv));
					callback.receiveResultinviaFileSanita((it.finanze.sanita.dem.dpcm.wsdl.invioTelematicoSanita.service.RicevutaDocument) object);

				} catch (org.apache.axis2.AxisFault e) {
					callback.receiveErrorinviaFileSanita(e);
				}
			}

			public void onError(java.lang.Exception error) {
				if (error instanceof org.apache.axis2.AxisFault) {
					org.apache.axis2.AxisFault f = (org.apache.axis2.AxisFault) error;
					org.apache.axiom.om.OMElement faultElt = f.getDetail();
					if (faultElt != null) {
						if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(), "inviaFileSanita"))) {
							//make the fault by reflection
							try {
								java.lang.String exceptionClassName = (java.lang.String) faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(), "inviaFileSanita"));
								java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
								java.lang.reflect.Constructor constructor = exceptionClass.getConstructor(String.class);
								java.lang.Exception ex = (java.lang.Exception) constructor.newInstance(f.getMessage());
								//message class
								java.lang.String messageClassName = (java.lang.String) faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(), "inviaFileSanita"));
								java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
								java.lang.Object messageObject = fromOM(faultElt, messageClass, null);
								java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage", new java.lang.Class[] { messageClass });
								m.invoke(ex, new java.lang.Object[] { messageObject });

								callback.receiveErrorinviaFileSanita(new java.rmi.RemoteException(ex.getMessage(), ex));
							} catch (java.lang.ClassCastException e) {
								// we cannot intantiate the class - throw the original Axis fault
								callback.receiveErrorinviaFileSanita(f);
							} catch (java.lang.ClassNotFoundException e) {
								// we cannot intantiate the class - throw the original Axis fault
								callback.receiveErrorinviaFileSanita(f);
							} catch (java.lang.NoSuchMethodException e) {
								// we cannot intantiate the class - throw the original Axis fault
								callback.receiveErrorinviaFileSanita(f);
							} catch (java.lang.reflect.InvocationTargetException e) {
								// we cannot intantiate the class - throw the original Axis fault
								callback.receiveErrorinviaFileSanita(f);
							} catch (java.lang.IllegalAccessException e) {
								// we cannot intantiate the class - throw the original Axis fault
								callback.receiveErrorinviaFileSanita(f);
							} catch (java.lang.InstantiationException e) {
								// we cannot intantiate the class - throw the original Axis fault
								callback.receiveErrorinviaFileSanita(f);
							} catch (org.apache.axis2.AxisFault e) {
								// we cannot intantiate the class - throw the original Axis fault
								callback.receiveErrorinviaFileSanita(f);
							}
						} else {
							callback.receiveErrorinviaFileSanita(f);
						}
					} else {
						callback.receiveErrorinviaFileSanita(f);
					}
				} else {
					callback.receiveErrorinviaFileSanita(error);
				}
			}

			public void onFault(org.apache.axis2.context.MessageContext faultContext) {
				org.apache.axis2.AxisFault fault = org.apache.axis2.util.Utils.getInboundFaultFromMessageContext(faultContext);
				onError(fault);
			}

			public void onComplete() {
				try {
					_messageContext.getTransportOut().getSender().cleanup(_messageContext);
				} catch (org.apache.axis2.AxisFault axisFault) {
					callback.receiveErrorinviaFileSanita(axisFault);
				}
			}
		});

		org.apache.axis2.util.CallbackReceiver _callbackReceiver = null;
		if (_operations[0].getMessageReceiver() == null && _operationClient.getOptions().isUseSeparateListener()) {
			_callbackReceiver = new org.apache.axis2.util.CallbackReceiver();
			_operations[0].setMessageReceiver(_callbackReceiver);
		}

		//execute the operation client
		_operationClient.execute(false);

	}

	/**
	 *  A utility method that copies the namepaces from the SOAPEnvelope
	 */
	private java.util.Map getEnvelopeNamespaces(org.apache.axiom.soap.SOAPEnvelope env) {
		java.util.Map returnMap = new java.util.HashMap();
		java.util.Iterator namespaceIterator = env.getAllDeclaredNamespaces();
		while (namespaceIterator.hasNext()) {
			org.apache.axiom.om.OMNamespace ns = (org.apache.axiom.om.OMNamespace) namespaceIterator.next();
			returnMap.put(ns.getPrefix(), ns.getNamespaceURI());
		}
		return returnMap;
	}

	private javax.xml.namespace.QName[] opNameArray = null;

	private boolean optimizeContent(javax.xml.namespace.QName opName) {

		if (opNameArray == null) {
			return false;
		}
		for (int i = 0; i < opNameArray.length; i++) {
			if (opName.equals(opNameArray[i])) {
				return true;
			}
		}
		return false;
	}

	//https://mirsac.sanita.finanze.it/invioTelematicoSwaWeb/services/invioTelematicoSanita

	private org.apache.axiom.om.OMElement toOM(it.finanze.sanita.dem.dpcm.wsdl.invioTelematicoSanita.service.InputBeanDocument param, boolean optimizeContent) throws org.apache.axis2.AxisFault {

		return toOM(param);

	}

	private org.apache.axiom.om.OMElement toOM(final it.finanze.sanita.dem.dpcm.wsdl.invioTelematicoSanita.service.InputBeanDocument param) throws org.apache.axis2.AxisFault {

		org.apache.xmlbeans.XmlOptions xmlOptions = new org.apache.xmlbeans.XmlOptions();
		xmlOptions.setSaveNoXmlDecl();
		xmlOptions.setSaveAggressiveNamespaces();
		xmlOptions.setSaveNamespacesFirst();
		org.apache.axiom.om.OMXMLParserWrapper builder = org.apache.axiom.om.OMXMLBuilderFactory.createOMBuilder(new javax.xml.transform.sax.SAXSource(new org.apache.axis2.xmlbeans.XmlBeansXMLReader(param, xmlOptions), new org.xml.sax.InputSource()));
		try {
			return builder.getDocumentElement(true);
		} catch (java.lang.Exception e) {
			throw org.apache.axis2.AxisFault.makeFault(e);
		}
	}

	private org.apache.axiom.om.OMElement toOM(it.finanze.sanita.dem.dpcm.wsdl.invioTelematicoSanita.service.RicevutaDocument param, boolean optimizeContent) throws org.apache.axis2.AxisFault {

		return toOM(param);

	}

	private org.apache.axiom.om.OMElement toOM(final it.finanze.sanita.dem.dpcm.wsdl.invioTelematicoSanita.service.RicevutaDocument param) throws org.apache.axis2.AxisFault {

		org.apache.xmlbeans.XmlOptions xmlOptions = new org.apache.xmlbeans.XmlOptions();
		xmlOptions.setSaveNoXmlDecl();
		xmlOptions.setSaveAggressiveNamespaces();
		xmlOptions.setSaveNamespacesFirst();
		org.apache.axiom.om.OMXMLParserWrapper builder = org.apache.axiom.om.OMXMLBuilderFactory.createOMBuilder(new javax.xml.transform.sax.SAXSource(new org.apache.axis2.xmlbeans.XmlBeansXMLReader(param, xmlOptions), new org.xml.sax.InputSource()));
		try {
			return builder.getDocumentElement(true);
		} catch (java.lang.Exception e) {
			throw org.apache.axis2.AxisFault.makeFault(e);
		}
	}

	private org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, it.finanze.sanita.dem.dpcm.wsdl.invioTelematicoSanita.service.InputBeanDocument param, boolean optimizeContent, javax.xml.namespace.QName methodQName) throws org.apache.axis2.AxisFault {
		org.apache.axiom.soap.SOAPEnvelope envelope = factory.getDefaultEnvelope();
		if (param != null) {
			envelope.getBody().addChild(toOM(param, optimizeContent));
		}
		return envelope;
	}

	/**
	*  get the default envelope
	*/
	private org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory) {
		return factory.getDefaultEnvelope();
	}

	public org.apache.xmlbeans.XmlObject fromOM(org.apache.axiom.om.OMElement param, java.lang.Class type, java.util.Map extraNamespaces) throws org.apache.axis2.AxisFault {
		try {

			if (it.finanze.sanita.dem.dpcm.wsdl.invioTelematicoSanita.service.InputBeanDocument.class.equals(type)) {
				if (extraNamespaces != null) {
					return it.finanze.sanita.dem.dpcm.wsdl.invioTelematicoSanita.service.InputBeanDocument.Factory.parse(param.getXMLStreamReaderWithoutCaching(), new org.apache.xmlbeans.XmlOptions().setLoadAdditionalNamespaces(extraNamespaces));
				} else {
					return it.finanze.sanita.dem.dpcm.wsdl.invioTelematicoSanita.service.InputBeanDocument.Factory.parse(param.getXMLStreamReaderWithoutCaching());
				}
			}

			if (it.finanze.sanita.dem.dpcm.wsdl.invioTelematicoSanita.service.RicevutaDocument.class.equals(type)) {
				if (extraNamespaces != null) {
					return it.finanze.sanita.dem.dpcm.wsdl.invioTelematicoSanita.service.RicevutaDocument.Factory.parse(param.getXMLStreamReaderWithoutCaching(), new org.apache.xmlbeans.XmlOptions().setLoadAdditionalNamespaces(extraNamespaces));
				} else {
					return it.finanze.sanita.dem.dpcm.wsdl.invioTelematicoSanita.service.RicevutaDocument.Factory.parse(param.getXMLStreamReaderWithoutCaching());
				}
			}

		} catch (java.lang.Exception e) {
			throw org.apache.axis2.AxisFault.makeFault(e);
		}
		return null;
	}

}
