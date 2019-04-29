package org.onosproject.whisper;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.timeout.IdleStateHandler;
import org.jboss.netty.handler.timeout.ReadTimeoutHandler;
import org.jboss.netty.util.CharsetUtil;
import org.jboss.netty.util.ExternalResourceReleasable;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * emunicio
 */
public class WhisperPipelineFactory implements ChannelPipelineFactory, ExternalResourceReleasable {
    protected WhisperApp controller;
    protected ThreadPoolExecutor pipelineExecutor;
    protected Timer timer;
    protected IdleStateHandler idleHandler;
    protected ReadTimeoutHandler readTimeoutHandler;

    public WhisperPipelineFactory(WhisperApp controller, ThreadPoolExecutor pipelineExecutor) {
        super();
        this.controller = controller;
        this.pipelineExecutor = pipelineExecutor;
        this.timer = new HashedWheelTimer();
        this.idleHandler = new IdleStateHandler(timer, 0, 0, 0);
        this.readTimeoutHandler = new ReadTimeoutHandler(timer, 0);
    }

    @Override
    public ChannelPipeline getPipeline() throws Exception {
    	WhisperChannelHandler handler = new WhisperChannelHandler(controller);

        ChannelPipeline pipeline = Channels.pipeline();

        pipeline.addLast("idle", idleHandler);
        pipeline.addLast("timeout", readTimeoutHandler);
        if (pipelineExecutor != null) {
            pipeline.addLast("pipelineExecutor",
                    new ExecutionHandler(pipelineExecutor));
        }
        pipeline.addLast("handler", handler);
        pipeline.addLast("stringDecoder", new StringDecoder(CharsetUtil.UTF_8));
        pipeline.addLast("stringEncoder", new StringEncoder(CharsetUtil.UTF_8));
        return pipeline;
    }

    @Override
    public void releaseExternalResources() {
        timer.stop();
    }
}
