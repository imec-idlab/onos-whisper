package org.onosproject.whisper.datamodel;

import java.util.List;

/**
 * emunicio
 */
public class SensorNode {
	
    private boolean connected;
    private boolean root;
    private final SensorNodeId id;
        
    public SensorNode(SensorNodeId id) {
        this.id = id;
    }

    public String getStringId(){
    	return this.id.toString();
    }

    public SensorNodeId getId(){
    	return this.id;
    }
       
    public boolean isRoot(){
    	return this.root;
    }
   
    public boolean isConnected(){
    	return this.connected;
    }
    
    public void setConnected(boolean set){
    	this.connected = set;
    }
}
