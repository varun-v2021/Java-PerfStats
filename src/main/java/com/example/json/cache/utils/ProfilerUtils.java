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
