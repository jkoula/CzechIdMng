package eu.bcvsolutions.idm.core.security.auth.filter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.modelmapper.internal.util.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.filter.GenericFilterBean;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.utils.HttpFilterUtils;
import eu.bcvsolutions.idm.core.security.api.filter.IdmAuthenticationFilter;
import eu.bcvsolutions.idm.core.security.api.service.EnabledEvaluator;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;

/**
 * Base class for Idm authentication filters. All authentication
 * filters are called from this class, which handles the HTTP request
 * and authentication flow.
 * 
 * @author Jan Helbich
 *
 */
public class AuthenticationFilter extends GenericFilterBean {

	private SecurityService securityService;
	private List<IdmAuthenticationFilter> filters;
	private EnabledEvaluator enabledEvaluator;

	@Autowired
	public AuthenticationFilter(List<IdmAuthenticationFilter> filters, SecurityService securityService,
								EnabledEvaluator enabledEvaluator) {
		Assert.notNull(filters);
		Assert.notNull(securityService);
		Assert.notNull(enabledEvaluator);
		//
		this.filters = filters.stream().filter(f -> enabledEvaluator.isEnabled(f)).collect(Collectors.toList());
		this.securityService = securityService;
	}

	/**
	 * Authentication flow implementation.
	 */
	@Override
	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain chain) throws IOException, ServletException {
		
		HttpServletRequest request = HttpFilterUtils.asHttp(req);
		HttpServletResponse response = HttpFilterUtils.asHttp(res);
		filters.stream()
			.filter(f -> isAuthenticated()
					|| res.isCommitted()
					|| handleAuthenticationHeader(request, response, f))
			.findFirst();
		
		if (!res.isCommitted()) {
			chain.doFilter(req, res);
		}
	}

	/**
	 * Generic method for parsing the authentication token out of HTTP request.
	 * If specified token is found, it is passed to the authentication method
	 * of specific filter (implementing {@link IdmAuthenticationFilter}).
	 * @param req
	 * @param res
	 * @return
	 */
	protected boolean handleAuthenticationHeader(HttpServletRequest req, HttpServletResponse res,
			IdmAuthenticationFilter filter) {
		
		// transform the token from all matching auth. headers and find first successful
		Optional<String> succToken = HttpFilterUtils
				.filterTransformHeaders(getAuthHeader(req, filter), filter.getAuthorizationHeaderPrefix())
				.stream()
			.filter(token -> filter.authorize(token, req, res))
			.findFirst();
		
		return succToken.isPresent();
	}

	protected SecurityService getSecurityService() {
		return securityService;
	}

	protected List<IdmAuthenticationFilter> getFilters() {
		return Collections.unmodifiableList(Lists.newArrayList(filters));
	}
	
	private boolean isAuthenticated() {
		return securityService.isAuthenticated();
	}
	
	private List<String> getAuthHeader(HttpServletRequest request, IdmAuthenticationFilter filter) {
		return HttpFilterUtils.getHeaders(request, filter.getAuthorizationHeaderName());
	}
	
}
