package com.inno72.pusher.dto;

public class PushOneBean {
	
	private String machineCode;
	
	private String data;
	
	private int isQueue = 0;

	public String getMachineCode() {
		return machineCode;
	}

	public void setMachineCode(String machineCode) {
		this.machineCode = machineCode;
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
