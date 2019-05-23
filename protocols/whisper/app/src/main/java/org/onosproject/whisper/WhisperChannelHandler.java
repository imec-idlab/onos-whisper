package org.onosproject.whisper;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.timeout.IdleStateAwareChannelHandler;

import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpVersion;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.concurrent.Executors;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpClientCodec;
import org.jboss.netty.util.CharsetUtil;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.net.URI;
import java.net.URISyntaxException;

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
public class WhisperChannelHandler{
    private static final Logger log = LoggerFactory.getLogger(WhisperChannelHandler.class);
    private final WhisperApp controller;

    private static StringBuffer buf = new StringBuffer();

    public WhisperChannelHandler(WhisperApp controller) {
        this.controller = controller;
    }

    public boolean write(String messageType,String msg) {
      	
        try {

        	if ( (msg != null) || (messageType != null) ) {
        	
        		String URL = System.getProperty("url", "http://127.0.0.1:9999/");
            	
            	URI uri = new URI(URL);
            	String scheme = uri.getScheme() == null? "http" : uri.getScheme();
            	String host = uri.getHost() == null? "127.0.0.1" : uri.getHost();
            	int port = uri.getPort();

            	ObjectMapper mapper = new ObjectMapper();
            	ObjectNode jNode = mapper.createObjectNode();
            	((ObjectNode) jNode).put("protocol", "onos-whisper-sb");
            	((ObjectNode) jNode).put("message", messageType);
            	((ObjectNode) jNode).put("data", msg);
            
    			ClientBootstrap client = new ClientBootstrap(
    				new NioClientSocketChannelFactory(
    						Executors.newCachedThreadPool(),
    						Executors.newCachedThreadPool()));

    			client.setPipelineFactory(new ClientPipelineFactory());

    			//Connect to server, wait till connection is established, get channel to write to
    			Channel channel = client.connect(new InetSocketAddress("127.0.0.1", 9999)).awaitUninterruptibly().getChannel();
    		
    			//Writing request to channel and wait till channel is closed from server
    			HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "test");
    			ChannelBuffer buffer = ChannelBuffers.copiedBuffer(jNode.toString(), Charset.defaultCharset());
    			request.headers().set(HttpHeaders.Names.CONTENT_TYPE, "application/json");
    			request.headers().set(HttpHeaders.Names.CONTENT_LENGTH, buffer.readableBytes());
    			request.setContent(buffer);

    			channel.write(request).awaitUninterruptibly().getChannel().getCloseFuture().awaitUninterruptibly();
    		     
    	    	log.info("Sent!");
    	    	return true;
            
        	}else{
        		log.warn("Message or Type is Null");
        		return false;
        	}

        }catch(URISyntaxException e){
            e.printStackTrace();
            return false;
        }
    }
       
	private static class ClientPipelineFactory implements ChannelPipelineFactory {

		@Override
		public ChannelPipeline getPipeline() throws Exception {
			ChannelPipeline pipeline = Channels.pipeline();

			pipeline.addLast("codec", new HttpClientCodec());

			pipeline.addLast("aggregator", new HttpChunkAggregator(1048576));

			pipeline.addLast("handler", new TestResponseHandler());
			return pipeline;
		}
	}
	
	private static class TestResponseHandler extends SimpleChannelUpstreamHandler {
		@Override
		public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
			HttpResponse response = (HttpResponse) e.getMessage();
			buf.append(response.getContent().toString(CharsetUtil.UTF_8));
			super.messageReceived(ctx, e);
		}
	}
}
