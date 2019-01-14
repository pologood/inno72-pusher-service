package com.inno72.pusher.dto;

public class SendDataMsgBean {
	
	private String msgType;
	
	private int isEncrypt;
	
	private String data;
	
	public SendDataMsgBean(String msgType, String data, int isEncrypt) {
		this.msgType = msgType;
		this.data = data;
		this.isEncrypt = isEncrypt;
	}

	public String getMsgType() {
		return msgType;
	}

	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public int getIsEncrypt() {
		return isEncrypt;
	}

	public void setIsEncrypt(int isEncrypt) {
		this.isEncrypt = isEncrypt;
	}

}
