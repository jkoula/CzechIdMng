package eu.bcvsolutions.idm.core.security.auth.filter;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.cas.client.authentication.AttributePrincipalImpl;
import org.jasig.cas.client.validation.AssertionImpl;
import org.jasig.cas.client.validation.TicketValidationException;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.CoreModuleDescriptor;
import eu.bcvsolutions.idm.core.api.config.domain.CasConfiguration;
import eu.bcvsolutions.idm.core.api.domain.IdentityState;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;
import eu.bcvsolutions.idm.core.security.api.exception.TwoFactorAuthenticationRequiredException;
import eu.bcvsolutions.idm.core.security.api.service.CasValidationService;
import eu.bcvsolutions.idm.core.security.api.service.JwtAuthenticationService;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;

/**
 * Test authentication using CAS.
 *
 * @author Roman Kučera
 * @author Radek Tomiška
 */
@Transactional
public class CasAuthenticationFilterIntegrationTest extends AbstractUnitTest {

	private String TEST_TOKEN = "token-123";
	private String CAS_USER = "casSsoTestUser";

	private String CAS_URL = "http://cas/cas";
	private String IDM_URL = "http://idm/idm";

	@Mock private AuthenticationExceptionContext ctx;
	@Mock private CasValidationService casValidationService;
	@Mock private IdmIdentityService identityService;
	@Mock private JwtAuthenticationService jwtAuthenticationService;
	@Mock private CasConfiguration casConfiguration;

	@InjectMocks
	private CasAuthenticationFilter casAuthenticationFilter;

	@Test
	public void testAuthorizeSuccess() throws Exception {
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

		IdmIdentityDto idmIdentityDto = new IdmIdentityDto(CAS_USER);
		LoginDto loginDto = new LoginDto(idmIdentityDto);

		AttributePrincipalImpl attributePrincipal = new AttributePrincipalImpl(CAS_USER);
		AssertionImpl assertion = new AssertionImpl(attributePrincipal);

		Mockito.when(casConfiguration.getUrl()).thenReturn(CAS_URL);
		Mockito.when(casConfiguration.getService(request, true)).thenReturn(IDM_URL);

		Mockito.when(casValidationService.validate(TEST_TOKEN, IDM_URL, CAS_URL)).thenReturn(assertion);
		Mockito.when(identityService.getByUsername(CAS_USER)).thenReturn(idmIdentityDto);

		Mockito.when(jwtAuthenticationService.createJwtAuthenticationAndAuthenticate(Mockito.any(LoginDto.class), Mockito.eq(idmIdentityDto),
				Mockito.eq(CoreModuleDescriptor.MODULE_ID))).thenReturn(loginDto);

		boolean authorizeResult = casAuthenticationFilter.authorize(TEST_TOKEN, request, response);

		Assert.assertTrue(authorizeResult);
	}
	
	@Test
	public void testAuthorizeAssertionFailed() throws Exception {
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

		AttributePrincipalImpl attributePrincipal = new AttributePrincipalImpl(CAS_USER);
		AssertionImpl assertion = new AssertionImpl(attributePrincipal, Date.from(ZonedDateTime.now().plusDays(1).toInstant()), null, null, new HashMap<>());

		Mockito.when(casConfiguration.getUrl()).thenReturn(CAS_URL);
		Mockito.when(casConfiguration.getService(request, true)).thenReturn(IDM_URL);

		Mockito.when(casValidationService.validate(TEST_TOKEN, IDM_URL, CAS_URL)).thenReturn(assertion);

		boolean authorizeResult = casAuthenticationFilter.authorize(TEST_TOKEN, request, response);

		Assert.assertFalse(authorizeResult);
	}

	@Test
	public void testAuthorizeFailedNoConfProperties() {
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

		boolean authorizeResult = casAuthenticationFilter.authorize(TEST_TOKEN, request, response);

		Assert.assertFalse(authorizeResult);
	}

	@Test
	public void testAuthorizeFailedNoToken() {
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

		Mockito.when(casConfiguration.getUrl()).thenReturn(CAS_URL);
		Mockito.when(casConfiguration.getService(request, true)).thenReturn(IDM_URL);

		boolean authorizeResult = casAuthenticationFilter.authorize(null, request, response);

		Assert.assertFalse(authorizeResult);
	}

