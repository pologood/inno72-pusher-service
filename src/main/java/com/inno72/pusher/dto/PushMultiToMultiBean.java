package com.inno72.pusher.dto;

import java.util.Map;

public class PushMultiToMultiBean {
	
	
	private Map<String, String> peers;
	
	private int isQueue = 0;

	public Map<String, String> getPeers() {
		return peers;
	}

	public void setPeers(Map<String, String> peers) {
		this.peers = peers;
	}

	public int getIsQueue() {
		return isQueue;
	}

	public void setIsQueue(int isQueue) {
		this.isQueue = isQueue;
	}
	
	
	
	

}
