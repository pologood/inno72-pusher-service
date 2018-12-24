package com.inno72.pusher.dto;

import java.util.List;

public class PushMultiToOneBean {
	
	private List<String> machineCodes;
	
	private String data;
	
	private int isQueue = 0;

	public List<String> getMachineCodes() {
		return machineCodes;
	}

	public void setMachineCodes(List<String> machineCodes) {
		this.machineCodes = machineCodes;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public int getIsQueue() {
		return isQueue;
	}

	public void setIsQueue(int isQueue) {
		this.isQueue = isQueue;
	}

	
	
	
	
	

}
