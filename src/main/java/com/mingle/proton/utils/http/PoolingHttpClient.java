package com.mingle.proton.utils.http;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NoHttpResponseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PoolingHttpClient {

	private static final Logger logger = Logger.getLogger(PoolingHttpClient.class);

	private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.153 Safari/537.36";
	private static final long PERIOD_TIME = 3600 * 1000L;
	private static String DEFAULT_CHARSET = "utf8";
	// 连接池更新时间
	private long lastUpdateTime = 0;

	private CloseableHttpClient httpClient;

	private String charset;

	private int maxRetries = 1;

	private int connectionTimeOut = 2000;

	private int soTimeOut = 2000;

	// the timeout in milliseconds used when requesting a connection from the
	// connection manager.
	private int connectionRequestTimeout = 2000;

	private int maxConnection = 500;

	private int maxPerRoute = 20;

	public PoolingHttpClient() {
		charset = DEFAULT_CHARSET;
		httpClient = createClient();
	}

	private CloseableHttpClient createClient() {
		// 多线程连接池
		PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
		connectionManager.setDefaultMaxPerRoute(maxPerRoute);
		connectionManager.setMaxTotal(maxConnection);
		connectionManager.setDefaultConnectionConfig(ConnectionConfig.custom().setCharset(Charset.forName(charset))
				.build());

		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
		// 设置默认request参数
		httpClientBuilder.setDefaultRequestConfig(RequestConfig.custom().setConnectTimeout(connectionTimeOut)
				.setSocketTimeout(soTimeOut).setConnectionRequestTimeout(connectionRequestTimeout).build());
		// 重试机制
		httpClientBuilder.setRetryHandler(new HttpRequestRetryHandler() {
			@Override
			public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
				if (executionCount > maxRetries) {
					logger.info("Maximum tries reached for client http pool, maxRetries= " + maxRetries);
					return false;
				}
				if (exception instanceof NoHttpResponseException) {
					logger.debug("No response from server on " + executionCount + " call");
					return true;
				}
				if (exception instanceof SSLHandshakeException) {
					return false;
				}
				HttpRequest request = (HttpRequest) context.getAttribute(HttpCoreContext.HTTP_REQUEST);
				boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
				if (idempotent) {
					return true;
				}
				return false;
			}
		});
		// // 自定义长连接策略
		// httpClientBuilder.setKeepAliveStrategy(new
		// ConnectionKeepAliveStrategy() {
		// @Override
		// public long getKeepAliveDuration(HttpResponse response, HttpContext
		// context) {
		// // Honor 'keep-alive' header
		// HeaderElementIterator it = new
		// BasicHeaderElementIterator(response.headerIterator(HTTP.CONN_KEEP_ALIVE));
		// while (it.hasNext()) {
		// HeaderElement he = it.nextElement();
		// String param = he.getName();
		// String value = he.getValue();
		// if (value != null && param.equalsIgnoreCase("timeout")) {
		// try {
		// return (Long.parseLong(value) * 1000);
		// } catch (NumberFormatException ignore) {
		// }
		// }
		// }
		// return (60 * 1000);
		// }
		// });
		httpClientBuilder.setConnectionManager(connectionManager);
		httpClientBuilder.setUserAgent(USER_AGENT);
		return httpClientBuilder.build();
	}

	@SuppressWarnings("deprecation")
	private synchronized void releaseConnection() {
		if (System.currentTimeMillis() - lastUpdateTime > PERIOD_TIME) {
			lastUpdateTime = System.currentTimeMillis();
			// Close expired connections
			httpClient.getConnectionManager().closeExpiredConnections();
			// close connections that have been idle longer than special time
			httpClient.getConnectionManager().closeIdleConnections(1 * 60, TimeUnit.SECONDS);
		}
	}

	public CloseableHttpClient getHttpClient() {
		if (System.currentTimeMillis() - lastUpdateTime > PERIOD_TIME) {
			releaseConnection();
		}
		return httpClient;
	}

	public String get(String url) {
		return get(url, charset);
	}

	public String get(String url, String charset) {
		if (StringUtils.isBlank(url)) {
			return null;
		}
		HttpGet httpGet = new HttpGet(url);
		CloseableHttpResponse response = null;
		try {
			response = getHttpClient().execute(httpGet);
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != HttpStatus.SC_OK) {
				httpGet.abort();
				logger.error("PoolingHttpClient error status code :" + statusCode);
			}
			return EntityUtils.toString(response.getEntity(), charset);
		} catch (ClientProtocolException e) {
			logger.error("PoolingHttpClient ClientProtocolException , url=" + url, e);
		} catch (IOException e) {
			logger.error("PoolingHttpClient IOException , url=" + url, e);
		} finally {
			if (response != null) {
				try {
					response.close();
				} catch (IOException e) {
					logger.error("PoolingHttpClient response.close ", e);
				}
			}
		}
		return null;
	}

	public String post(String url, Map<String, Object> params) {
		return post(url, params, charset);
	}

	@SuppressWarnings("unchecked")
	public String post(String url, Map<String, Object> params, String charset) {
		if (StringUtils.isEmpty(url)) {
			return null;
		}
		List<BasicNameValuePair> pairs = null;
		if (params != null && !params.isEmpty()) {
			pairs = new ArrayList<BasicNameValuePair>(params.size());
			for (Map.Entry<String, Object> entry : params.entrySet()) {
				Object value = entry.getValue();
				if (value instanceof List) {
					List<Object> list = (List<Object>) value;
					for (Object object : list) {
						pairs.add(new BasicNameValuePair(entry.getKey(), String.valueOf(object)));
					}
				} else {
					pairs.add(new BasicNameValuePair(entry.getKey(), String.valueOf(value)));
				}
			}
		}
		HttpPost httpPost = new HttpPost(url);
		if (pairs != null && pairs.size() > 0) {
			try {
				httpPost.setEntity(new UrlEncodedFormEntity(pairs, charset));
			} catch (UnsupportedEncodingException e) {
				logger.error("PoolingHttpClient UnsupportedEncodingException ", e);
				return null;
			}
		}
		CloseableHttpResponse response = null;
		String result = null;
		try {
			response = getHttpClient().execute(httpPost);
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != HttpStatus.SC_OK) {
				httpPost.abort();
				logger.error("PoolingHttpClient error status code :" + statusCode);
			}
			result = EntityUtils.toString(response.getEntity(), charset);
		} catch (IOException e) {
			logger.error("PoolingHttpClient IOException ", e);
		} finally {
			if (response != null) {
				try {
					response.close();
				} catch (IOException e) {
					logger.error("PoolingHttpClient response.close error ", e);
				}
			}
		}
		return result;
	}
}