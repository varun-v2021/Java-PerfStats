package com.example.json.cache.sampleapp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
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
import com.example.json.parser.JsonParserUtil;
import com.sun.management.OperatingSystemMXBean;

public class App {

	final static Logger logger = Logger.getLogger(App.class);

	//Default values initialization
	static boolean flag = true;
	final MonitorObject monitor = new MonitorObject();
	volatile String fileSizeReadable = "0";
	private ReadWriteLock rwlock = new ReentrantReadWriteLock();
	private String fileNameUnderTest = "sample.json";
	private static final long MEGABYTE = 1024L * 1024L;
	private int stepCount = 1;
	private int incrementBy = 10;
	List<ObjectStat> objList = new ArrayList<ObjectStat>();
	private static String type = "json";

	public static long bytesToMegabytes(long bytes) {
		return bytes / MEGABYTE;
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		
		JsonParserUtil util = new JsonParserUtil();
		util.parse();
		/*
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
			obj.type = prop.getProperty("dataType");
			obj.trigger();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		*/
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
							obj.setDataType(type);

							logger.info("=========================================================");
							logger.info("File data type: " + obj.getDataType());
							logger.info("File size: " + obj.getSize());
							logger.info("System CPU Load: " + obj.getCpuUsage() + "%");
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
				if (type.equals("json")) {
					JSONObject jo = new JSONObject();
					{
						jo.put("firstName" + count, "Example firstname");
						jo.put("lastName" + count, "Example lastname");
						jo.put("age" + count, 25);
					}
					pw.append(jo.toJSONString());
				} else if (type.equals("txt")) {
					pw.append("ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890");
				}
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
		try {
			if (type.equals("json")) {
				File jsonInputFile = new File(fileName);
				InputStream is;
				is = new FileInputStream(jsonInputFile);
				JsonReader reader = Json.createReader(is);
				JsonObject empObj = reader.readObject();
				reader.close();
			} else if (type.equals("txt")) {
				FileReader reader = new FileReader(fileName);
				BufferedReader bufferedReader = new BufferedReader(reader);
				String line;
				while ((line = bufferedReader.readLine()) != null) {
					// dummy read line
				}
				reader.close();
			}
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
