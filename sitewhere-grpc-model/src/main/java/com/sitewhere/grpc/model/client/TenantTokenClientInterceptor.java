package com.sitewhere.grpc.model.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.sitewhere.grpc.model.spi.security.ITenantAwareAuthentication;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall.SimpleForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;

/**
 * GRPC interceptor that pushes tenant token into GRPC call metadata.
 * 
 * @author Derek
 */
public class TenantTokenClientInterceptor implements ClientInterceptor {

    /** Static logger instance */
    private static Logger LOGGER = LogManager.getLogger();

    /** Tenant token metadata key */
    public static final Metadata.Key<String> TENANT_TOKEN_KEY = Metadata.Key.of("tenant",
	    Metadata.ASCII_STRING_MARSHALLER);

    /*
     * (non-Javadoc)
     * 
     * @see io.grpc.ClientInterceptor#interceptCall(io.grpc.MethodDescriptor,
     * io.grpc.CallOptions, io.grpc.Channel)
     */
    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method,
	    CallOptions callOptions, Channel next) {
	return new SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {

	    /*
	     * (non-Javadoc)
	     * 
	     * @see
	     * io.grpc.ForwardingClientCall#start(io.grpc.ClientCall.Listener,
	     * io.grpc.Metadata)
	     */
	    @Override
	    public void start(Listener<RespT> responseListener, Metadata headers) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if ((authentication != null) && (authentication instanceof ITenantAwareAuthentication)) {
		    String tenantToken = ((ITenantAwareAuthentication) authentication).getTenantToken();
		    if (tenantToken != null) {
			headers.put(TENANT_TOKEN_KEY, tenantToken);
		    }
		}
		super.start(responseListener, headers);
	    }
	};
    }
}