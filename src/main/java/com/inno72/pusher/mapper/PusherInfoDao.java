package com.inno72.pusher.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.inno72.pusher.model.PusherServiceDaoBean;
import com.inno72.pusher.model.PusherTaskDaoBean;

public interface PusherInfoDao {
	
	List<PusherServiceDaoBean> queryServiceMaps();
	
	List<PusherTaskDaoBean> queryAvailable();
	
	int insertPusherTask(PusherTaskDaoBean pusherTaskDaoBean);
	
	int updatePusherTaskStatus(@Param("id") long id, @Param("status") int status, @Param("updateTime") long updateTime);
	
	int updatePusherTaskQueueLevel(@Param("id") long id, @Param("queueLevel") int queueLevel,  @Param("times")int times, @Param("updateTime") long updateTime);
	
}
