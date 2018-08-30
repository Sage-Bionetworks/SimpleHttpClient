package org.sagebionetworks.simpleHttpClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;


public final class SimpleHttpClientImpl implements SimpleHttpClient{

	private static final String CONTENT_TYPE = "Content-Type";
	private CloseableHttpClient httpClient;
	private StreamProvider provider;
	private CookieStore cookieStore;

	public SimpleHttpClientImpl() {
		this(null);
	}

	/**
	 * Create a SimpleHttpClient with a new connection pool
	 * 
	 * @param config
	 */
	public SimpleHttpClientImpl(SimpleHttpClientConfig config) {
		if (config == null) {
			httpClient = HttpClients.createDefault();
		} else {
			RequestConfig requestConfig = RequestConfig.custom()
					.setConnectionRequestTimeout(config.getConnectionRequestTimeoutMs())
					.setConnectTimeout(config.getConnectTimeoutMs())
					.setSocketTimeout(config.getSocketTimeoutMs())
					.build();
			httpClient = HttpClients.custom()
					.setDefaultRequestConfig(requestConfig)
					.setDefaultCookieStore(cookieStore)
					.build();
		}
		provider = new StreamProviderImpl();
	}

	@Override
	public SimpleHttpResponse get(SimpleHttpRequest request)
			throws ClientProtocolException, IOException {
		validateSimpleHttpRequest(request);
		HttpGet httpGet = new HttpGet(request.getUri());
		copyHeaders(request, httpGet);
		return execute(httpGet);
	}

	@Override
	public SimpleHttpResponse post(SimpleHttpRequest request, String requestBody)
			throws ClientProtocolException, IOException {
		validateSimpleHttpRequest(request);
		HttpPost httpPost = new HttpPost(request.getUri());
		if (requestBody != null) {
			httpPost.setEntity(new StringEntity(requestBody, extractContentType(request)));
		}
		copyHeaders(request, httpPost);
		return execute(httpPost);
	}

	@Override
	public SimpleHttpResponse put(SimpleHttpRequest request, String requestBody)
			throws ClientProtocolException, IOException {
		validateSimpleHttpRequest(request);
		HttpPut httpPut = new HttpPut(request.getUri());
		if (requestBody != null) {
			httpPut.setEntity(new StringEntity(requestBody, extractContentType(request)));
		}
		copyHeaders(request, httpPut);
		return execute(httpPut);
	}

	@Override
	public SimpleHttpResponse delete(SimpleHttpRequest request)
			throws ClientProtocolException, IOException {
		validateSimpleHttpRequest(request);
		HttpDelete httpDelete = new HttpDelete(request.getUri());
		copyHeaders(request, httpDelete);
		return execute(httpDelete);
	}

	@Override
	public SimpleHttpResponse putFile(SimpleHttpRequest request, File toUpload)
			throws ClientProtocolException, IOException {
		validateSimpleHttpRequest(request);
		if (toUpload == null) {
			throw new IllegalArgumentException("toUpload cannot be null");
		}
		HttpPut httpPut = new HttpPut(request.getUri());
		httpPut.setEntity(new FileEntity(toUpload));
		copyHeaders(request, httpPut);
		return execute(httpPut);
	}

	@Override
	public SimpleHttpResponse putToURL(SimpleHttpRequest request, InputStream toUpload, long inputLength)
			throws ClientProtocolException, IOException {
		validateSimpleHttpRequest(request);
		if (toUpload == null) {
			throw new IllegalArgumentException("toUpload cannot be null");
		}
		HttpPut httpPut = new HttpPut(request.getUri());
		InputStreamEntity entity = new InputStreamEntity(toUpload, inputLength);
		entity.setChunked(false);
		httpPut.setEntity(entity);
		copyHeaders(request, httpPut);
		return execute(httpPut);
	}

	@Override
	public SimpleHttpResponse getFile(SimpleHttpRequest request, File result)
			throws ClientProtocolException, IOException {
		validateSimpleHttpRequest(request);
		if (result == null) {
			throw new IllegalArgumentException("result cannot be null");
		}
		HttpGet httpGet = new HttpGet(request.getUri());
		copyHeaders(request, httpGet);
		CloseableHttpResponse response = null;
		FileOutputStream fileOutputStream = provider.getFileOutputStream(result);
		try {
			response = httpClient.execute(httpGet);
			String content = null;
			if (response.getEntity() != null) {
				if (response.getStatusLine().getStatusCode() == 200) {
					response.getEntity().writeTo(fileOutputStream);
				} else {
					content = EntityUtils.toString(response.getEntity());
				}
			}
			return new SimpleHttpResponse(
					response.getStatusLine().getStatusCode(),
					response.getStatusLine().getReasonPhrase(),
					content,
					convertHeaders(response.getAllHeaders()));
		} finally {
			if (fileOutputStream != null) {
				fileOutputStream.close();
			}
			if (response != null) {
				response.close();
			}
		}
	}

