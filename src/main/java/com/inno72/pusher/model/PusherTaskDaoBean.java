package com.inno72.pusher.model;

import com.inno72.pusher.dto.TargetInfoBean;
import org.apache.commons.lang3.StringUtils;

public class PusherTaskDaoBean {
	
	private Long id;
	
	private String targetCode;
	
	private String targetType;
	
	private int type;
	
	private Integer queueLevel = 0;
	
	private byte[] message;
	
	private Integer status = 0;
	
	private Long createTime;
	
	private Long updateTime;
	
	private Integer times = 0;

	private String msgType;
	
	
	public PusherTaskDaoBean() {
	}
	
	public PusherTaskDaoBean(PusherTaskDaoBean bean) {
		this.id = bean.id == null ? null : bean.id.longValue();
		this.targetCode = bean.targetCode;
		this.targetType = bean.targetType;
		this.type = bean.type;
		this.queueLevel = bean.queueLevel;
		this.message = bean.message;
		this.status = bean.status == null ? null : bean.status.intValue();
		this.createTime = bean.createTime == null ? null : bean.createTime.longValue();
		this.updateTime = bean.updateTime == null ? null : bean.updateTime.longValue();
		this.times = bean.times == null ? null : bean.times.intValue();
	}
	

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public Integer getQueueLevel() {
		return queueLevel;
	}

	public void setQueueLevel(Integer queueLevel) {
		this.queueLevel = queueLevel;
	}

	public byte[] getMessage() {
		return message;
	}

	public void setMessage(byte[] message) {
		this.message = message;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Long getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Long updateTime) {
		this.updateTime = updateTime;
	}

	public Long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Long createTime) {
		this.createTime = createTime;
	}

	public Integer getTimes() {
		return times;
	}

	public void setTimes(Integer times) {
		this.times = times;
	}

	
	public PusherTaskDaoBean clone() {
		return  new PusherTaskDaoBean(this);
	}
	
	public TargetInfoBean getTargetInfo() {
		return new TargetInfoBean(this.getTargetCode(), this.getTargetType());
	}

	public String getMsgType() {
		return msgType;
	}

	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}

	public int getPriority() {
		if (StringUtils.isNotEmpty(this.msgType)) {
			if (this.msgType.equals("h5")) {
				return 1;
			}
			else if (this.msgType.equals("message")) {
				return 2;
			}
		}
		return 0;
	}
}
