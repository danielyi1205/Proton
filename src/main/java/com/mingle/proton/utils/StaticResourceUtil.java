package com.mingle.proton.utils;

import com.mingle.proton.utils.http.HTTPLongClient;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 静态文件工具类
 * 
 * @author chentong
 * @date 2015年4月20日
 */
public class StaticResourceUtil {

    private static final Logger logger = Logger.getLogger(StaticResourceUtil.class);

	private static final String CLASSPATH = FileTool.getClassPath();
	/** 应用程序中配置文件名 */
	private static final String LOCAL_STATIC_CONFIG = "staticConfig.properties";
	/** staticResourceConfig.properties文件名 */
	private static final String STATIC_RESOURCE_CONFIG_NAME = "staticResourceConfig.properties";
	/** staticResource.properties文件名 */
	private static final String STATIC_RESOURCE_NAME = "staticResource.properties";
	/** 公共配置文件staticResourceConfig.properties地址 */
	private static String staticResourceConfigURL;
	/** 公共配置文件staticResource.properties地址 */
	private static String staticResourceURL;
	/** 配置文件目录 */
	private static File staticResourceDir = new File(CLASSPATH + "staticResourceDir");
	/** staticResourceConfig.properties本地文件 */
	private static File staticResourceConfigLocalFile = new File(CLASSPATH + "staticResourceDir/"
			+ STATIC_RESOURCE_CONFIG_NAME);
	/** staticResource.properties本地文件 */
	private static File staticResourceLocalFile = new File(CLASSPATH + "staticResourceDir/" + STATIC_RESOURCE_NAME);
	/** 是否自动加载staticResource.properties */
	private static volatile boolean isAutoReloadStaticResource;
	/** 每分钟加载一次staticResourceConfig.properties */
	private static final long LOAD_STATIC_RESOURCE_CONFIG_PERIOD = 1 * 60;
	/** 加载的staticResource.properties数据 */
	private static Map<String, String> staticResourceMap = new HashMap<String, String>(10000);
	/** 是否完成初始化 */
	private static volatile boolean isInit = false;
	/** 开发模式 */
	private static boolean isDevEnv = false;

	private static final ReentrantLock lock = new ReentrantLock();

    public static void main(String[] args) {
        new StaticResourceUtil();
    }

