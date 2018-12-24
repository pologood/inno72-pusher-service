package com.inno72.pusher.common;

public class TransException extends Exception {


	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public String getMessage() {
		return toString();
	}

	private int ret;
	
	private String msg;
	
	public TransException(int ret, String msg){
		this.ret = ret;
		this.msg = msg;
	}

	public int getRet() {
		return ret;
	}

	public void setRet(int ret) {
		this.ret = ret;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	@Override
	public String toString() {
		return "code:" + ret + " msg:"+msg;
	}
	
}
