package com.inno72.pusher.model;

public class PusherTaskDaoBean {
	
	private Long id;
	
	private String targetCode;
	
	private Integer queueLevel = 0;
	
	private String message;
	
	private Integer status = 0;
	
	private Long createTime;
	
	private Long updateTime;
	
	private Integer times = 0;
	
	
	public PusherTaskDaoBean() {
	}
	
	public PusherTaskDaoBean(PusherTaskDaoBean bean) {
		this.id = bean.id == null ? null : bean.id.longValue();
		this.targetCode = bean.targetCode == null ? null : bean.targetCode;
		this.queueLevel = bean.queueLevel == null ? null : bean.queueLevel;
		this.message = bean.message == null ? null : bean.message;
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

	public Integer getQueueLevel() {
		return queueLevel;
	}

	public void setQueueLevel(Integer queueLevel) {
		this.queueLevel = queueLevel;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
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
	
	
	

}