    static {
		try {
			load();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * 使用VelocityToolBox中 toolbox.xml文件配置： <toolbox> <tool> <key>static</key>
	 * <scope>application</scope>
	 * <class>com.manyi.staticresource.StaticResourceClient</class> </tool> ...
	 * </toolbox>
	 * 
	 * 本地staticConfig.properties文件格式:
	 * staticResourceConfigURL=http://oss.aliyun.com
	 * /userweb/staticResourceConfig.properties
	 * staticResourceURL=http://oss.aliyun.com/userweb/staticResource.properties
	 */
	public static void load() {
		load(LOCAL_STATIC_CONFIG);
	}

	/**
	 * 初始化需要加载的配置文件URL
	 * 
	 * @param staticConfigFileName
	 */
	public static void load(String staticConfigFileName) {
		if (isInit) {
			return;
		}
		if (StringUtils.isBlank(staticConfigFileName)) {
			throw new IllegalArgumentException("staticConfigFileName isn't null!");
		}
		File staticConfig = new File(CLASSPATH + staticConfigFileName);
		Properties localStaticConfig = null;
		if (staticConfig.exists()) {
			localStaticConfig = loadLoaclProperties(staticConfig.getPath());
		}
		if (localStaticConfig == null) {
			throw new IllegalArgumentException("staticConfig error!");
		}
		staticResourceConfigURL = localStaticConfig.getProperty("staticResourceConfigURL");
		staticResourceURL = localStaticConfig.getProperty("staticResourceURL");
		isDevEnv = Boolean.parseBoolean(localStaticConfig.getProperty("isDev"));
		if (StringUtils.isBlank(staticResourceConfigURL) || StringUtils.isBlank(staticResourceURL)) {
			throw new IllegalArgumentException("staticConfig properties error!");
		}
		// 初始化静态资源目录
		if (!staticResourceDir.exists()) {
			if (!(staticResourceDir.mkdir())) {
				throw new IllegalArgumentException("mkdir staticResourceDir error!");
			}
		}
		// 装载本地staticResourceConfig.properties配置 第一次装载失败且本地无存留文件则抛出异常
		if (!loadStaticResourceConfig()) {
			throw new IllegalArgumentException("staticResourceConfig.properties load fail!");
		}
		// 装载本地staticResource.properties配置 第一次装载失败且本地无存留文件则抛出异常
		if (!loadStaticResource()) {
			throw new IllegalArgumentException("staticResource.properties load fail!");
		}
		//开发环境使用本地静态文件
		if (!isDevEnv) {
			// 每一分钟定时装载staticResourceConfig任务
			Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new Runnable() {
				public void run() {
					loadStaticResourceConfig();
					loadStaticResource();
				}
			}, LOAD_STATIC_RESOURCE_CONFIG_PERIOD, LOAD_STATIC_RESOURCE_CONFIG_PERIOD, TimeUnit.SECONDS);
		}
	}

	/**
	 * @param paramString
	 * @return
	 */
	private static Properties loadLoaclProperties(String paramString) {
		FileInputStream localFileInputStream = null;
		Properties localExtendedProperties = new Properties();
		try {
			localFileInputStream = new FileInputStream(paramString);
			if (localFileInputStream != null) {
				localExtendedProperties.load(localFileInputStream);
			}
		} catch (IOException e) {
			logger.error("loadLoaclProperties " + paramString, e);
		} finally {
			IOUtils.closeQuietly(localFileInputStream);
		}
		return localExtendedProperties;
	}

	/**
	 * 下载文件至本地
	 * 
	 * @param source
	 * @param destination
	 * @throws java.io.IOException
	 */
	private static void download2LocalFile(String source, File destination) throws IOException {
		String data = HTTPLongClient.instance().geturlcontent(source);
		// 超时等原因导致未加载到数据
		if (StringUtils.isBlank(data)) {
			throw new IllegalArgumentException("load " + source + " fail");
		}
		// 写入本地，写入错误抛异常
		FileUtils.writeStringToFile(destination, data);
	}

	/**
	 * 加载配置文件到本地
	 * 
	 * @param configURL
	 *            远程文件地址
	 * @param localFile
	 *            本地配置文件
	 * @return boolean 是否加载成功
	 */
	private static boolean download2Local(String configURL, File localFile) {
		try {
			// 文件不存在直接加载到本地
			if (!localFile.exists()) {
				download2LocalFile(configURL, localFile);
				return true;
			}
			File localFileTmp = new File(localFile.getPath() + ".tmp"); // 本地临时文件地址
			download2LocalFile(configURL, localFileTmp);
			localFile.deleteOnExit();
			FileUtils.copyFile(localFileTmp, localFile);
			return true;
		} catch (Throwable e) {
			logger.error("load " + configURL + StrUtils.ex2Str(e));
		}
		return false;
	}

	/**
	 * 
	 * http://oss.aliyun.com/userweb/staticResourceConfig.properties
	 * 装载staticResourceConfig.properties至内存 staticResourceConfig.properties配置样例：
	 * #是否自动加载 autoReload=true
	 * 
	 * @return boolean 是否装载成功
	 */
	private static boolean loadStaticResourceConfig() {
		boolean isLoadStaticResourceConfigFinish = download2Local(staticResourceConfigURL,
				staticResourceConfigLocalFile);
		// staticResourceConfig.properties加载失败，立即停止自动加载等待下一轮更新
		if (!isLoadStaticResourceConfigFinish && !staticResourceConfigLocalFile.exists()) {
			isAutoReloadStaticResource = false;
			return false;
		}
		try {
			Properties loaclStaticResourceConfig = loadLoaclProperties(staticResourceConfigLocalFile.getPath());
			// 是否自动加载最新配置和staticResource.properties文件
			isAutoReloadStaticResource = Boolean.parseBoolean(loaclStaticResourceConfig.getProperty("autoReload"));
			logger.info("------------------ load file " + STATIC_RESOURCE_CONFIG_NAME + " finish --------------------");
			return true;
		} catch (Throwable e) {
			logger.error("loadStaticResourceConfig " + STATIC_RESOURCE_CONFIG_NAME, e);
		}
		return false;
	}

	/**
	 * 装载staticResource.properties至内存 staticResource.properties数据样例：
	 * /js/Public.js=/js/Public_2.js
	 * 
	 * @return boolean 是否装载成功
	 */
	private static boolean loadStaticResource() {
		if (!isAutoReloadStaticResource && MapUtils.isNotEmpty(staticResourceMap)) {
			// 停静态化时一定要先看到这个信息才能打包上传静态文件
			logger.error(" ----------------- load file " + STATIC_RESOURCE_NAME + " suspended -----------------");
			return true;
		}
		if (isAutoReloadStaticResource || !staticResourceLocalFile.exists()) {
			download2Local(staticResourceURL, staticResourceLocalFile);
		}
		try {
			Properties props = loadLoaclProperties(staticResourceLocalFile.getPath());
			if (MapUtils.isEmpty(props)) {
				return false; // 远程文件没有加载到，本地也不存在历史文件
			}
			Map<String, String> staticResourceContentTmpMap = new HashMap<String, String>(10000);
			for (Map.Entry<Object, Object> entry : props.entrySet()) {
				staticResourceContentTmpMap.put((String) entry.getKey(), (String) entry.getValue());
			}
			try {
				lock.lock();
				staticResourceMap = staticResourceContentTmpMap;
			} finally {
				lock.unlock();
			}
			logger.info("------------------ load file " + STATIC_RESOURCE_NAME + " finish --------------------");
		} catch (Throwable e) {
			logger.error("loadStaticResource " + STATIC_RESOURCE_NAME, e);
			return false;
		}
		return true;

	}

	/**
	 * 根据静态文件名获取最新版本的静态文件url
	 *
	 * @param key
	 * @return
	 */
	public static String getURL(String key) {
		if (StringUtils.isBlank(key)) {
			return "";
		}
		// 开发模式，使用本地
		if (isDevEnv) {
			return staticResourceURL + key;
		}
		String url = null;
		try {
			url = staticResourceMap.get(key);
		} catch (Exception e) {
			logger.error("getURL() key:" + key);
		}
		return StringUtils.isBlank(url) ? "" : url;
	}
}