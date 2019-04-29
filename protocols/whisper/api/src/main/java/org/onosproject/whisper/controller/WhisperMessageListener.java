package org.onosproject.whisper.controller;

import com.fasterxml.jackson.databind.node.ObjectNode;
/**
 * emunicio 
 */
public interface WhisperMessageListener {
    public void handleMessage(ObjectNode jsonTree);
}