	@Test
	public void testAuthorizeFailedAssertionNull() {
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

		Mockito.when(casConfiguration.getUrl()).thenReturn(CAS_URL);
		Mockito.when(casConfiguration.getService(request, true)).thenReturn(IDM_URL);

		boolean authorizeResult = casAuthenticationFilter.authorize("Unknowntoken", request, response);

		Assert.assertFalse(authorizeResult);
	}

	@Test
	public void testAuthorizeFailedAssertionException() throws TicketValidationException {
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

		Mockito.when(casConfiguration.getUrl()).thenReturn(CAS_URL);
		Mockito.when(casConfiguration.getService(request, true)).thenReturn(IDM_URL);

		Mockito.when(casValidationService.validate(TEST_TOKEN, IDM_URL, CAS_URL)).thenThrow(TicketValidationException.class);

		boolean authorizeResult = casAuthenticationFilter.authorize(TEST_TOKEN, request, response);

		Assert.assertFalse(authorizeResult);
	}

	@Test()
	public void testAuthorizeFailedNullIdentity() throws Exception {
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

		AttributePrincipalImpl attributePrincipal = new AttributePrincipalImpl(CAS_USER);
		AssertionImpl assertion = new AssertionImpl(attributePrincipal);

		Mockito.when(casConfiguration.getUrl()).thenReturn(CAS_URL);
		Mockito.when(casConfiguration.getService(request, true)).thenReturn(IDM_URL);

		Mockito.when(casValidationService.validate(TEST_TOKEN, IDM_URL, CAS_URL)).thenReturn(assertion);
		Mockito.when(identityService.getByUsername(CAS_USER)).thenReturn(null);

		boolean authorizeResult = casAuthenticationFilter.authorize(TEST_TOKEN, request, response);

		Assert.assertFalse(authorizeResult);
	}
	
	@Test()
	public void testAuthorizeFailedDisabledIdentity() throws Exception {
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

		AttributePrincipalImpl attributePrincipal = new AttributePrincipalImpl(CAS_USER);
		AssertionImpl assertion = new AssertionImpl(attributePrincipal);

		Mockito.when(casConfiguration.getUrl()).thenReturn(CAS_URL);
		Mockito.when(casConfiguration.getService(request, true)).thenReturn(IDM_URL);

		Mockito.when(casValidationService.validate(TEST_TOKEN, IDM_URL, CAS_URL)).thenReturn(assertion);
		IdmIdentityDto idmIdentityDto = new IdmIdentityDto(CAS_USER);
		idmIdentityDto.setState(IdentityState.DISABLED);
		Mockito.when(identityService.getByUsername(CAS_USER)).thenReturn(idmIdentityDto);

		boolean authorizeResult = casAuthenticationFilter.authorize(TEST_TOKEN, request, response);

		Assert.assertFalse(authorizeResult);
	}

	@Test(expected = TwoFactorAuthenticationRequiredException.class)
	public void testTwoFactorAuthenticationRequiredException() throws TicketValidationException {

		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

		IdmIdentityDto idmIdentityDto = new IdmIdentityDto(CAS_USER);

		AttributePrincipalImpl attributePrincipal = new AttributePrincipalImpl(CAS_USER);
		AssertionImpl assertion = new AssertionImpl(attributePrincipal);

		Mockito.when(casConfiguration.getUrl()).thenReturn(CAS_URL);
		Mockito.when(casConfiguration.getService(request, true)).thenReturn(IDM_URL);

		Mockito.when(casValidationService.validate(TEST_TOKEN, IDM_URL, CAS_URL)).thenReturn(assertion);
		Mockito.when(identityService.getByUsername(CAS_USER)).thenReturn(idmIdentityDto);

		Mockito.when(jwtAuthenticationService.createJwtAuthenticationAndAuthenticate(Mockito.any(LoginDto.class), Mockito.eq(idmIdentityDto),
				Mockito.eq(CoreModuleDescriptor.MODULE_ID))).thenThrow(TwoFactorAuthenticationRequiredException.class);

		casAuthenticationFilter.authorize(TEST_TOKEN, request, response);
	}

}