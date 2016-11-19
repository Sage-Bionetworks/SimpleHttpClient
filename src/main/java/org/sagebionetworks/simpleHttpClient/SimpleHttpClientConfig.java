package org.sagebionetworks.simpleHttpClient;

public class SimpleHttpClientConfig {

	// from HttpClientBuilder
	public static final int DEFAULT_CONNECTION_TIMEOUT_MS = -1;
	// from SocketConfig.getSoTimeout()
	public static final int DEFAULT_SOCKET_TIMEOUT_MS = 0;

	int connectionTimeoutMs;
	int socketTimeoutMs;

	public SimpleHttpClientConfig() {
		connectionTimeoutMs = DEFAULT_CONNECTION_TIMEOUT_MS;
		socketTimeoutMs = DEFAULT_SOCKET_TIMEOUT_MS;
	}

	public int getConnectionTimeoutMs() {
		return connectionTimeoutMs;
	}

	public void setConnectionTimeoutMs(int connectionTimeoutMs) {
		this.connectionTimeoutMs = connectionTimeoutMs;
	}

	public int getSocketTimeoutMs() {
		return socketTimeoutMs;
	}

	public void setSocketTimeoutMs(int socketTimeoutMs) {
		this.socketTimeoutMs = socketTimeoutMs;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + connectionTimeoutMs;
		result = prime * result + socketTimeoutMs;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimpleHttpClientConfig other = (SimpleHttpClientConfig) obj;
		if (connectionTimeoutMs != other.connectionTimeoutMs)
			return false;
		if (socketTimeoutMs != other.socketTimeoutMs)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SimpleHttpClientConfig [connectionTimeoutMs=" + connectionTimeoutMs + ", socketTimeoutMs="
				+ socketTimeoutMs + "]";
	}
}
