package com.example.json.cache.sampleapp;

public class ObjectStat {
	private String size;
	private long maxHeapUsed;
	private long maxFreeHeap;
	private String cpuUsage;
	private long totalMemory;
	private String dataType;
	
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
	public String getDataType() {
		return dataType;
	}
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	
}
