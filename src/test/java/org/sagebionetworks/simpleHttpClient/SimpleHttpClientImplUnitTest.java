package org.sagebionetworks.simpleHttpClient;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

public class SimpleHttpClientImplUnitTest {

	@Mock
	private CloseableHttpClient mockHttpClient;

	private SimpleHttpClient simpleHttpClient;

	@Before
	public void before() {
		simpleHttpClient = new SimpleHttpClientImpl();
		ReflectionTestUtils.setField(simpleHttpClient, "httpClient", mockHttpClient);
	}

	@Test (expected = IllegalArgumentException.class)
	public void testCopyHeadersWithNullSimpleHttpRequest() {
		SimpleHttpClientImpl.copyHeaders(null, new HttpGet());
	}

	@Test (expected = IllegalArgumentException.class)
	public void testCopyHeadersWithNullHttpUriRequest() {
		SimpleHttpClientImpl.copyHeaders(new SimpleHttpRequest(), null);
	}

	@Test
	public void testCopyHeadersWithEmptyHeaders() {
		HttpGet mockHttpGet = Mockito.mock(HttpGet.class);
		SimpleHttpClientImpl.copyHeaders(new SimpleHttpRequest(), mockHttpGet);
		verifyZeroInteractions(mockHttpGet);
	}

	@Test
	public void testCopyHeaders() {
		String name1 = "name1";
		String name2 = "name2";
		String value1 = "value1";
		String value2 = "value2";
		Map<String, String> headers = new HashMap<String, String>();
		headers.put(name1, value1);
		headers.put(name2, value2);
		SimpleHttpRequest request = new SimpleHttpRequest();
		request.setHeaders(headers);
		HttpGet mockHttpGet = Mockito.mock(HttpGet.class);
		SimpleHttpClientImpl.copyHeaders(request, mockHttpGet);
		verify(mockHttpGet).addHeader(name1, value1);
		verify(mockHttpGet).addHeader(name2, value2);
		verifyNoMoreInteractions(mockHttpGet);
	}

	@Test (expected = IllegalArgumentException.class)
	public void testValidateSimpleHttpRequestWithNullRequest() {
		SimpleHttpClientImpl.validateSimpleHttpRequest(null);
	}

	@Test (expected = IllegalArgumentException.class)
	public void testValidateSimpleHttpRequestWithNullUri() {
		SimpleHttpClientImpl.validateSimpleHttpRequest(new SimpleHttpRequest());
	}

	@Test
	public void testValidateSimpleHttpRequestWithValidRequest() {
		SimpleHttpRequest request = new SimpleHttpRequest();
		request.setUri("uri");
		SimpleHttpClientImpl.validateSimpleHttpRequest(request);
	}
}
