package com.inno72.pusher.remoting.netty;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inno72.pusher.remoting.ChannelEventListener;
import com.inno72.pusher.remoting.common.ServiceThread;

public class NettyEventExecutor extends ServiceThread {
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	 
	private final LinkedBlockingQueue<NettyEvent> eventQueue = new LinkedBlockingQueue<NettyEvent>();
    private final int maxSize = 10000;
    
    private ChannelEventListener channelEventListener;
    
    public NettyEventExecutor(ChannelEventListener channelEventListener) {
    	this.channelEventListener = channelEventListener;
    }

    public void putNettyEvent(final NettyEvent event) {
        if (this.eventQueue.size() <= maxSize) {
            this.eventQueue.add(event);
        } else {
            log.warn("event queue size[{}] enough, so drop this event {}", this.eventQueue.size(), event.toString());
        }
    }

    @Override
    public void run() {
        log.info(this.getServiceName() + " service started");

        final ChannelEventListener listener = channelEventListener;

        while (!this.isStopped()) {
            try {
                NettyEvent event = this.eventQueue.poll(3000, TimeUnit.MILLISECONDS);
                if (event != null && listener != null) {
                    switch (event.getType()) {
                        case IDLE:
                            listener.onChannelIdle(event.getRemoteAddr(), event.getChannel());
                            break;
                        case CLOSE:
                            listener.onChannelClose(event.getRemoteAddr(), event.getChannel());
                            break;
                        case CONNECT:
                            listener.onChannelConnect(event.getRemoteAddr(), event.getChannel());
                            break;
                        case EXCEPTION:
                            listener.onChannelException(event.getRemoteAddr(), event.getChannel());
                            break;
                        default:
                            break;

                    }
                }
            } catch (Exception e) {
                log.warn(this.getServiceName() + " service has exception. ", e);
            }
        }

        log.info(this.getServiceName() + " service end");
    }

    @Override
    public String getServiceName() {
        return NettyEventExecutor.class.getSimpleName();
    }

}
