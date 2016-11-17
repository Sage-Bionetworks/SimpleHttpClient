package org.sagebionetworks.simpleHttpClient;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
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
	public SimpleHttpResponse get(SimpleHttpRequest request) throws IOException {
		HttpGet httpGet = new HttpGet(request.getUri());
		copyHeaders(request, httpGet);
		return execute(httpGet);
	}

	@Override
	public SimpleHttpResponse post(SimpleHttpRequest request, String requestBody) throws ClientProtocolException, IOException {
		HttpPost httpPost = new HttpPost(request.getUri());
		httpPost.setEntity(new StringEntity(requestBody));
		copyHeaders(request, httpPost);
		return execute(httpPost);
	}

	@Override
	public SimpleHttpResponse put(SimpleHttpRequest request, String requestBody) throws ClientProtocolException, IOException {
		HttpPut httpPut = new HttpPut(request.getUri());
		httpPut.setEntity(new StringEntity(requestBody));
		copyHeaders(request, httpPut);
		return execute(httpPut);
	}

	@Override
	public void delete(SimpleHttpRequest request) throws ClientProtocolException, IOException {
		HttpDelete httpDelete = new HttpDelete(request.getUri());
		copyHeaders(request, httpDelete);
		CloseableHttpResponse response = null;
		try {
			response = httpClient.execute(httpDelete);
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_NO_CONTENT) {
				throw new RuntimeException("Unexpected status code: "+response.getStatusLine().getStatusCode()+" Reason: "+response.getStatusLine().getReasonPhrase());
			}
		} finally {
			if (response != null) {
				response.close();
			}
		}
	}

	@Override
	public SimpleHttpResponse putFile(SimpleHttpRequest request, File toUpload) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SimpleHttpResponse getFile(SimpleHttpRequest request, File result) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Copies the headers from a SimpleHttpRequest to a HttpUriRequest
	 * 
	 * @param request
	 * @param httpUriRequest
	 */
	public static void copyHeaders(SimpleHttpRequest request, HttpUriRequest httpUriRequest) {
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
	private SimpleHttpResponse execute(HttpUriRequest httpUriRequest) throws IOException, ClientProtocolException {
		CloseableHttpResponse response = null;
		try {
			response = httpClient.execute(httpUriRequest);
			SimpleHttpResponse simpleHttpResponse = new SimpleHttpResponse();
			simpleHttpResponse.setStatusCode(response.getStatusLine().getStatusCode());
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_NO_CONTENT && response.getEntity() != null) {
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
