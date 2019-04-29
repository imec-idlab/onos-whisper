package org.onosproject.whisper.controller;

import com.fasterxml.jackson.databind.node.ObjectNode;

import org.onosproject.whisper.datamodel.SensorNodeId;
import org.onosproject.whisper.datamodel.SensorNode;
import org.onosproject.net.DeviceId;
/**
 * emunicio 
 */
public interface WhisperController {

    public void processMessage(ObjectNode jsonTree);
    	
    public Iterable<SensorNode> getNodes();

    public SensorNode getNode(DeviceId id);
   
    public boolean addConnectedSensorNode(SensorNodeId id, boolean isRoot);

    public void removeConnectedSensorNode(SensorNodeId id);
	
    public void addMessageListener(WhisperMessageListener wListener);
    
    public void addSensorNodeListener(WhisperSensorNodeListener wListener);
    
    public void removeSensorNodeListener(WhisperSensorNodeListener wListener);
    
    public boolean sendWhisperMessage(String val);

}
