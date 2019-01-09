package com.inno72.pusher.dto;

import java.util.List;

import com.inno72.pusher.remoting.common.Pair;

public class PushMultiToMultiBean {
	
	private List<Pair<TargetInfoBean, String>> peers;
	
	private String msgType;
	
	private int isQueue = 0;

	public List<Pair<TargetInfoBean, String>> getPeers() {
		return peers;
	}

	public void setPeers(List<Pair<TargetInfoBean, String>> peers) {
		this.peers = peers;
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
