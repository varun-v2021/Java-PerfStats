package com.example.json.parser;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.sql.*;

public class JsonParserUtil {

	public void parse() throws JsonParseException, JsonMappingException, IOException{
		
		String input ="{\"createDateTime\": \"2018-03-08 10:35:19\","
				+ "\"masterName\": \"lacibl13vm20.rsv.ven.veritas.com\","
				+ "\"attributes\": [{"
				+ "\"clientName\": \"Client 1\","
				+ "\"mbytes\": 0,"
				+ "\"policyName\": \"basicPolicy41\","
				+ "\"policyType\": \"MS-Windows\","
				+ "\"storageType\": \"basicdisk\","
				+ "\"storageUnitName\": \"fortyone\"},"
				+"{"
				+ "\"clientName\": \"Chandran\","
				+ "\"mbytes\": 0,"
				+ "\"policyName\": \"basicPolicy42\","
				+ "\"policyType\": \"MS-Windows\","
				+ "\"storageType\": \"advanceddisk\","
				+ "\"storageUnitName\": \"fortytwo\""				
				+ "}]"
				+ "}";
		
		ObjectMapper objectMapper = new ObjectMapper();
		BackupStorageDTO data = objectMapper.readValue(input, BackupStorageDTO.class);
		
		System.out.println("Master name: "+data.getMasterName());
		System.out.println("Created date time: "+data.getCreateDateTime());
		
		for(BackupStorageAttributeDetails attr: data.getAttributes()){
			System.out.println("Client: "+attr.getClientName());
			System.out.println("MBytes: "+attr.getMbytes());
			System.out.println("PolicyName: "+attr.getPolicyName());
			System.out.println("PolicyType: "+attr.getPolicyType());
			System.out.println("Storage Unitname: "+attr.getStorageUnitName());
			System.out.println("Storage Type: "+attr.getStorageType());			
		}
		
		JDBCUtil jdbcUtil = new JDBCUtil();
		jdbcUtil.insert(data);
	}
}