	/**
	 * Validates a SimpleHttpRequest and throw exception if any required field is null
	 * 
	 * @param request
	 */
	public static void validateSimpleHttpRequest(SimpleHttpRequest request) {
		if (request == null) {
			throw new IllegalArgumentException("request cannot be null");
		}
		if (request.getUri() == null) {
			throw new IllegalArgumentException("SimpleHttpRequest.uri cannot be null");
		}
	}

	/**
	 * Copies the headers from a SimpleHttpRequest to a HttpUriRequest
	 * 
	 * @param request
	 * @param httpUriRequest
	 */
	public static void copyHeaders(SimpleHttpRequest request, HttpUriRequest httpUriRequest) {
		if (request == null) {
			throw new IllegalArgumentException("request cannot be null");
		}
		if (httpUriRequest == null) {
			throw new IllegalArgumentException("httpUriRequest cannot be null");
		}
		Map<String, String> headers = request.getHeaders();
		if (headers != null) {
			for (String name : headers.keySet()) {
				httpUriRequest.addHeader(name, headers.get(name));
			}
		}
	}

	/**
	 * Extract ContentType from the request headers. If there is none, use
	 * ContentType.APPLICATION_JSON.
	 * 
	 * @param request
	 * @return
	 */
	protected static ContentType extractContentType(SimpleHttpRequest request) {
		if (request == null) {
			throw new IllegalArgumentException("SimpleHttpRequest is required.");
		}
		try {
			ContentType contentType = ContentType.parse(request.getHeaders().get(CONTENT_TYPE));
			if (contentType.getCharset() == null) {
				contentType = contentType.withCharset(Charset.forName("UTF-8"));
			}
			request.getHeaders().put(CONTENT_TYPE, contentType.toString());
			return contentType;
		} catch (Exception e) {
			if (request.getHeaders() == null) {
				request.setHeaders(new HashMap<String, String>());
			}
			request.getHeaders().put(CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
			return ContentType.APPLICATION_JSON;
		}
	}

	/**
	 * Convert org.apache.http.Header[] to list of SimpleHttpResponse's Header
	 * 
	 * @param headers
	 * @return
	 */
	protected static List<Header> convertHeaders(org.apache.http.Header[] headers) {
		if (headers == null) {
			return null;
		}
		List<Header> results = new LinkedList<Header>();
		for (int i = 0; i < headers.length; i++) {
			results.add(new Header(headers[i].getName(), headers[i].getValue()));
		}
		return results;
	}

	/**
	 * Performs the request, then consume the response to build a simpleHttpResponse
	 * 
	 * @param httpUriRequest
	 * @return
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	protected SimpleHttpResponse execute(HttpUriRequest httpUriRequest)
			throws IOException, ClientProtocolException {
		if (httpUriRequest == null) {
			throw new IllegalArgumentException("httpUriRequest cannot be null");
		}
		CloseableHttpResponse response = null;
		try {
			response = httpClient.execute(httpUriRequest);
			String content = null;
			if (response.getEntity() != null) {
				content = EntityUtils.toString(response.getEntity());
			}
			return new SimpleHttpResponse(
					response.getStatusLine().getStatusCode(),
					response.getStatusLine().getReasonPhrase(),
					content,
					convertHeaders(response.getAllHeaders()));
		} finally {
			if (response != null) {
				response.close();
			}
		}
	}

	protected void setHttpClient(CloseableHttpClient httpClient) {
		this.httpClient = httpClient;
	}

	protected void setStreamProvider(StreamProvider provider) {
		this.provider = provider;
	}

	protected String getCookieValue(String name){
		if(name == null){
			throw new IllegalArgumentException("name can not be null");
		}

		List<Cookie> cookies = cookieStore.getCookies();
		if (cookies == null){
			return null;
		}
		for(Cookie cookie : cookies){
			if (name.equals(cookie.getName())){
				return cookie.getValue();
			}
		}
		return null;
	}
}
