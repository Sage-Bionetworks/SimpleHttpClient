package org.sagebionetworks.simpleHttpClient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class SimpleHttpClientImplUnitTest {

	@Mock
	private CloseableHttpClient mockHttpClient;
	@Mock
	private CloseableHttpResponse mockResponse;
	@Mock
	private StreamProvider mockProvider;

	private SimpleHttpClientImpl simpleHttpClient;
	private SimpleHttpRequest request;
	private SimpleHttpResponse response;
	private List<Header> responseHeaders;

	@Before
	public void before() throws Exception {
		MockitoAnnotations.initMocks(this);
		simpleHttpClient = new SimpleHttpClientImpl();
		simpleHttpClient.setStreamProvider(mockProvider);
		simpleHttpClient.setHttpClient(mockHttpClient);

		Map<String, String> headers = new HashMap<String, String>();
		headers.put("name", "value");
		request = new SimpleHttpRequest();
		request.setUri("uri");
		request.setHeaders(headers);
		when(mockHttpClient.execute(any(HttpUriRequest.class))).thenReturn(mockResponse);
		StatusLine mockStatusLine = Mockito.mock(StatusLine.class);
		when(mockResponse.getStatusLine()).thenReturn(mockStatusLine);
		when(mockStatusLine.getReasonPhrase()).thenReturn("reason");
		when(mockStatusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);
		responseHeaders = new LinkedList<Header>();
		when(mockResponse.getAllHeaders()).thenReturn(new org.apache.http.Header[]{});
		response = new SimpleHttpResponse(HttpStatus.SC_OK, "reason", null, responseHeaders);
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

	@Test
	public void testGet() throws Exception {
		assertEquals(response, simpleHttpClient.get(request));
		ArgumentCaptor<HttpGet> captor = ArgumentCaptor.forClass(HttpGet.class);
		verify(mockHttpClient).execute(captor.capture());
		HttpGet captured = captor.getValue();
		assertEquals(request.getUri(), captured.getURI().toString());
		assertEquals("value", captured.getHeaders("name")[0].getValue());
		verify(mockResponse).close();
	}

	@Test
	public void testPostWithNullBody() throws Exception {
		assertEquals(response, simpleHttpClient.post(request, null));
		ArgumentCaptor<HttpPost> captor = ArgumentCaptor.forClass(HttpPost.class);
		verify(mockHttpClient).execute(captor.capture());
		HttpPost captured = captor.getValue();
		assertEquals(request.getUri(), captured.getURI().toString());
		assertNull(captured.getEntity());
		assertEquals("value", captured.getHeaders("name")[0].getValue());
		verify(mockResponse).close();
	}

	@Test
	public void testPost() throws Exception {
		String body = "body";
		assertEquals(response, simpleHttpClient.post(request, body));
		ArgumentCaptor<HttpPost> captor = ArgumentCaptor.forClass(HttpPost.class);
		verify(mockHttpClient).execute(captor.capture());
		HttpPost captured = captor.getValue();
		assertEquals(request.getUri(), captured.getURI().toString());
		assertEquals(body, EntityUtils.toString(captured.getEntity()));
		assertEquals("value", captured.getHeaders("name")[0].getValue());
		verify(mockResponse).close();
	}

	@Test
	public void testPutWithNullBody() throws Exception {
		assertEquals(response, simpleHttpClient.put(request, null));
		ArgumentCaptor<HttpPut> captor = ArgumentCaptor.forClass(HttpPut.class);
		verify(mockHttpClient).execute(captor.capture());
		HttpPut captured = captor.getValue();
		assertEquals(request.getUri(), captured.getURI().toString());
		assertNull(captured.getEntity());
		assertEquals("value", captured.getHeaders("name")[0].getValue());
		verify(mockResponse).close();
	}

	@Test
	public void testPut() throws Exception {
		String body = "body";
		assertEquals(response, simpleHttpClient.put(request, body));
		ArgumentCaptor<HttpPut> captor = ArgumentCaptor.forClass(HttpPut.class);
		verify(mockHttpClient).execute(captor.capture());
		HttpPut captured = captor.getValue();
		assertEquals(request.getUri(), captured.getURI().toString());
		assertEquals(body, EntityUtils.toString(captured.getEntity()));
		assertEquals("value", captured.getHeaders("name")[0].getValue());
		verify(mockResponse).close();
	}

	@Test
	public void testDelete() throws Exception {
		assertEquals(response, simpleHttpClient.delete(request));
		ArgumentCaptor<HttpDelete> captor = ArgumentCaptor.forClass(HttpDelete.class);
		verify(mockHttpClient).execute(captor.capture());
		HttpDelete captured = captor.getValue();
		assertEquals(request.getUri(), captured.getURI().toString());
		assertEquals("value", captured.getHeaders("name")[0].getValue());
		verify(mockResponse).close();
	}

	@Test (expected = IllegalArgumentException.class)
	public void testPutFileWithNullFile() throws Exception {
		simpleHttpClient.putFile(request, null);
	}

	@Test
	public void testPutFile() throws Exception {
		File mockFile = Mockito.mock(File.class);
		assertEquals(response, simpleHttpClient.putFile(request, mockFile));
		ArgumentCaptor<HttpPut> captor = ArgumentCaptor.forClass(HttpPut.class);
		verify(mockHttpClient).execute(captor.capture());
		HttpPut captured = captor.getValue();
		assertEquals(request.getUri(), captured.getURI().toString());
		assertTrue(captured.getEntity() instanceof FileEntity);
		assertEquals("value", captured.getHeaders("name")[0].getValue());
		verify(mockResponse).close();
	}

	@Test (expected = IllegalArgumentException.class)
	public void testDownloadFileWithNullFile() throws Exception {
		simpleHttpClient.getFile(request, null);
	}

	@Test
	public void testGetFileWithException() throws Exception {
		File mockFile = Mockito.mock(File.class);
		FileOutputStream mockStream = Mockito.mock(FileOutputStream.class);
		when(mockProvider.getFileOutputStream(mockFile)).thenReturn(mockStream);
		when(mockResponse.getEntity()).thenThrow(new RuntimeException());
		try {
			simpleHttpClient.getFile(request, mockFile);
		} catch (RuntimeException e) {
			// expected
		}
		ArgumentCaptor<HttpGet> captor = ArgumentCaptor.forClass(HttpGet.class);
		verify(mockHttpClient).execute(captor.capture());
		HttpGet captured = captor.getValue();
		assertEquals(request.getUri(), captured.getURI().toString());
		assertEquals("value", captured.getHeaders("name")[0].getValue());
		verify(mockResponse).close();
		verify(mockStream).close();
	}

	@Test
	public void testGetFile() throws Exception {
		File mockFile = Mockito.mock(File.class);
		FileOutputStream mockStream = Mockito.mock(FileOutputStream.class);
		when(mockProvider.getFileOutputStream(mockFile)).thenReturn(mockStream);
		assertEquals(response, simpleHttpClient.getFile(request, mockFile));
		ArgumentCaptor<HttpGet> captor = ArgumentCaptor.forClass(HttpGet.class);
		verify(mockHttpClient).execute(captor.capture());
		HttpGet captured = captor.getValue();
		assertEquals(request.getUri(), captured.getURI().toString());
		assertEquals("value", captured.getHeaders("name")[0].getValue());
		verify(mockResponse).close();
		verify(mockStream).close();
	}

	@Test (expected = IllegalArgumentException.class)
	public void testExecuteWithNullRequest() throws Exception {
		simpleHttpClient.execute(null);
	}

	@Test
	public void testExecuteWithNullResponseBody() throws Exception {
		assertEquals(response, simpleHttpClient.execute(new HttpGet("uri")));
		ArgumentCaptor<HttpGet> captor = ArgumentCaptor.forClass(HttpGet.class);
		verify(mockHttpClient).execute(captor.capture());
		HttpGet captured = captor.getValue();
		assertEquals("uri", captured.getURI().toString());
		verify(mockResponse).close();
	}

	@Test
	public void testExecuteWithResponseBody() throws Exception {
		HttpEntity mockHttpEntity = Mockito.mock(HttpEntity.class);
		when(mockResponse.getEntity()).thenReturn(mockHttpEntity);
		when(mockHttpEntity.getContent()).thenReturn(new ByteArrayInputStream("content".getBytes()));
		response = new SimpleHttpResponse(HttpStatus.SC_OK, "reason", "content", responseHeaders);
		assertEquals(response, simpleHttpClient.execute(new HttpGet("uri")));
		ArgumentCaptor<HttpGet> captor = ArgumentCaptor.forClass(HttpGet.class);
		verify(mockHttpClient).execute(captor.capture());
		HttpGet captured = captor.getValue();
		assertEquals("uri", captured.getURI().toString());
		verify(mockResponse).close();
	}

	@Test
	public void testConvertHeadersWithNull() {
		assertNull(SimpleHttpClientImpl.convertHeaders(null));
	}

	@Test
	public void testConvertHeadersWithEmptyArray() {
		assertEquals(new LinkedList<Header>(),
				SimpleHttpClientImpl.convertHeaders(new org.apache.http.Header[]{}));
	}

	@Test
	public void testConvertHeaders() {
		org.apache.http.Header header1 = new BasicHeader("name", "value");
		org.apache.http.Header header2 = new BasicHeader("name2", "value2");
		org.apache.http.Header[] toConvert = new org.apache.http.Header[]{header1, header2};
		List<Header> converted = new LinkedList<Header>();
		converted.add(new Header("name", "value"));
		converted.add(new Header("name2", "value2"));
		assertEquals(converted, SimpleHttpClientImpl.convertHeaders(toConvert));
	}

	@Test (expected = IllegalArgumentException.class)
	public void testExtractContentTypeWithNullRequest() {
		SimpleHttpClientImpl.extractContentType(null);
	}

	@Test
	public void testExtractContentTypeWithNullHeader() {
		assertEquals(ContentType.APPLICATION_JSON,
				SimpleHttpClientImpl.extractContentType(new SimpleHttpRequest()));
	}

	@Test
	public void testExtractContentTypeWithEmptyContentTypeHeader() {
		assertEquals(ContentType.APPLICATION_JSON,
				SimpleHttpClientImpl.extractContentType(request));
	}

	@Test
	public void testExtractContentTypeWithExistingContentTypeHeader() {
		request.getHeaders().put("Content-Type", ContentType.TEXT_PLAIN.toString());
		assertEquals(ContentType.TEXT_PLAIN.getCharset(),
				SimpleHttpClientImpl.extractContentType(request).getCharset());
		assertEquals(ContentType.TEXT_PLAIN.getMimeType(),
				SimpleHttpClientImpl.extractContentType(request).getMimeType());
	}

	@Test (expected = IllegalArgumentException.class)
	public void testExtractContentTypeWithContentTypeHeaderWithoutCharset() {
		request.getHeaders().put("Content-Type", "text/plain");
		SimpleHttpClientImpl.extractContentType(request);
	}

	@Test
	public void testExtractContentTypeWithInvalidContentTypeHeader() {
		request.getHeaders().put("Content-Type", "");
		assertEquals(ContentType.APPLICATION_JSON,
				SimpleHttpClientImpl.extractContentType(request));
	}
}
