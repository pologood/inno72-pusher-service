package com.inno72.pusher.dto;

public class PushOneBean {
	
	private String targetCode;
	
	private String targetType;
	
	private String msgType;
	
	private int isEncrypt = 1;
	
	private String data;
	
	private int isQueue = 0;


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

	public String getTargetCode() {
		return targetCode;
	}

	public void setTargetCode(String targetCode) {
		this.targetCode = targetCode;
	}

	public String getTargetType() {
		return targetType;
	}

	public void setTargetType(String targetType) {
		this.targetType = targetType;
	}

	public String getMsgType() {
		return msgType;
	}

	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}

	public int getIsEncrypt() {
		return isEncrypt;
	}

	public void setIsEncrypt(int isEncrypt) {
		this.isEncrypt = isEncrypt;
	}
	
	

}
