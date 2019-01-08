package com.inno72.pusher.remoting.netty;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.inno72.pusher.service.PusherTaskService;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;


@Scope("prototype")
@Component
public class TransactionHandle extends SimpleChannelInboundHandler<WebSocketFrame> {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Resource
	private PusherTaskService pusherTaskService;
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
		
		if (frame instanceof TextWebSocketFrame) {
			TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
			
			String info = textFrame.text();
			
			logger.info("TransactionHandle recv:" + info);
			
			try {
				JSONObject request = JSON.parseObject(info);
				
				String method = request.getString("method");
				
				String msgType = request.getString("msgType");
				
				Object param = request.get("param");
				
				pusherTaskService.handleWithRequest(method, msgType, param, ctx.channel());
				
			}catch (Exception e) {
				logger.warn(e.getMessage(), e);
				
			}
		
		}else {
			logger.warn("TransactionHandle not binary frame");
		}		
	}

}
