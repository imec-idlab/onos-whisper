package org.onosproject.whisper.controller;

import org.onosproject.whisper.datamodel.SensorNode;
/**
 * emunicio 
 */
public interface WhisperSensorNodeListener {
   
    public void handleNewSensorNode(SensorNode node);

    public void handleDeprecatedSensorNode(SensorNode node);
}
