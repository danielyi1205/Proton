package com.mingle.proton.utils.http;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author chentong
 */
@SuppressWarnings("rawtypes")
public class HTTPLongClient {
	private static final Logger logger = Logger.getLogger(HTTPLongClient.class);
	/** 连接池释放时间间隔 */
	private static final long PERIOD_TIME = 1000L * 3600;

	private final static int DEFAULT_HTTP_EXCEPTION_RETRY_NUM = 1;

	private static String DEFAULT_CHARSET = "utf-8";

	private static HTTPLongClient instance = null;

	/** 带有http长连接池的http客户端 */
	private HttpClient httpClient = null;
	/** 上次连接池更新时间 */
	private long lastUpdateTime = 0;
	/** 连接超时时间设置,单位毫秒 */
	private int connectTimeout = 2000;
	/** 读取socket时间设置,单位毫秒 */
	private int soTimeout = 2000;
	/** 每个目的host最多保持多少个连接 */
	private int maxConnectionsPerHost = 100;
	/** 最多保持多少个连接 */
	private int maxTotalConnections = 1000;
	/** 默认http请求出错重试次数 */

	static {
		instance = new HTTPLongClient();
	}

	/**
	 * 禁止通过new的方式创建实例
	 */
	private HTTPLongClient() {
		reinit();
	}

	/**
	 * 禁止通过new的方式创建实例
	 */
	private HTTPLongClient(int soTimeout) {
		this.soTimeout = soTimeout;
		reinit();
	}

	/**
	 * 禁止通过new的方式创建实例
	 */
	private HTTPLongClient(int connectTimeout, int soTimeout) {
		this.soTimeout = soTimeout;
		this.connectTimeout = connectTimeout;
		reinit();
	}

	/**
	 * 禁止通过new的方式创建实例
	 */
	private HTTPLongClient(int connectTimeout, int soTimeout, int maxConnectionsPerHost, int maxTotalConnections) {
		this.soTimeout = soTimeout;
		this.connectTimeout = connectTimeout;
		this.maxConnectionsPerHost = maxConnectionsPerHost;
		this.maxTotalConnections = maxTotalConnections;
		reinit();
	}

	/**
	 * 创建实例
	 * 
	 * @param soTimeout
	 *            , socket通信（read or write）超时时间
	 */
	public static HTTPLongClient newInstance(int soTimeout) {
		return new HTTPLongClient(soTimeout);
	}

	/** 初始化连接 */
	public void reinit() {
		HttpClientParams params = new HttpClientParams();
		params.setConnectionManagerClass(MultiThreadedHttpConnectionManager.class);
		params.setConnectionManagerTimeout(connectTimeout);
		params.setSoTimeout(soTimeout);
		params.setVersion(HttpVersion.HTTP_1_1);
		httpClient = new HttpClient(params);
		HttpConnectionManagerParams param = new HttpConnectionManagerParams();
		param.setConnectionTimeout(connectTimeout);
		param.setSoTimeout(soTimeout);
		param.setDefaultMaxConnectionsPerHost(maxConnectionsPerHost);
		param.setMaxTotalConnections(maxTotalConnections);
		httpClient.getHttpConnectionManager().setParams(param);
		if (logger.isInfoEnabled()) {
			logger.info("current properties: \"connectTimeout:" + connectTimeout + "\" , soTimeout:" + soTimeout
					+ "\" , maxConnectionsPerHost:" + maxConnectionsPerHost + "\"\n\r"
					+ "if you wanna change the properties and make effective,please call reInit() method after you set new properties!");
		}
	}

	/**
	 * 获得经过长连接池配置的http客户端
	 */
	private HttpClient gethttpclient() {
		if (System.currentTimeMillis() - lastUpdateTime > PERIOD_TIME) {
			checkReleaseConnection();
		}
		httpClient.setState(new HttpState());
		return httpClient;
	}

