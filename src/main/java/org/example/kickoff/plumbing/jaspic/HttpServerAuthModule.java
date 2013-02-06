package org.example.kickoff.plumbing.jaspic;

import static javax.security.auth.message.AuthStatus.SEND_SUCCESS;

import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.AuthStatus;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.MessagePolicy;
import javax.security.auth.message.config.ServerAuthContext;
import javax.security.auth.message.module.ServerAuthModule;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A server authentication module (SAM) implementation base class, tailored for the Servlet Container Profile.
 * 
 * @author Arjan Tijms
 * 
 */
public abstract class HttpServerAuthModule implements ServerAuthModule {

	private CallbackHandler handler;
	private final Class<?>[] supportedMessageTypes = new Class[] { HttpServletRequest.class, HttpServletResponse.class };

	@Override
	public void initialize(MessagePolicy requestPolicy, MessagePolicy responsePolicy, CallbackHandler handler,
			@SuppressWarnings("rawtypes") Map options) throws AuthException {
		this.handler = handler;
	}

	/**
	 * A Servlet Container Profile compliant implementation should return HttpServletRequest and HttpServletResponse, so
	 * the delegation class {@link ServerAuthContext} can choose the right SAM to delegate to.
	 */
	@Override
	public Class<?>[] getSupportedMessageTypes() {
		return supportedMessageTypes;
	}
	
	@Override
	public AuthStatus validateRequest(MessageInfo messageInfo, Subject clientSubject, Subject serviceSubject) throws AuthException {
		
		// Cast the request and response messages. Because of our getSupportedMessageTypes, they have to be of the correct
		// types and casting should always work.
		HttpServletRequest request = (HttpServletRequest) messageInfo.getRequestMessage();
		HttpServletResponse response = (HttpServletResponse) messageInfo.getResponseMessage();
		
		AuthStatus status = validateHttpRequest(request, response, clientSubject, handler);
		if (status == AuthStatus.FAILURE) {
			throw new IllegalStateException("Servlet Container Profile SAM should not return status FAILURE. This is for CLIENT SAMs only");
		}
		
		return status;
	}
	
	/**
	 * WebLogic 12c calls this before Servlet is called, Geronimo v3 after, JBoss EAP 6 and GlassFish 3.1.2.2 don't call this at all. WebLogic
	 * (seemingly) only continues if SEND_SUCCESS is returned, Geronimo completely ignores return value.
	 */
	@Override
	public AuthStatus secureResponse(MessageInfo messageInfo, Subject serviceSubject) throws AuthException {
		return SEND_SUCCESS;
	}

	@Override
	public void cleanSubject(MessageInfo messageInfo, Subject subject) throws AuthException {
		if (subject != null) {
			subject.getPrincipals().clear();
		}
	}
	
	public AuthStatus validateHttpRequest(HttpServletRequest request, HttpServletResponse response, MessageInfo messageInfo, Subject clientSubject, Subject serviceSubject, CallbackHandler handler) {
		return validateHttpRequest(request, response, clientSubject, handler);
	}
	
	public AuthStatus validateHttpRequest(HttpServletRequest request, HttpServletResponse response, Subject clientSubject, CallbackHandler handler) {
		throw new IllegalStateException("Not implemented");
	}

}