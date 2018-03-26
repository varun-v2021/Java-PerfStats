package com.example.json.parser;

import java.util.List;

public class BackupStorageDTO {

	private String masterName;
	private String createDateTime;
	private List<BackupStorageAttributeDetails> attributes;
	
	public String getMasterName() {
		return masterName;
	}
	public void setMasterName(String masterName) {
		this.masterName = masterName;
	}
	public String getCreateDateTime() {
		return createDateTime;
	}
	public void setCreateDateTime(String createDateTime) {
		this.createDateTime = createDateTime;
	}
	public List<BackupStorageAttributeDetails> getAttributes() {
		return attributes;
	}
	public void setAttributes(List<BackupStorageAttributeDetails> attributes) {
		this.attributes = attributes;
	}
}
