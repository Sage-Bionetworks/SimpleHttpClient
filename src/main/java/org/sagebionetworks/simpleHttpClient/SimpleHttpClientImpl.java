package org.sagebionetworks.simpleHttpClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;


public class SimpleHttpClientImpl implements SimpleHttpClient{

	private CloseableHttpClient httpClient;

	/**
	 * Create a SimpleHttpClient with a new connection pool.
	 */
	public SimpleHttpClientImpl() {
		httpClient = HttpClients.createDefault();
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
		httpPost.setEntity(new StringEntity(requestBody));
		copyHeaders(request, httpPost);
		return execute(httpPost);
	}

	@Override
	public SimpleHttpResponse put(SimpleHttpRequest request, String requestBody)
			throws ClientProtocolException, IOException {
		validateSimpleHttpRequest(request);
		HttpPut httpPut = new HttpPut(request.getUri());
		httpPut.setEntity(new StringEntity(requestBody));
		copyHeaders(request, httpPut);
		return execute(httpPut);
	}

	@Override
	public void delete(SimpleHttpRequest request)
			throws ClientProtocolException, IOException {
		validateSimpleHttpRequest(request);
		HttpDelete httpDelete = new HttpDelete(request.getUri());
		copyHeaders(request, httpDelete);
		CloseableHttpResponse response = null;
		try {
			response = httpClient.execute(httpDelete);
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_NO_CONTENT
					&& response.getStatusLine().getStatusCode() != HttpStatus.SC_OK
					&& response.getStatusLine().getStatusCode() != HttpStatus.SC_ACCEPTED) {
				throw new HttpResponseException(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
			}
		} finally {
			if (response != null) {
				EntityUtils.consumeQuietly(response.getEntity());
				response.close();
			}
		}
	}

	@Override
	public SimpleHttpResponse putFile(SimpleHttpRequest request, File toUpload)
			throws ClientProtocolException, IOException {
		validateSimpleHttpRequest(request);
		HttpPut httpPut = new HttpPut(request.getUri());
		httpPut.setEntity(new FileEntity(toUpload));
		copyHeaders(request, httpPut);
		return execute(httpPut);
	}

	@Override
	public void getFile(SimpleHttpRequest request, File result)
			throws ClientProtocolException, IOException {
		validateSimpleHttpRequest(request);
		HttpGet httpGet = new HttpGet(request.getUri());
		copyHeaders(request, httpGet);
		CloseableHttpResponse response = null;
		try {
			response = httpClient.execute(httpGet);
			if (response.getEntity() != null) {
				FileOutputStream fileOutputStream = new FileOutputStream(result);
				response.getEntity().writeTo(fileOutputStream);
				fileOutputStream.close();
			}
		} finally {
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
	 * Performs the request, then consume the response to build a simpleHttpResponse
	 * 
	 * @param httpUriRequest
	 * @return
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	private SimpleHttpResponse execute(HttpUriRequest httpUriRequest)
			throws IOException, ClientProtocolException {
		if (httpUriRequest == null) {
			throw new IllegalArgumentException("httpUriRequest cannot be null");
		}
		CloseableHttpResponse response = null;
		try {
			response = httpClient.execute(httpUriRequest);
			SimpleHttpResponse simpleHttpResponse = new SimpleHttpResponse();
			simpleHttpResponse.setStatusCode(response.getStatusLine().getStatusCode());
			if (response.getEntity() != null) {
				simpleHttpResponse.setContent(EntityUtils.toString(response.getEntity()));
			}
			return simpleHttpResponse;
		} finally {
			if (response != null) {
				response.close();
			}
		}
	}
}
