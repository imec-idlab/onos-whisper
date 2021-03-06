package org.onosproject.whisper.controller;

import com.fasterxml.jackson.databind.node.ObjectNode;

import org.onosproject.whisper.datamodel.SensorNodeId;
import org.onosproject.whisper.datamodel.SensorNode;
import org.onosproject.whisper.datamodel.WirelessLink;
import org.onosproject.whisper.datamodel.ControllerEntry;
import org.onosproject.net.DeviceId;
import org.onosproject.net.HostId;
/**
 * emunicio 
 */
public interface WhisperController {

    public void processMessage(ObjectNode jsonTree);

    public Iterable<SensorNode> getNodes();

    public SensorNode getNode(DeviceId id);

    public Iterable<HostId> getHosts();

    public HostId getHost(String s);

    public Iterable<WirelessLink> getLinks();

    public WirelessLink getLink(String id);

    public void putLink(WirelessLink link);

    public boolean addConnectedSensorNode(ObjectNode jsonNode);

    public void removeConnectedSensorNode(SensorNode node);

    public void addMessageListener(WhisperMessageListener wListener);

    public void addHostListener(WhisperHostListener hListener);

    public void addSensorNodeListener(WhisperSensorNodeListener wListener);

    public void removeSensorNodeListener(WhisperSensorNodeListener wListener);

    public boolean sendWhisperSouthBandMessage(String type, String data);

    public Iterable<ControllerEntry> getWhisperControllers();

    public ControllerEntry getWhisperController(String id);

    public void putWhisperController(ControllerEntry cont);
}
