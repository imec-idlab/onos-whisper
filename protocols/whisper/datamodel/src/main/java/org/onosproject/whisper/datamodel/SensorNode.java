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
 */
public class SensorNode {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
    private boolean connected;
    private boolean root;
    private boolean isWhisper;
    private SensorNodeId id=null;
    
    private InetAddress ipv6Addr;
    private InetAddress ipv4Addr;
        
    public SensorNode(ObjectNode jsonTree) {
    	
    	try{
    	
    		log.info("Creating SensorNode...");
    		
	        if ( (null == jsonTree.get("ipv6")) || (null == jsonTree.get("ipv4"))) {
	            throw new IllegalArgumentException("ipAddress isn't specified");
	        }
	        
	        if (null == jsonTree.get("mac")){
	            throw new IllegalArgumentException("macAddress isn't specified");
	        }
	    	
	    	if (jsonTree.get("macParent").toString().equals("\"false\"")){
	    		root=true;
	    	}else{
		    	if (jsonTree.get("isWhisperNode").toString().equals("\"false\"")){
		    		isWhisper=false;
		    	}else{
		    		isWhisper=true;
		    	}
	    		root=false;
	    	}
	    	
	    	
	    	
	    	String ipv6 = jsonTree.get("ipv6").toString().replace("\"", "");
	    	
	    	log.info("Checking adresses:"+ipv6);	 
	    	
	    	ipv6Addr = InetAddress.getByName(ipv6);  
	    	
	    	String ipv4 = jsonTree.get("ipv4").toString().replace("\"", "");
	    	log.info("Checking adresses:"+ipv4);
	    	
	    	ipv4Addr = InetAddress.getByName(ipv4); 
	        this.id = new SensorNodeId(jsonTree.get("mac").toString());
	        
    	}catch (UnknownHostException e) {
    		e.printStackTrace();      
    	}catch(IllegalArgumentException e){
    		e.printStackTrace();  
    	}
    }

    public String getStringId(){
    	return this.id.toString();
    }

    public String getStringIpv6(){
    	return this.ipv6Addr.toString();
    }

    public String getStringIpv4(){
    	return this.ipv4Addr.toString();
    }
 
    public SensorNodeId getId(){
    	return this.id;
    }

    public boolean isRoot(){
    	return this.root;
    }
    
    public boolean isWhisperNode(){
    	return this.isWhisper;
    }

    public boolean isConnected(){
    	return this.connected;
    }

    public void setConnected(boolean set){
    	this.connected = set;
    }

    
//    public Iterable<String> getNeighborNodes() {
//        return neighbors.values();
//    }
}