	/**
	 * 释放空闲的连接
	 */
	private synchronized void checkReleaseConnection() {
		lastUpdateTime = System.currentTimeMillis();
		try {
			httpClient.getHttpConnectionManager().closeIdleConnections(1);
			if (logger.isInfoEnabled()) {
				logger.info("free connections successfully!");
			}
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	/**
	 * http get请求， 默认异常重试一次
	 * 
	 * @param httpAddr
	 * @return
	 */
	public String geturlcontent(String httpAddr) {
		return geturlcontent(httpAddr, true, DEFAULT_CHARSET, DEFAULT_HTTP_EXCEPTION_RETRY_NUM);
	}

	public String geturlcontent(String httpAddr, boolean isGet, String charset, int retry) {
		for (int num = 1; num <= retry; num++) {
			try {
				return geturlcontentEx(httpAddr, isGet, charset);
			} catch (Exception e) {
			}
			logger.info("http retry = " + num);
		}
		return geturlcontentEx(httpAddr, isGet, charset);
	}

	/**
	 * @param httpAddr
	 * @param isGet
	 * @param charset
	 * @return
	 */
	private String geturlcontentEx(String httpAddr, boolean isGet, String charset) {
		HttpMethod httpMethod = null;
		try {
			HttpClient client = gethttpclient();
			if (isGet) {
				httpMethod = new GetMethod(httpAddr);
			} else {
				httpMethod = new PostMethod(httpAddr);
			}
			HttpMethodParams arg0 = new HttpMethodParams();
			arg0.setContentCharset(charset);
			arg0.setSoTimeout(client.getParams().getSoTimeout());
			httpMethod.setParams(arg0);
			setHttpHeader(httpMethod);
			client.executeMethod(httpMethod);
			int httpStatus = httpMethod.getStatusCode();
			if (httpStatus == 200 || (httpStatus > 300 && httpStatus < 400)) {
				return httpMethod.getResponseBodyAsString();
			} else {
				logger.error("httpStatus:" + httpMethod.getStatusCode() + "," + httpAddr);
				throw new IOException("httpStatus:" + httpMethod.getStatusCode());
			}
		} catch (HttpException e) {
			logger.error("HttpException", e);
		} catch (IOException e) {
			logger.error("IOException", e);
		} catch (Exception e) {
			logger.error("OtherException", e);
		} finally {
			if (httpMethod != null)
				httpMethod.releaseConnection(); // 释放connection的占用，以便给其他调用者复用
		}
		return null;
	}

	/**
	 * http post请求， 默认异常重试一次
	 * 
	 * @param httpAddr
	 *            请求地址
	 * @param params
	 *            传递参数
	 * @return
	 */
	public String geturlcontent(String httpAddr, Map<String, Object> params) {
		return geturlcontent(httpAddr, false, params, DEFAULT_CHARSET, DEFAULT_HTTP_EXCEPTION_RETRY_NUM);
	}

	public String geturlcontent(String httpAddr, boolean isGet, Map<String, Object> params, String charset, int retry) {
		for (int num = 1; num <= retry; num++) {
			try {
				return geturlcontentEx(httpAddr, isGet, params, charset);
			} catch (Exception e) {
				logger.error("retry geturlcontent error:", e);
			}
			logger.info("http retry = " + num);
		}
		try {
			return geturlcontentEx(httpAddr, isGet, params, charset);
		} catch (Exception e) {
			logger.error("geturlcontent error:", e);
		}
		return null;
	}

	/**
	 * @param httpAddr
	 * @param isGet
	 * @param params
	 *            , 支持List作为值来传递数组参数
	 * @param charset
	 * @return
	 * @throws Exception
	 */
	private String geturlcontentEx(String httpAddr, boolean isGet, Map<String, Object> params, String charset) throws Exception {
		HttpMethod httpMethod = null;
		try {
			HttpClient client = gethttpclient();
			if (isGet) {
				httpMethod = new GetMethod(httpAddr);
			} else {
				httpMethod = new PostMethod(httpAddr);
			}
			HttpMethodParams arg0 = new HttpMethodParams();
			arg0.setContentCharset(charset);
			arg0.setSoTimeout(client.getParams().getSoTimeout());
			httpMethod.setParams(arg0);
			if (params != null && !isGet) {
				Iterator<String> it = params.keySet().iterator();
				while (it.hasNext()) {
					String name = it.next();
					Object value = params.get(name);
					if (value instanceof List) {
						List list = (List) value;
						for (Object object : list) {
							((PostMethod) httpMethod).addParameter(name, String.valueOf(object));
						}
					} else {
						((PostMethod) httpMethod).addParameter(name, String.valueOf(value));
					}
				}

			}
			setHttpHeader(httpMethod);
			client.executeMethod(httpMethod);
			int httpStatus = httpMethod.getStatusCode();
			if (httpStatus == 200 || (httpStatus > 300 && httpStatus < 400)) {
				return httpMethod.getResponseBodyAsString();
			} else {
				logger.error("httpStatus:" + httpMethod.getStatusCode() + "," + httpAddr);
				throw new IOException("httpStatus:" + httpMethod.getStatusCode());
			}
		} finally {
			httpMethod.releaseConnection();
		}
	}

	/**
	 * 文件上传请求
	 * 
	 * @param httpAddr
	 * @param fileFieldName
	 * @param file
	 * @param params
	 * @return
	 */
	public String postfile(String httpAddr, String fileFieldName, File file, Map<String, Object> params) {
		return postfile(httpAddr, fileFieldName, file, params, DEFAULT_CHARSET);
	}

	private String postfile(String httpAddr, String fileFieldName, File file, Map<String, Object> params, String charset) {
		PostMethod httpMethod = new PostMethod(httpAddr);
		try {
			HttpClient client = gethttpclient();
			HttpMethodParams hmParams = new HttpMethodParams();
			hmParams.setContentCharset(charset);
			hmParams.setSoTimeout(client.getParams().getSoTimeout());
			httpMethod.setParams(hmParams);
			int total = params.size() + (file != null ? 1 : 0);
			Part[] parts = new Part[total];
			int i = 0;
			if (params != null) {
				Iterator<String> it = params.keySet().iterator();
				while (it.hasNext()) {
					String name = it.next();
					Object value = params.get(name);
					if (value instanceof List) {
						List list = (List) value;
						for (Object object : list) {
							parts[i++] = new StringPart(name, String.valueOf(object));
						}
					} else {
						parts[i++] = new StringPart(name, String.valueOf(value));
					}

				}
			}
			if (file != null) {
				parts[i++] = new FilePart(fileFieldName, file);
			}
			httpMethod.setRequestEntity(new MultipartRequestEntity(parts, httpMethod.getParams()));
			int httpStatus = client.executeMethod(httpMethod);
			if (httpStatus == 200 || (httpStatus > 300 && httpStatus < 400)) {
				return httpMethod.getResponseBodyAsString();
			} else {
				logger.error("httpStatus:" + httpMethod.getStatusCode() + "," + httpAddr);
				throw new IOException("httpStatus:" + httpMethod.getStatusCode());
			}
		} catch (HttpException e) {
			logger.error("HttpException", e);
		} catch (IOException e) {
			logger.error("IOException", e);
		} catch (Exception e) {
			logger.error("OtherException", e);
		} finally {
			if (httpMethod != null)
				httpMethod.releaseConnection();
		}

		return null;
	}

	/**
	 * @param httpMethod
	 */
	private static void setHttpHeader(HttpMethod httpMethod) {
		httpMethod.setRequestHeader("Connection", "keep-alive");
		httpMethod.setRequestHeader("User-Agent",
				"Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.153 Safari/537.36");
		httpMethod.setRequestHeader("refere", "http://www.iwjw.com/");
	}

	/**
	 * @param timeout
	 */
	public void setconnecttimeout(int timeout) {
		this.connectTimeout = timeout;
	}

	/**
	 * @param timeout
	 */
	public void setsotimeout(int timeout) {
		this.soTimeout = timeout;
	}

	/**
	 * @param maxConnections
	 */
	public void setmaxconnectionsperhost(int maxConnections) {
		this.maxConnectionsPerHost = maxConnections;
	}

	public int getMaxTotalConnections() {
		return maxTotalConnections;
	}

	public void setmaxtotalconnections(int maxTotalConnections) {
		this.maxTotalConnections = maxTotalConnections;
	}

	/**
	 * 全局公用httppool
	 * 
	 * @return
	 */
	public static HTTPLongClient instance() {
		return instance;
	}
}