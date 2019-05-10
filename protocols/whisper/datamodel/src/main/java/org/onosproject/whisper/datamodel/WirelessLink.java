package org.onosproject.whisper.datamodel;

import java.util.List;
import com.fasterxml.jackson.databind.JsonNode;	
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.concurrent.ConcurrentHashMap;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.onosproject.net.DeviceId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * emunicio
 * 
 * Bidireccional Wireless Links
 * 
 */
public class WirelessLink {
	
    private final Logger log = LoggerFactory.getLogger(getClass());
	
    private boolean connected;
    private boolean root;
    private SensorNodeId node1=null;
    private SensorNodeId node2=null;
    private String m;
    private String mReversed;
    double pdr;
           
    public WirelessLink(SensorNodeId n1,SensorNodeId n2) {

    		log.info("Creating Wireless Link...");
    		node1=n1;
    		node2=n2;
    		m=node1.uri().toString()+"-"+node2.uri().toString();
    		mReversed=node2.uri().toString()+"-"+node1.uri().toString();
    		pdr=1.0;
    }

    public String getStringId(){

    	return m;
    }
    
    public String getReversedStringId(){

    	return mReversed;
    }
    
    public SensorNodeId getNode1(){
    	return this.node1;
    }

    public SensorNodeId getNode2(){
    	return this.node2;
    }

    public double getPDR(){
    	return this.pdr;
    }

}
