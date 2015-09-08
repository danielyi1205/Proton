package com.mingle.proton.utils;

import java.io.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author chentong
 * 
 */
public class FileTool {

	private static final boolean IS_WIN = -1 != System.getProperty("os.name").toLowerCase().indexOf("windows");

	private static final String CLASS_PATH = FileTool.class.getResource("/").getPath();

	/**
	 * 从包package中获取所有的Class
	 * 
	 * @param packageName
	 * @return
	 */
	public static Set<Class<?>> getClasses(String packageName) {
		// 第一个class类的集合
		Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
		// 是否循环迭代
		boolean recursive = true;
		// 获取包的名字 并进行替换
		String packageDirName = packageName.replace('.', '/');
		// 定义一个枚举的集合 并进行循环来处理这个目录下的things
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		try {
			for (Enumeration<URL> dirs = cl.getResources(packageDirName); dirs.hasMoreElements();) {
				URL url = dirs.nextElement();
				// 得到协议的名称
				String protocol = url.getProtocol();
				// 如果是以文件的形式保存在服务器上
				if ("file".equals(protocol)) {// "file类型的扫描"
					// 获取包的物理路径
					String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
					// 以文件的方式扫描整个包下的文件 并添加到集合中
					findAndAddClassesInPackageByFile(cl, packageName, filePath, recursive, classes);
				} else if ("jar".equals(protocol)) {
					try {
						// 获取jar
						JarFile jar = ((JarURLConnection) url.openConnection()).getJarFile();
						for (Enumeration<JarEntry> entries = jar.entries(); entries.hasMoreElements();) {
							// 获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
							JarEntry entry = entries.nextElement();
							String name = entry.getName();
							// 如果是以/开头的
							if (name.charAt(0) == '/') {
								// 获取后面的字符串
								name = name.substring(1);
							}
							if (!name.startsWith(packageDirName)) {
								// 如果前半部分和定义的包名不同跳过
								continue;
							}
							int idx = name.lastIndexOf('/');
							// 如果以"/"结尾 是一个包
							if (idx != -1) {
								// 获取包名 把"/"替换成"."
								packageName = name.substring(0, idx).replace('/', '.');
							}
							// 如果可以迭代下去 并且是一个包
							if (recursive || idx != -1) {
								// 如果是一个.class文件 而且不是目录
								if (name.endsWith(".class") && !entry.isDirectory()) {
									// 去掉后面的".class" 获取真正的类名
									String className = name.substring(packageName.length() + 1, name.length() - 6);
									try {
										classes.add(cl.loadClass(packageName + '.' + className));
									} catch (ClassNotFoundException e) {
									}
								}
							}
						}
					} catch (IOException e) {
						// log.error("在扫描用户定义视图时从jar包获取文件出错");
					}
				}// jar
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return classes;
	}

	/**
	 * 以文件的形式来获取包下的所有Class
	 * 
	 * @param cl
	 *            ClassLoader
	 * @param packageName
	 * @param packagePath
	 * @param recursive
	 * @param classes
	 */
	private static void findAndAddClassesInPackageByFile(ClassLoader cl, String packageName, String packagePath,
			final boolean recursive, Set<Class<?>> classes) {
		// 获取此包的目录 建立一个File
		File dir = new File(packagePath);
		// 如果不存在或者 也不是目录就直接返回
		if (!dir.exists() || !dir.isDirectory()) {
			// log.warn("用户定义包名 " + packageName + " 下没有任何文件");
			return;
		}
		// 如果存在 就获取包下的所有文件 包括目录
		File[] dirfiles = dir.listFiles(new FileFilter() {
			// 自定义过滤规则 如果可以循环(包含子目录) 或则是以.class结尾的文件(编译好的java类文件)
			public boolean accept(File file) {
				return file.getName().endsWith(".class") || (recursive && file.isDirectory());
			}
		});
		// 循环所有文件
		for (File file : dirfiles) {
			// 如果是目录 则继续扫描
			if (file.isDirectory()) {
				findAndAddClassesInPackageByFile(cl, packageName + "." + file.getName(), file.getAbsolutePath(),
						recursive, classes);
				continue;
			}
			// 如果是java类文件 去掉后面的.class 只留下类名
			String className = file.getName().substring(0, file.getName().length() - 6);
			try {
				classes.add(cl.loadClass(packageName + '.' + className));
			} catch (ClassNotFoundException e) {
			}
		}
	}

	public static void writeFile(String fileFullPath, String charsetName, Collection<String> lines) {
		new File(fileFullPath).delete();
		BufferedWriter bw = null;
		try {
			bw = getBw(fileFullPath, charsetName);
			for (String line : lines) {
				bw.append(line + "\r\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalArgumentException(e.getMessage());
		} finally {
			close(bw);
		}
	}

	public static BufferedWriter getBw(String fileName, String charsetName) throws UnsupportedEncodingException,
			FileNotFoundException {
		return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), charsetName));
	}

	public static BufferedReader getBr(String fileName, String charsetName) throws UnsupportedEncodingException,
			FileNotFoundException {
		return new BufferedReader(new InputStreamReader(new FileInputStream(fileName), charsetName));
	}

	public static void close(BufferedReader br) {
		if (null != br) {
			try {
				br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	public static void close(BufferedWriter bw) {
		if (null != bw) {
			try {
				bw.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	public static List<String[]> readFile4Init(String filePathName) {
		return readFile4InitByFilePath(filePathName, "UTF-8");
	}

	/**
	 * 读取txt的数据转换成List<String[]>的格式返回，每一行的元素作为一个List的元素，每一行采用制表符作为分隔符
	 * 
	 * @param filePathName
	 * @param charsetName
	 *            字符编码名称
	 * @return List<String[]>
	 */
	public static List<String[]> readFile4InitByFilePath(String filePathName, String charsetName) {
		InputStream in = null;
		try {
			in = new FileInputStream(filePathName);
			return readFile4Init(in, charsetName);
		} catch (IOException ex) {
			throw new IllegalArgumentException(ex.getMessage());
		} finally {
			try {
				if (null != in) {
					in.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * 读取txt的数据转换成List<String[]>的格式返回，每一行的元素作为一个List的元素，每一行采用制表符作为分隔符
	 * 
	 * @param in
	 * @param charsetName
	 *            字符编码名称
	 * @return List<String[]>
	 */
	public static List<String[]> readFile4Init(InputStream in, String charsetName) {
		List<String[]> list = new LinkedList<String[]>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(in, charsetName));
			int nullNum = 0;
			while (nullNum < 2) {// 超过2个空行才认为是读完了
				String line = br.readLine();
				if (null == line) {
					nullNum++;
					continue;
				}
				if (0 != line.trim().length()) {
					list.add(line.split(","));
				}
			}
		} catch (IOException ex) {
			throw new IllegalArgumentException(ex.getMessage());
		} finally {
			try {
				if (null != br) {
					br.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return list;
	}

	public static List<String> readFile(String fullFileName, String charsetName) {
		return readFile(new File(fullFileName), charsetName);
	}

	public static List<String> readFileInClassPath(String fileName, String charsetName) {
		return readFile(getClassPath() + fileName, charsetName);
	}

	public static List<String> readFile(File file, String charsetName) {
		try {
			return readFile4Init2Strs(new FileInputStream(file), charsetName);
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
			throw new IllegalArgumentException(ex);
		}
	}

	/**
	 * 读取txt的数据转换成List<String>的格式返回，每一行的元素作为一个List的元素
	 * 
	 * @param in
	 * @param charsetName
	 *            字符编码名称
	 * @return List<String>
	 */
	public static List<String> readFile4Init2Strs(InputStream in, String charsetName) {
		List<String> list = new LinkedList<String>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(in, charsetName));
			int nullNum = 0;
			while (nullNum < 2) {// 超过2个空行才认为是读完了
				String line = br.readLine();
				if (null == line) {
					nullNum++;
					continue;
				}
				if (0 != line.trim().length()) {
					list.add(line);
				}
			}
		} catch (IOException ex) {
			throw new IllegalArgumentException(ex.getMessage());
		} finally {
			try {
				if (null != br) {
					br.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return list;
	}

	/**
	 * 将字节数组形式的文件写入磁盘文件(主要用于该类的模拟测试)
	 * 
	 * @param datas
	 *            字节数组形式的文件
	 * @param desFile
	 *            要写入的文件
	 */
	public static void writeFile(byte[] datas, String fileName) {
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(fileName);
			out.write(datas);
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (null != out) {
					out.flush();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			try {
				if (null != out) {
					out.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * 从文件中读取字节流
	 * 
	 * @param fileName
	 *            文件名
	 * @return byte[]
	 */
	public static byte[] getFileData(String fileName) {
		return getFileData(new File(fileName));
	}

	/**
	 * 从文件中读取字节流
	 * 
	 * @param file
	 *            File
	 * @return byte[]
	 */
	public static byte[] getFileData(File file) {
		byte[] datas = null;
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(file, "r");
			datas = new byte[(int) raf.length()];
			raf.read(datas);
		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		} finally {
			try {
				if (raf != null) {
					raf.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return datas;
	}

	public static byte[] inputstream2Bytes(InputStream is) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			for (int read = 0; -1 != (read = is.read());) {
				baos.write(read);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
			throw new IllegalArgumentException(ex.getMessage());
		} finally {
			try {
				if (null != is) {
					is.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return baos.toByteArray();
	}

	/**
	 * 从一个现有的byte数组中去指定的长度的数据，通常的目的是去掉后面的空余部分
	 * 
	 * @param src
	 *            原始byte数组
	 * @param count
	 *            指定的长度
	 * @return byte[]
	 */
	public static byte[] getBytesWithCount(final byte[] src, int count) {
		byte[] datas = new byte[count];
		System.arraycopy(src, 0, datas, 0, count);
		return datas;
	}

	/**
	 * 判断当前操作系统是否是windows
	 * 
	 * @return boolean
	 */
	public static boolean isWin() {
		return IS_WIN;
	}

	/**
	 * 字节到字符串的转换,采用utf-8进行转换，如果发生异常返回null
	 * 
	 * @param byteArray
	 *            字节数组
	 * @return 字符串
	 */
	public static String bytesToStr(byte[] byteArray) {
		try {
			return new String(byteArray, "UTF-8");
		} catch (UnsupportedEncodingException uee) {
			uee.printStackTrace();
			return null;
		}
	}

	/**
	 * 得到一个文件的前缀部分，如果没有“.”分隔符，返回null
	 * 
	 * @param fileName
	 *            String
	 * @return prefix
	 */
	public static String getFileNamePrefix(String fileName) {
		int dotPost = fileName.lastIndexOf('.');
		return dotPost == -1 ? null : fileName.substring(0, dotPost);
	}

	/**
	 * 得到一个文件的后缀部分，如果没有“.”分隔符，返回null
	 * 
	 * @param fileName
	 *            String
	 * @return suffix
	 */
	public static String getFileNameSuffix(String fileName) {
		int dotPost = fileName.lastIndexOf('.');
		return dotPost == -1 ? null : fileName.substring(dotPost + 1);
	}

	/**
	 * 得到当前ClassLoader的在操作系统中的绝对路径
	 * 
	 * @return
	 */
	public static String getClassPath() {
		return CLASS_PATH;
	}

}
