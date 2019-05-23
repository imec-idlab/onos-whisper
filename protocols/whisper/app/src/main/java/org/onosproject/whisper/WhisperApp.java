/*
 * Copyright 2019-present Open Networking Foundation	
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onosproject.whisper;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import org.onosproject.net.DeviceId;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;

import static org.onlab.util.Tools.namedThreads;
import org.onlab.rest.AbstractWebApplication;	

import org.onosproject.whisper.controller.WhisperMessageListener;
import org.onosproject.whisper.controller.WhisperHostListener;
import org.onosproject.whisper.controller.WhisperSensorNodeListener;
import org.onosproject.whisper.controller.WhisperController;
import org.onosproject.whisper.datamodel.SensorNodeId;
import org.onosproject.whisper.datamodel.SensorNode;
import org.onosproject.whisper.datamodel.WirelessLink;
import org.onosproject.whisper.rest.WhisperWebResource;

import org.onosproject.net.HostId;
import org.onlab.packet.MacAddress;
import org.onlab.packet.VlanId;

import com.google.common.collect.Sets;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Skeletal ONOS application component.
 * emunicio 
 */

@Component(immediate = true)
public class WhisperApp implements WhisperController {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final ExecutorService executorMsgs =
            Executors.newFixedThreadPool(32,
                    namedThreads("whisper-event-stats-%d"));

    private final ExecutorService executorBarrier =
            Executors.newFixedThreadPool(4,
                    namedThreads("whisper-event-barrier-%d"));
    
    private ChannelGroup cg;
    
    private Channel channel;

    protected int whisperPort = 9999;
    protected int workerThreads = 0;

    protected long systemStartTime;

    protected Set<WhisperMessageListener> whisperMessageListeners = Sets.newHashSet();
    
    protected Set<WhisperHostListener> whisperHostListeners = Sets.newHashSet();
    
    protected Set<WhisperSensorNodeListener> whisperSensorNodeListeners = Sets.newHashSet();
    
    protected ConcurrentHashMap<DeviceId, SensorNode> connectedSensorNodes = new ConcurrentHashMap<>();
    
    protected ConcurrentHashMap<String, HostId> connectedHosts = new ConcurrentHashMap<>();

    protected ConcurrentHashMap<String, WirelessLink> connectedLinks = new ConcurrentHashMap<>();
    
    private WhisperChannelHandler channelHandler;
    private NioServerSocketChannelFactory execFactory;

    protected static final int SEND_BUFFER_SIZE = 4 * 1024 * 1024;
    protected static final int RCV_BUFFER_SIZE = 10240;

    public long getSystemStartTime() {
        return this.systemStartTime;
    }
     
    @Activate
    protected void activate() {
        log.info("Started Whisper");
        channelHandler = new WhisperChannelHandler(this);
        this.run();
        log.info("Done.");
    }

    @Deactivate
    protected void deactivate() {
    	log.info("Stopping Whisper IO");
    	execFactory.shutdown();
    	cg.close();
        log.info("Stopped");
    }
          
    public void run() {

        try {
        	log.info("Starting thread Whisper");
        	
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void addMessageListener(WhisperMessageListener wListener) {
        if (!this.whisperMessageListeners.contains(wListener)) {
            this.whisperMessageListeners.add(wListener);
        }
    }

    @Override
    public void addHostListener(WhisperHostListener hListener) {
        if (!this.whisperHostListeners.contains(hListener)) {
            this.whisperHostListeners.add(hListener);
        }
    }
    
    @Override
    public void addSensorNodeListener(WhisperSensorNodeListener wListener) {
        if (!this.whisperSensorNodeListeners.contains(wListener)) {
            this.whisperSensorNodeListeners.add(wListener);
        }
    }
    
    @Override
    public void removeSensorNodeListener(WhisperSensorNodeListener wListener) {
        this.whisperSensorNodeListeners.remove(wListener);
    }
    
    @Override
    public Iterable<SensorNode> getNodes() {
        return connectedSensorNodes.values();
    }

    @Override
    public SensorNode getNode(DeviceId id) {
        return connectedSensorNodes.get(id);
    }
    
    @Override
    public Iterable<HostId> getHosts() {
        return connectedHosts.values();
    }
    
    @Override
    public HostId getHost(String s) {
        return connectedHosts.get(s);
    }  
    
    @Override
    public Iterable<WirelessLink> getLinks() {
        return connectedLinks.values();
    }

    @Override
    public WirelessLink getLink(String id) {
        return connectedLinks.get(id);
    }
    
    @Override
    public void putLink(WirelessLink link) {
        connectedLinks.put(link.getStringId(),link);
    }
    
    public void processMessage(ObjectNode jsonTree){
    	
	    log.info("Processing REST message");
	    
	    SensorNode snode = new SensorNode(jsonTree);  
	    SensorNodeId id = snode.getId();

		for (WhisperMessageListener wListener : whisperMessageListeners) {
		    	wListener.handleMessage(jsonTree);
		}
		
        MacAddress mac = snode.getId().getMacAddressHost();
        VlanId vlanId = VlanId.vlanId((short) -1);
		HostId hostId = HostId.hostId(mac, vlanId);
		
	    if (connectedHosts.get(hostId.toString()) != null) {
		      log.info("Host already exists skipping new ");
	          return;
		}
		
	    connectedHosts.put(hostId.toString(),hostId);
	    if (!jsonTree.get("macParent").toString().equals("\"false\"")){
	    	log.info("Adding virtual host to " + id.toString());
	    	
		    for (WhisperHostListener hListener : whisperHostListeners) {
		    	hListener.addVirtualHost(snode);
		    }
	    }else{
	    	log.info("Not adding virtual host because this is the root");
	    }
    }
    
    public boolean addConnectedSensorNode(ObjectNode jsonNode){
	    log.info("Adding node...");

	    SensorNode snode = new SensorNode(jsonNode);  
	    SensorNodeId id = snode.getId();

	    if (connectedSensorNodes.get(DeviceId.deviceId(id.uri())) != null) {
		      log.info("Node already exists skipping new Device");
	          return false;
		}
	    
	    connectedSensorNodes.put(DeviceId.deviceId(id.uri()),snode);
	    log.info("Added node " + id.uri().toString());
	    for (WhisperSensorNodeListener wListener : whisperSensorNodeListeners) {
	    	wListener.handleNewSensorNode(snode);
	    }

	    return true;	    
    }

    public void removeConnectedSensorNode(SensorNode node){
	    log.info("Removing node...");
	    
	    for (WhisperSensorNodeListener wListener : whisperSensorNodeListeners) {
	    	wListener.handleDeprecatedSensorNode(node);
	    }
    }
    
    public boolean sendWhisperSouthBandMessage(String type, String data){
	    log.info("Sending Whisper message to the IoT network...");	    	      
	    return this.channelHandler.write(type,data);
   
    }
   
   
}
