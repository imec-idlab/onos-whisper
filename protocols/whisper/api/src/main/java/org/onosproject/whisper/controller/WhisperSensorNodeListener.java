package org.onosproject.whisper.controller;

import org.onosproject.whisper.datamodel.SensorNodeId;
/**
 * emunicio 
 */
public interface WhisperSensorNodeListener {
   
    public void handleNewSensorNode(SensorNodeId nodeId,boolean isRoot);

    public void handleDeprecatedSensorNode(SensorNodeId nodeId);
}
