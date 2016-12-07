package org.sagebionetworks.simpleHttpClient;

import static org.junit.Assert.*;

import java.util.LinkedList;

import org.junit.Test;

public class SimpleHttpResponseTest {

	@Test
	public void testGetFirstHeaderNull() {
		SimpleHttpResponse response = new SimpleHttpResponse(200, "OK", null, null);
		assertNull(response.getFirstHeader("anything"));
	}

	@Test
	public void testGetFirstHeaderNotFound() {
		SimpleHttpResponse response = new SimpleHttpResponse(200, "OK", null, new LinkedList<Header>());
		assertNull(response.getFirstHeader("anything"));
	}

	@Test
	public void testGetFirstHeaderMultipleValue() {
		LinkedList<Header> headers = new LinkedList<Header>();
		headers.add(new Header("key", "first"));
		headers.add(new Header("key", "second"));
		headers.add(new Header("key", "third"));
		SimpleHttpResponse response = new SimpleHttpResponse(200, "OK", null, headers);
		assertEquals("first", response.getFirstHeader("key").getValue());
	}
}
