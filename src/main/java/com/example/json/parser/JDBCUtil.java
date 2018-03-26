package com.example.json.parser;

import java.sql.*;

public class JDBCUtil {
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	static final String DB_URL = "jdbc:mysql://localhost/sys";
	static final String USER = "root";
	static final String PASS = "root";

	public void insert(BackupStorageDTO daoData) {
		Connection conn = null;
		Statement stmt = null;
		try {
			// STEP 2: Register JDBC driver
			Class.forName("com.mysql.jdbc.Driver");

			// STEP 3: Open a connection
			System.out.println("Connecting to a selected database...");
			conn = DriverManager.getConnection(DB_URL, USER, PASS);
			System.out.println("Connected database successfully...");

			// STEP 4: Execute a query
			System.out.println("Creating statement...");

			String masterServerName = daoData.getMasterName();
			String createDateTime = daoData.getCreateDateTime();
			for (BackupStorageAttributeDetails attr : daoData.getAttributes()) {
				stmt = conn.createStatement();
				String sql = "INSERT INTO sys.NB_BackupStorage VALUES('" 
						+ masterServerName+ "','" + attr.getClientName() + "','" + attr.getPolicyName() + "','"
						+ attr.getPolicyType() + "','" + attr.getStorageUnitName() + "','" + attr.getStorageType() + "',"
						+ attr.getMbytes() + ",'" + createDateTime + "');";
				stmt.executeUpdate(sql);
			}
		} catch (SQLException se) {
			// Handle errors for JDBC
			se.printStackTrace();
		} catch (Exception e) {
			// Handle errors for Class.forName
			e.printStackTrace();
		} finally {
			// finally block used to close resources
			try {
				if (stmt != null)
					conn.close();
			} catch (SQLException se) {
			} // do nothing
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			} // end finally try
		} // end try
		System.out.println("Goodbye!");
	}// end main
}
