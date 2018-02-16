# Java-PerfStats

package com.example.json.cache.sampleapp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import com.example.json.cache.utils.ProfilerUtils;
import com.sun.management.OperatingSystemMXBean;

public class App {

	final static Logger logger = Logger.getLogger(App.class);

	static boolean flag = true;
	final MonitorObject monitor = new MonitorObject();
	volatile String fileSizeReadable = "0";
	private ReadWriteLock rwlock = new ReentrantReadWriteLock();
	private String fileNameUnderTest = "sample.json";
	private static final long MEGABYTE = 1024L * 1024L;
	private int stepCount = 1;
	private int incrementBy = 10;
	List<ObjectStat> objList = new ArrayList<ObjectStat>();

	public static long bytesToMegabytes(long bytes) {
		return bytes / MEGABYTE;
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		ProfilerUtils utils = new ProfilerUtils();
		App obj = new App();
		Properties prop;
		try {
			if (args.length >= 1) {
				if (args[0].isEmpty()) {
					logger.info("Using default configuration");
					prop = utils.loadConfigFile("config.properties");
				} else {
					logger.info("Using configuration: " + args[0]);
					prop = utils.loadConfigFile(args[0]);
				}
			} else {
				logger.info("Using default configuration");
				prop = utils.loadConfigFile("config.properties");
			}
			obj.fileNameUnderTest = prop.getProperty("filename");
			obj.incrementBy = Integer.parseInt(prop.getProperty("incrementBy"));
			obj.stepCount = Integer.parseInt(prop.getProperty("stepCount"));
			obj.trigger();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void trigger() throws IOException, InterruptedException {
		Thread worker = new Thread(new Runnable() {
			public void run() {
				logger.info("Running profiler ...");
				ObjectStat obj = new ObjectStat();
				try {
					while (true && flag) {
						Thread.sleep(1);
						Runtime runtime = Runtime.getRuntime();
						OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
						long memory = runtime.totalMemory() - runtime.freeMemory();
						if (memory > obj.getMaxHeapUsed()) {
							obj = new ObjectStat();
							obj.setMaxHeapUsed(memory);
							obj.setSize(fileSizeReadable);
							obj.setMaxFreeHeap(runtime.freeMemory());
							obj.setCpuUsage(String.format("%.2f", osBean.getSystemCpuLoad() * 100));
							obj.setTotalMemory(runtime.totalMemory());

							logger.info("=========================================================");
							logger.info("System CPU Load: " + obj.getCpuUsage() + "%");
							logger.info("File size: " + obj.getSize());
							logger.info("Heap utilization statistics [MB]");
							logger.info("Used memory: " + bytesToMegabytes(obj.getMaxHeapUsed()));
							logger.info("Free Memory: " + bytesToMegabytes(obj.getMaxFreeHeap()));
							logger.info("Total Memory: " + bytesToMegabytes(obj.getTotalMemory()));
							logger.info("=========================================================");
							objList.add(obj);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		worker.start();
		write();
		flag = false;
		worker.join();
		deleteFile(fileNameUnderTest);
		logger.info("Stopping profiler ...");
	}

	public void write() throws IOException, InterruptedException {
		long stepValue = 1;
		for (int step = 0; step < stepCount; step++) {
			rwlock.writeLock().lock();
			stepValue *= incrementBy;
			PrintWriter pw = new PrintWriter(fileNameUnderTest);
			// clearing file contents
			pw.print("");
			pw.flush();
			for (int count = 0; count < stepValue; count++) {
				JSONObject jo = new JSONObject();
				{
					jo.put("firstName" + count, "Example firstname");
					jo.put("lastName" + count, "Example lastname");
					jo.put("age" + count, 25);
				}
				pw.append(jo.toJSONString());
			}
			pw.flush();
			pw.close();
			rwlock.writeLock().unlock();
			Thread.sleep(2000);
			read(fileNameUnderTest);
		}
	}

	public void read(String fileName) throws IOException, InterruptedException {
		rwlock.readLock().lock();
		File fileObj = new File(fileName);
		fileSizeReadable = FileUtils.byteCountToDisplaySize(fileObj.length());
		File jsonInputFile = new File(fileName);
		InputStream is;
		try {
			is = new FileInputStream(jsonInputFile);
			JsonReader reader = Json.createReader(is);
			JsonObject empObj = reader.readObject();
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		rwlock.readLock().unlock();
	}

	public void deleteFile(String fileName) {
		try {
			File file = new File(fileName);
			if (file.delete()) {
				logger.info("" + file.getName() + " is deleted!");
			} else {
				logger.info("Delete operation failed.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
======================================================================================================================================
package com.example.json.cache.sampleapp;

public class MonitorObject {

}

======================================================================================================================================
package com.example.json.cache.sampleapp;

public class ObjectStat {
	private String size;
	private long maxHeapUsed;
	private long maxFreeHeap;
	private String cpuUsage;
	private long totalMemory;
	
	public String getSize() {
		return size;
	}
	public void setSize(String size) {
		this.size = size;
	}
	public long getMaxHeapUsed() {
		return maxHeapUsed;
	}
	public void setMaxHeapUsed(long maxHeapUsed) {
		this.maxHeapUsed = maxHeapUsed;
	}
	public long getMaxFreeHeap() {
		return maxFreeHeap;
	}
	public void setMaxFreeHeap(long maxFreeHeap) {
		this.maxFreeHeap = maxFreeHeap;
	}
	public String getCpuUsage() {
		return cpuUsage;
	}
	public void setCpuUsage(String cpuUsage) {
		this.cpuUsage = cpuUsage;
	}
	public long getTotalMemory() {
		return totalMemory;
	}
	public void setTotalMemory(long totalMemory) {
		this.totalMemory = totalMemory;
	}
	
}

======================================================================================================================================
package com.example.json.cache.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.log4j.Logger;

public class ProfilerUtils {

	final static Logger logger = Logger.getLogger(ProfilerUtils.class);

	public Properties loadConfigFile(String config) {
		Properties properties = new Properties();
		try {
			File file = new File(config);
			FileInputStream fileInput = new FileInputStream(file);
			properties.load(fileInput);
			fileInput.close();

			/*
			 * Enumeration enuKeys = properties.keys(); while
			 * (enuKeys.hasMoreElements()) { String key = (String)
			 * enuKeys.nextElement(); String value =
			 * properties.getProperty(key); System.out.println(key + ": " +
			 * value); }
			 */
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return properties;
	}

	public void deleteFiles(final String fileExtension) {
		File parentDir = new File(System.getProperty("user.dir"));
		if (!parentDir.exists())
			logger.info(parentDir + " doesn't exist");
		File[] fList = parentDir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(fileExtension);
			}
		});
		if (fList.length == 0) {
			logger.info(parentDir + "doesn't have any file with extension " + fileExtension);
		} else {
			for (File f : fList) {
				if (f.delete()) {
					logger.info("" + f.getName() + " is deleted!");
				} else {
					logger.info(f.getName() + " Delete operation failed.");
				}
			}
		}
	}
}

=====================================================log4j.properties=================================================================================
# Root logger option
log4j.rootLogger=DEBUG, stdout, file

# Redirect log messages to console
log4j.appender.stdout=org.apache.log4j.ConsoleAppender
log4j.appender.stdout.Target=System.out
log4j.appender.stdout.layout=org.apache.log4j.PatternLayout
log4j.appender.stdout.layout.ConversionPattern=%d{yyyy-MM-dd HH:mm:ss} %-5p - %m%n
#log4j.appender.stdout.layout.ConversionPattern=%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n

# Redirect log messages to a log file, support file rolling.
log4j.appender.file=org.apache.log4j.RollingFileAppender
log4j.appender.file.File=profiler.log
log4j.appender.file.MaxFileSize=5MB
log4j.appender.file.MaxBackupIndex=5
log4j.appender.file.layout=org.apache.log4j.PatternLayout
log4j.appender.file.layout.ConversionPattern=%d{yyyy-MM-dd HH:mm:ss} %-5p - %m%n
log4j.appender.file.Append=false
===========================================================config.properties===========================================================================
filename=sample1.json
stepCount=7
incrementBy=10
================================================================pom.xml======================================================================
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>

	<groupId>com.example.json.cache</groupId>
	<artifactId>sampleapp</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<packaging>jar</packaging>

	<name>sampleapp</name>
	<url>http://maven.apache.org</url>

	<properties>
		<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
	</properties>

	<dependencies>
		<dependency>
			<groupId>com.googlecode.json-simple</groupId>
			<artifactId>json-simple</artifactId>
			<version>1.1.1</version>
		</dependency>
		<!-- https://mvnrepository.com/artifact/javax.json/javax.json-api -->
		<dependency>
			<groupId>javax.json</groupId>
			<artifactId>javax.json-api</artifactId>
			<version>1.0</version>
		</dependency>
		<dependency>
			<groupId>org.glassfish</groupId>
			<artifactId>javax.json</artifactId>
			<version>1.0.4</version>
		</dependency>
		<dependency>
			<groupId>org.apache.commons</groupId>
			<artifactId>commons-io</artifactId>
			<version>1.3.2</version>
		</dependency>
		<dependency>
			<groupId>log4j</groupId>
			<artifactId>log4j</artifactId>
			<version>1.2.17</version>
		</dependency>
	</dependencies>
</project>
======================================================================================================================================
