package com.inno72.pusher.dto;

import org.apache.commons.lang.StringUtils;

public class TargetInfoBean {
	
	private String targetCode;
	
	private String targetType;
	
	
	public TargetInfoBean(String targetCode, String targetType) {
		this.targetCode = targetCode;
		this.targetType = targetType;
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
	
	public boolean check() {
		if(StringUtils.isBlank(this.targetCode) || StringUtils.isBlank(this.targetType))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((targetCode == null) ? 0 : targetCode.hashCode());
		result = prime * result + ((targetType == null) ? 0 : targetType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		TargetInfoBean other = (TargetInfoBean) obj;
		if (targetCode == null) {
			if (other.targetCode != null) return false;
		} else if (!targetCode.equals(other.targetCode)) return false;
		if (targetType == null) {
			if (other.targetType != null) return false;
		} else if (!targetType.equals(other.targetType)) return false;
		return true;
	}

	@Override
	public String toString() {
		return "[targetCode=" + targetCode + ", targetType=" + targetType + "]";
	}
	
	
	
	
}
