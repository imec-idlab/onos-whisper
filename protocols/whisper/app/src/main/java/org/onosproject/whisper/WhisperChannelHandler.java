package org.onosproject.whisper;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.timeout.IdleStateAwareChannelHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * emunicio.
 */
public class WhisperChannelHandler extends IdleStateAwareChannelHandler{
    private static final Logger log = LoggerFactory.getLogger(WhisperChannelHandler.class);
    private final WhisperApp controller;

    private Channel channel;
    byte[] prevRecvBuf = null;

    public WhisperChannelHandler(WhisperApp controller) {
        this.controller = controller;
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx,
                                 ChannelStateEvent e) throws Exception {
        channel = e.getChannel();        
        controller.setChannelHandlerController(this);        
        log.info("New network connection from {}", channel.getRemoteAddress());
       
    }

    @Override
    public void channelDisconnected(ChannelHandlerContext ctx,
                                    ChannelStateEvent e) throws Exception {
        log.info("Channel disconnected");
    }
      
    public boolean write(String msg) {
        if (msg != null) {
		boolean okToSend=true;
                byte[] buf = msg.getBytes();
                ChannelBuffer channelBuffer = ChannelBuffers.wrappedBuffer(buf);
                if (!channel.isConnected()) {
                    log.info("Channel not connected");
		    okToSend=false;
                }
                if (!channel.isOpen()) {
                    log.info("Channel not open");
		    okToSend=false;
                }
                if (!channel.isWritable()) {
                    log.info("Channel not writeable");
		    okToSend=false;
                }
                if (!channel.isReadable()) {
                    log.info("Channel not readable");
		    okToSend=false;
                }

		if (okToSend){
                	channel.write(channelBuffer);
            
		    	log.info("Sending message {}", msg.toString());
		    	log.info("Channel status is Connected {} Open {}",
		            channel.isConnected(),
		            channel.isOpen());
	    		return true;
		}else{
			return false;
		}
        } else {
        	log.warn("Null Message");
//            byte[] buf = {5, 0, 0, 0, 0};
//            ChannelBuffer channelBuffer = ChannelBuffers.wrappedBuffer(buf);
//            channel.write(channelBuffer);
		return false;
	    
        }
    }
     
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {    	
    	log.info("Message received from socket"); 	    	
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        if (e.getCause() instanceof IOException) {
            log.error("Disconnecting node");
            StackTraceElement[] stackTraceElements = e.getCause().getStackTrace();
            for (int i = 0; i < stackTraceElements.length; i++) {
                log.error(stackTraceElements[i].toString());
            }
        } else {
            log.error("Error while processing message from sensor node ");
            StackTraceElement[] stackTraceElements = e.getCause().getStackTrace();
            for (int i = 0; i < stackTraceElements.length; i++) {
                log.error(stackTraceElements[i].toString());
            }
        }
    }
}
