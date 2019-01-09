package com.inno72.pusher.dto;

import java.util.List;

public class PushMultiToOneBean {
	
	private List<TargetInfoBean> targets;
	
	private String msgType;
	
	private String data;
	
	private int isQueue = 0;


	public List<TargetInfoBean> getTargets() {
		return targets;
	}

	public void setTargets(List<TargetInfoBean> targets) {
		this.targets = targets;
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

	public String getMsgType() {
		return msgType;
	}

	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}

	
	
	
	
	

}
