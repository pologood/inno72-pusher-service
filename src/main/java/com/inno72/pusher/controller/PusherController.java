package com.inno72.pusher.controller;

import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.inno72.common.Result;
import com.inno72.pusher.common.Constants;
import com.inno72.pusher.dto.KickOffTargetsBean;
import com.inno72.pusher.dto.PushMultiToMultiBean;
import com.inno72.pusher.dto.PushMultiToOneBean;
import com.inno72.pusher.dto.PushOneBean;
import com.inno72.pusher.dto.SendDataMsgBean;
import com.inno72.pusher.dto.TargetInfoBean;
import com.inno72.pusher.model.PusherTaskDaoBean;
import com.inno72.pusher.remoting.common.ClientManager;
import com.inno72.pusher.remoting.common.Pair;
import com.inno72.pusher.service.IdWorker;
import com.inno72.pusher.service.PusherTaskService;

@RestController
@RequestMapping("/pusher")
public class PusherController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Resource
	private ClientManager clientManager;

	@Resource
	private PusherTaskService pusherTaskService;


	@Resource
	private IdWorker idWorker;

	@RequestMapping(value = "/push/one")
	public Result<Void> pushOne(@RequestHeader(required=false, value="MsgType") String msgType, @RequestBody PushOneBean reqBean) throws UnsupportedEncodingException {

		logger.info("msgType is {}, reqBean msgType is {}", msgType, reqBean.getMsgType());

		if (StringUtils.isBlank(reqBean.getTargetCode()) || StringUtils.isBlank(reqBean.getTargetType())
				|| StringUtils.isBlank(reqBean.getData())) {

			Result<Void> rspBean = new Result<Void>();
			rspBean.setCode(-1);
			rspBean.setMsg("param error");

			return rspBean;

		}

		long currentTime = System.currentTimeMillis();

		PusherTaskDaoBean bean = new PusherTaskDaoBean();

		bean.setCreateTime(currentTime);
		bean.setUpdateTime(currentTime);
		bean.setId(idWorker.nextId());
		bean.setTargetCode(reqBean.getTargetCode());
		bean.setTargetType(reqBean.getTargetType());
		bean.setType(Constants.MSG_TYPE_TEXT);

		String realMsgType = null;
		if(StringUtils.isNotBlank(reqBean.getMsgType())) {
			realMsgType = reqBean.getMsgType();
		}else if(StringUtils.isNotBlank(msgType)) {
			realMsgType = msgType;
		}
		
		bean.setMessage(JSON.toJSONString(new SendDataMsgBean(realMsgType, reqBean.getData(), reqBean.getIsEncrypt())).getBytes("utf-8"));
		bean.setMsgType(realMsgType);

		if (reqBean.getIsQueue() == 1) {
			clientManager.sendMsg(bean, pusherTaskService);
		} else {
			clientManager.sendMsg(bean, null);
		}


		Result<Void> rspBean = new Result<Void>();
		rspBean.setCode(0);
		rspBean.setMsg("ok");

		return rspBean;

	}

	@RequestMapping(value = "/push/multi/multi")
	public Result<Void> pushMultiToMulti(@RequestHeader(required=false, value="MsgType") String msgType, @RequestBody PushMultiToMultiBean reqBean) throws UnsupportedEncodingException {

		if (reqBean.getPeers() == null || reqBean.getPeers().isEmpty()) {

			Result<Void> rspBean = new Result<Void>();
			rspBean.setCode(-1);
			rspBean.setMsg("param error");

			return rspBean;

		}

		List<Pair<TargetInfoBean, String>> peers = reqBean.getPeers();

		long currentTime = System.currentTimeMillis();

		for (Pair<TargetInfoBean, String> peer : peers) {

			PusherTaskDaoBean bean = new PusherTaskDaoBean();

			bean.setCreateTime(currentTime);
			bean.setUpdateTime(currentTime);
			bean.setId(idWorker.nextId());
			if (peer.getFirst() == null || StringUtils.isBlank(peer.getFirst().getTargetCode())
					|| StringUtils.isBlank(peer.getFirst().getTargetType()) || StringUtils.isBlank(peer.getSecond())) {

				Result<Void> rspBean = new Result<Void>();
				rspBean.setCode(-1);
				rspBean.setMsg("param error");

				return rspBean;

			}
			bean.setTargetCode(peer.getFirst().getTargetCode());
			bean.setTargetType(peer.getFirst().getTargetType());
			bean.setType(Constants.MSG_TYPE_TEXT);
			
			String realMsgType = null;
			if(StringUtils.isNotBlank(reqBean.getMsgType())) {
				realMsgType = reqBean.getMsgType();
			}else if(StringUtils.isNotBlank(msgType)) {
				realMsgType = msgType;
			}
			
			bean.setMessage(JSON.toJSONString(new SendDataMsgBean(realMsgType, peer.getSecond(), reqBean.getIsEncrypt())).getBytes("utf-8"));
			bean.setMessage(peer.getSecond().getBytes("utf-8"));

			if (reqBean.getIsQueue() == 1) {
				clientManager.sendMsg(bean, pusherTaskService);
			} else {
				clientManager.sendMsg(bean, null);
			}
		}

		Result<Void> rspBean = new Result<Void>();
		rspBean.setCode(0);
		rspBean.setMsg("ok");

		return rspBean;
	}


	@RequestMapping(value = "/push/multi/one")
	public Result<Void> pushMultiToOne(@RequestHeader(required=false, value="MsgType") String msgType, @RequestBody PushMultiToOneBean reqBean) throws UnsupportedEncodingException {

		if (reqBean.getTargets() == null || reqBean.getTargets().isEmpty() || StringUtils.isBlank(reqBean.getData())) {

			Result<Void> rspBean = new Result<Void>();
			rspBean.setCode(-1);
			rspBean.setMsg("param error");

			return rspBean;

		}

		long currentTime = System.currentTimeMillis();

		for (TargetInfoBean targetBean : reqBean.getTargets()) {

			PusherTaskDaoBean bean = new PusherTaskDaoBean();

			bean.setCreateTime(currentTime);
			bean.setUpdateTime(currentTime);
			bean.setId(idWorker.nextId());

			if (StringUtils.isBlank(targetBean.getTargetCode()) || StringUtils.isBlank(targetBean.getTargetType())) {
				Result<Void> rspBean = new Result<Void>();
				rspBean.setCode(-1);
				rspBean.setMsg("param error");

				return rspBean;

			}
			
			bean.setTargetCode(targetBean.getTargetCode());
			bean.setTargetType(targetBean.getTargetType());
			bean.setType(Constants.MSG_TYPE_TEXT);

			
			String realMsgType = null;
			if(StringUtils.isNotBlank(reqBean.getMsgType())) {
				realMsgType = reqBean.getMsgType();
			}else if(StringUtils.isNotBlank(msgType)) {
				realMsgType = msgType;
			}
			
			bean.setMessage(JSON.toJSONString(new SendDataMsgBean(realMsgType, reqBean.getData(), reqBean.getIsEncrypt())).getBytes("utf-8"));
			bean.setMsgType(realMsgType);

			if (reqBean.getIsQueue() == 1) {
				clientManager.sendMsg(bean, pusherTaskService);
			} else {
				clientManager.sendMsg(bean, null);
			}
		}

		Result<Void> rspBean = new Result<Void>();
		rspBean.setCode(0);
		rspBean.setMsg("ok");

		return rspBean;
	}


	@RequestMapping(value = "/push/map")
	public Result<List<Pair<String, TargetInfoBean>>> pushMap() {

		List<Pair<String, TargetInfoBean>> ret = clientManager.getKeyChannelMap();

		Result<List<Pair<String, TargetInfoBean>>> rspBean = new Result<List<Pair<String, TargetInfoBean>>>();
		rspBean.setCode(0);
		rspBean.setData(ret);
		rspBean.setMsg("ok");

		return rspBean;
	}

	@RequestMapping(value = "/reload/service")
	public Result<Void> reloadService() {

		pusherTaskService.loadServiceMap();

		Result<Void> rspBean = new Result<Void>();
		rspBean.setCode(0);
		rspBean.setMsg("ok");
	
		return rspBean;
	}

	@RequestMapping(value = "/kickoff")
	public Result<Void> kickoff(@RequestBody KickOffTargetsBean reqBean) {

		if (reqBean.getTargets() == null || reqBean.getTargets().isEmpty()) {

			Result<Void> rspBean = new Result<Void>();
			rspBean.setCode(-1);
			rspBean.setMsg("param error");

			return rspBean;

			 
		}
		
		for (TargetInfoBean targetInfo : reqBean.getTargets()) {
			if(targetInfo == null || !targetInfo.check())
				continue;
			clientManager.kickOffChannel(targetInfo);
		}

		Result<Void> rspBean = new Result<Void>();
		rspBean.setCode(0);
		rspBean.setMsg("ok");

		return rspBean;
	}


}
