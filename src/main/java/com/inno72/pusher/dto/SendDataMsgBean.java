package com.inno72.pusher.dto;

public class SendDataMsgBean {
	
	private String msgType;
	
	private String data;
	
	public SendDataMsgBean(String msgType, String data) {
		this.msgType = msgType;
		this.data = data;
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
	
	

}
