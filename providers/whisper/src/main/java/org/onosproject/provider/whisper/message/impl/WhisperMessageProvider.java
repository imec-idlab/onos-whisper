package org.onosproject.provider.whisper.message.impl;

import org.onosproject.net.DefaultAnnotations;
import java.nio.ByteBuffer;				
import java.util.List;

import com.google.common.collect.Lists;

import org.onosproject.net.Device;	
import org.onosproject.net.DeviceId;
import org.onosproject.cluster.NodeId;
import org.onosproject.cluster.ClusterService;
import org.onlab.packet.ChassisId;
import org.onlab.packet.Ethernet;
import org.onosproject.net.Port;
import org.onosproject.net.Link;

import org.onlab.osgi.ServiceDirectory;
import org.onlab.osgi.DefaultServiceDirectory;

import org.onosproject.net.ConnectPoint;
import static org.onosproject.net.MastershipRole.MASTER;

import org.onosproject.net.SparseAnnotations;
import org.onosproject.net.device.DeviceEvent;
import org.onosproject.net.device.DeviceListener;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.link.DefaultLinkDescription;
import org.onosproject.net.link.LinkDescription;
import org.onosproject.net.link.LinkProvider;	
import org.onosproject.net.device.DefaultDeviceDescription;
import org.onosproject.net.device.DeviceDescription;
import org.onosproject.mastership.MastershipAdminService;
import org.onosproject.net.device.DeviceProviderRegistry;
import org.onosproject.net.device.DeviceProviderService;
import org.onosproject.net.device.PortDescription;
import org.onosproject.net.device.DefaultPortDescription;
import org.onosproject.net.PortNumber;
import org.onosproject.net.link.LinkProviderRegistry;
import org.onosproject.net.link.LinkProviderService;
import org.onosproject.net.provider.AbstractProvider;
import org.onosproject.net.provider.ProviderId;
import org.onosproject.net.device.DeviceProvider;
import org.onosproject.net.MastershipRole;

import org.onosproject.whisper.controller.WhisperController;
import org.onosproject.whisper.controller.WhisperMessageListener;
import org.onosproject.whisper.datamodel.SensorNodeId;
import org.onosproject.whisper.datamodel.SensorNode;
import org.onosproject.whisper.datamodel.WirelessLink;
import org.onosproject.whisper.datamodel.ControllerEntry;

import org.onosproject.whisper.WhisperApp;

import java.util.Iterator;
import com.fasterxml.jackson.databind.JsonNode;

import static org.onlab.util.Tools.toHex;

import org.osgi.service.component.annotations.Activate;	
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * emunicio 
 */

@Component(immediate = true)
public class WhisperMessageProvider extends AbstractProvider implements LinkProvider{ 

    private static final Logger LOG = LoggerFactory.getLogger(WhisperMessageProvider.class);
   
    protected int hostCount = 1;
    protected int infrastructurePorts = 10;   
    
    static final String SCHEME = "whisper";
    
    private LinkProviderService linkProviderService;
  
    private Object lock = new Object();
    
    private Map<DeviceId, LinkDescription> linkDescriptions = new ConcurrentHashMap<>();
    
    private Map<String, Long> sensorPortsUsedPerDevice = new ConcurrentHashMap<>();

    private Map<String, Long> sensorPortsUsedPerLink = new ConcurrentHashMap<>();
    
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected WhisperController controller;
    
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected LinkProviderRegistry linkProviderRegistry;
    
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected DeviceService deviceService;
    
    private InternalWhisperProvider wListener = new InternalWhisperProvider();
    
    private InternalDeviceListener deviceListener = new InternalDeviceListener();

    /**
     * Creates a provider with the supplier identifier.
     */
    public WhisperMessageProvider() {
        super(new ProviderId("whisper", "org.onosproject.provider.whisper.message"));
        LOG.warn("Initializing Whisper Message Provider");
    }
    
    @Activate
    public void activate() {
    	LOG.warn("Trying to activate the controller");
        linkProviderService = linkProviderRegistry.register(this);
        deviceService.addListener(deviceListener);
    	  	
    	controller.addMessageListener(wListener);    	
        LOG.warn("Started Whisper Message Provider");
    }

    @Deactivate
    public void deactivate() {
    	linkProviderRegistry.unregister(this);
        linkProviderService = null;
        deviceService=null;
        LOG.warn("Stopped Whisper Packet Provider");        
    }

    
    /**
     * Internal Packet Provider implementation.
     *
     */
    private class InternalWhisperProvider implements WhisperMessageListener {

        @Override
        public void handleMessage(ObjectNode jsonTree) {
        	LOG.warn("Internal Whisper Handling of a message called from the listener: "+jsonTree.toString());
        	
        	LOG.info("Received macParent: "+jsonTree.get("macParent").toString());
        	LOG.info("Received macParent: "+"\"false\"");
        	if (jsonTree.get("macParent").toString().equals("\"false\"")){
        		LOG.info("This is the root node");
        	
	        	SensorNodeId nodeId = new SensorNodeId(jsonTree.get("mac").toString());
	        	 	       	
				LOG.info("Received message from ROOT of SRC {} ",nodeId.uri());
				
				DeviceId incumbentDeviceId = DeviceId.deviceId(nodeId.uri());
						
				SensorNode incumentNode = controller.getNode(incumbentDeviceId);
				
	            String dpidStr = "of:0000000000000001";

	            DeviceId connectionSwitchId = DeviceId.deviceId(dpidStr);
          
				Long curPortNumber = sensorPortsUsedPerDevice.get(incumbentDeviceId.uri().toString());
						
				long portConnection = 0;
				if (curPortNumber != null) {
					//portConnection = curPortNumber.longValue();
					LOG.info("Keep alive from the ROOT, link already exists, nothing to do");	
					return;
				}
				portConnection++;
				
				ConnectPoint connectPoint = new ConnectPoint(incumbentDeviceId, PortNumber.portNumber(portConnection));
				sensorPortsUsedPerLink.put(incumbentDeviceId.uri().toString()+"-"+connectionSwitchId.uri().toString(), portConnection);					
				sensorPortsUsedPerDevice.put(incumbentDeviceId.uri().toString(), portConnection);	            
				
				portConnection = 1;
	            PortNumber portNumber = PortNumber.portNumber(portConnection);
	            
	            SparseAnnotations linkAnnotations = DefaultAnnotations.builder()
	                    .set("metric", "1")
	                    .build();
	            
	            ConnectPoint switchConnectPoint = new ConnectPoint(connectionSwitchId, portNumber);
	            sensorPortsUsedPerLink.put(connectionSwitchId.uri().toString()+"-"+incumbentDeviceId.uri().toString(), portConnection);
	            LinkDescription sensorSwitchLinkDescription = new DefaultLinkDescription(connectPoint, switchConnectPoint, Link.Type.DIRECT,linkAnnotations);
	            
	            if (deviceService.getDevice(incumbentDeviceId) != null) {
	                linkProviderService.linkDetected(sensorSwitchLinkDescription);
	                LOG.info("Link detected");
	            } else {
	                linkDescriptions.put(incumbentDeviceId, sensorSwitchLinkDescription);
	                LOG.info("Adding new link");
	            }
	            

	            sensorSwitchLinkDescription = new DefaultLinkDescription(switchConnectPoint, connectPoint, Link.Type.DIRECT,linkAnnotations);
	            
	            if (deviceService.getDevice(incumbentDeviceId) != null) {
	                linkProviderService.linkDetected(sensorSwitchLinkDescription);
	                LOG.info("Link detected reversed");               
	            } else {
	                linkDescriptions.put(incumbentDeviceId, sensorSwitchLinkDescription);
	                LOG.info("Adding new link reversed");
	            }
	            LOG.info("Connecting SINK {} with OFSwitch {}", connectPoint.deviceId(), switchConnectPoint.deviceId());
	            ControllerEntry wc= new ControllerEntry(incumbentDeviceId.uri().toString(),0,0,1);
	            controller.putWhisperController(wc);
        	  
        	}else{
        		
        		LOG.info("This is not the root node");
        		
            	SensorNodeId nodeId = new SensorNodeId(jsonTree.get("mac").toString());
            	SensorNodeId parentId = new SensorNodeId(jsonTree.get("macParent").toString());	
            	
            	SensorNode nodeTarget=controller.getNode(DeviceId.deviceId(nodeId.uri()));
            	
            	LOG.info("Checking if there has been a parent change...");  
            	LOG.info("Received message of SRC {} his current parent is {}",nodeId.uri(), parentId.uri());  
            	LOG.info("The previous stored parent of {} was {}",nodeId.uri(), nodeTarget.getParentId().uri()); 
            	
            	if (nodeTarget.getParentId().uri().equals(parentId.uri())){
            		addLinkFromNodeToNode(nodeId,parentId,true);			//if it exists, it will be discarded there
            	}else{
            		LOG.info("There seems to be a parent change");
            		
            		WirelessLink link=new WirelessLink(nodeId,parentId);
                	if (linkExists(nodeId,parentId)==false){
            	    	  LOG.info("Adding new Wireless Link");
                		  addLinkFromNodeToNode(nodeId,parentId,true);		//it the link does not exists, create it normally with the new parent
            		}else{
            		    	LOG.info("link already exists: "+link.getStringId()+" Updating link with new parent");
            		    	LOG.info("The link to be updated is between {} and new parent {}",nodeId.uri(), parentId.uri());  
            		    	updateLinkParentship(nodeId,parentId,true);
            		}
            		
                    if (linkExists(nodeId,nodeTarget.getParentId())==false){
              	    	  LOG.info("Error. Parent detected without existing link!");
                  		  //this should never happen, TODO throw exception
              		}else{
              		    	LOG.info("link already exists: "+link.getStringId()+" Updating link with old parent");
              		    	LOG.info("The link to be updated is between {} and old parent {}",nodeId.uri(), nodeTarget.getParentId().uri()); 
              		    	updateLinkParentship(nodeId,nodeTarget.getParentId(),false);
              		}
                    nodeTarget.setPreferredParent(parentId);
            	}           	
            	
        		//Updating neighbours normally
        		LOG.info("Adding neighbors links...");  
            	          	            
    	        Iterator<JsonNode> neighborNodes = jsonTree.get("neighbors").elements();
    	        
                while (neighborNodes.hasNext()) {
                	JsonNode neighbor = neighborNodes.next();   
                	SensorNodeId neighId = new SensorNodeId(neighbor.get("mac").toString());
                	
                	LOG.info("Node {} has neighbor {}",nodeId.uri(), neighId.uri());  
                	
                	DeviceId neighDeviceId = DeviceId.deviceId(neighId.uri());
                	
                	if (controller.getNode(neighDeviceId) != null) {
                		LOG.info("Node {} already exist. tying to add link...", neighId.uri()); 
                		LOG.info("My parent is {} This neighbor is {} ",jsonTree.get("macParent").toString(),neighbor.get("mac").toString());
                		if (jsonTree.get("macParent").toString().equals(neighbor.get("mac").toString())){
                			LOG.info("Node this link is with my parent, already added"); 
                		}else{
                			
                			addLinkFromNodeToNode(nodeId,neighId,false);
                		}
                	}else{
                		LOG.info("Node {} does not exist yet", neighId.uri()); 
                	}
                }
        	}
        }
               
        private boolean linkExists(SensorNodeId node1, SensorNodeId node2){
        	
        	WirelessLink link=new WirelessLink(node1,node2);
        	
        	boolean found=false;
        	
        	Iterable<WirelessLink> wLinks = controller.getLinks();
	        
        	for(WirelessLink l: wLinks){

            	LOG.info("checking link "+l.getStringId());
            	if( (l.getStringId().equals(link.getStringId())) || (l.getReversedStringId().equals(link.getStringId())) ){
            		found=true;          		
            		LOG.info("link already exists: "+link.getStringId());
            		break;
            	}
            }
        	if (!found){
        		LOG.info("link does not exist yet: "+link.getStringId());
        	}
        	return found;
        }
        
        private void updateLinkParentship(SensorNodeId node1, SensorNodeId node2,boolean childParent) {
        	
        	DeviceId incumbentDeviceId = DeviceId.deviceId(node1.uri());
        	DeviceId neighDeviceId = DeviceId.deviceId(node2.uri());
        	SparseAnnotations linkAnnotations;
            if (childParent){   
            	LOG.info("setting new metric to 1");
	            linkAnnotations = DefaultAnnotations.builder()
	                    .set("metric", "1")
	                    .build();
            }else{
            	LOG.info("setting new metric to 999");
	            linkAnnotations = DefaultAnnotations.builder()
	                    .set("metric", "999")
	                    .build();
            }
        	
            LOG.info("Updating link between {} and {}",node1.uri(), node2.uri());
            
            long portConnection=sensorPortsUsedPerLink.get(incumbentDeviceId.uri().toString()+"-"+neighDeviceId.uri().toString());          
    		ConnectPoint nodeConnectPoint = new ConnectPoint(incumbentDeviceId, PortNumber.portNumber(portConnection));
    		LOG.info("Node 1 using port "+portConnection);    
    		
    		
    		portConnection=sensorPortsUsedPerLink.get(neighDeviceId.uri().toString()+"-"+incumbentDeviceId.uri().toString());
            ConnectPoint parentConnectPoint = new ConnectPoint(neighDeviceId, PortNumber.portNumber(portConnection));
            LOG.info("Node 2 using port "+portConnection);  
                                   
            LinkDescription sensorLinkDescription = new DefaultLinkDescription(nodeConnectPoint, parentConnectPoint, Link.Type.DIRECT,linkAnnotations);          
            linkProviderService.linkDetected(sensorLinkDescription);
            LOG.info("Link updated");
            
            sensorLinkDescription = new DefaultLinkDescription(parentConnectPoint, nodeConnectPoint, Link.Type.DIRECT,linkAnnotations);            
            linkProviderService.linkDetected(sensorLinkDescription);
            LOG.info("Link updated reversed");
        }
        
        private void addLinkFromNodeToNode(SensorNodeId node1, SensorNodeId node2,boolean childParent) {
        	                   	
        	WirelessLink link=new WirelessLink(node1,node2);
        	if (linkExists(node1,node2)==false){
        		 
    	    	        LOG.info("Adding new Wireless Link");
    		        controller.putLink(link);
    			Iterable<ControllerEntry> nWhispControllers = controller.getWhisperControllers();
    		    	for(ControllerEntry wc: nWhispControllers){
    		    		if (wc.id().equals("whisper:4B:00:06:13:06:56")){	//parse with the correct controller id
    		    			wc.setLinks(wc.links()+1);
    		    		}
    		    	}
    		}else{
    		    	LOG.info("link already exists: "+link.getStringId());
    		    	return;
    		}

        	 LOG.info("Creating a NEW link between {} and {}",node1.uri(), node2.uri());
        	
			DeviceId incumbentDeviceId = DeviceId.deviceId(node1.uri());
			DeviceId neighDeviceId = DeviceId.deviceId(node2.uri());				
			SensorNode incumentNode = controller.getNode(incumbentDeviceId);
        				
			Long curPortNumber = sensorPortsUsedPerDevice.get(incumbentDeviceId.uri().toString());
			 
			
			long portConnection = 0;
			if (curPortNumber != null) {
				portConnection = curPortNumber.longValue();
			}
			LOG.info("Node 1 was using port "+portConnection);  
			portConnection++;
						       		
    		ConnectPoint nodeConnectPoint = new ConnectPoint(incumbentDeviceId, PortNumber.portNumber(portConnection));
    		sensorPortsUsedPerLink.put(incumbentDeviceId.uri().toString()+"-"+neighDeviceId.uri().toString(), portConnection);
    		sensorPortsUsedPerDevice.put(incumbentDeviceId.uri().toString(), portConnection); 
    		
       
			curPortNumber = sensorPortsUsedPerDevice.get(neighDeviceId.uri().toString());
			
			portConnection = 0;
			if (curPortNumber != null) {
				portConnection = curPortNumber.longValue();
			}
			LOG.info("Node 2 was using port "+portConnection); 
			
			portConnection++;
            ConnectPoint parentConnectPoint = new ConnectPoint(neighDeviceId, PortNumber.portNumber(portConnection));
            sensorPortsUsedPerLink.put(neighDeviceId.uri().toString()+"-"+incumbentDeviceId.uri().toString(), portConnection);
            sensorPortsUsedPerDevice.put(neighDeviceId.uri().toString(), portConnection);  	
              
             
            SparseAnnotations linkAnnotations;
            
            if (childParent){ 
            	LOG.info("using metric to 1");
	            linkAnnotations = DefaultAnnotations.builder()
	                    .set("metric", "1")
	                    .build();
            }else{
            	LOG.info("using metric to 999");
	            linkAnnotations = DefaultAnnotations.builder()
	                    .set("metric", "999")
	                    .build();
            }
            
            LinkDescription sensorLinkDescription = new DefaultLinkDescription(nodeConnectPoint, parentConnectPoint, Link.Type.DIRECT,linkAnnotations);
           
            if (deviceService.getDevice(incumbentDeviceId) != null) {
                linkProviderService.linkDetected(sensorLinkDescription);
                LOG.info("Link detected");
            } else {
                linkDescriptions.put(incumbentDeviceId, sensorLinkDescription);
                LOG.info("Adding new link");
            }
            
            sensorLinkDescription = new DefaultLinkDescription(parentConnectPoint, nodeConnectPoint, Link.Type.DIRECT,linkAnnotations);
            
            if (deviceService.getDevice(incumbentDeviceId) != null) {
                linkProviderService.linkDetected(sensorLinkDescription);
                LOG.info("Link detected reversed");               
            } else {
                linkDescriptions.put(incumbentDeviceId, sensorLinkDescription);
                LOG.info("Adding new link reversed");
            }
        } 
    }
    
    private class InternalDeviceListener implements DeviceListener {

        @Override
        public void event(DeviceEvent event) {
        	 	
            if (event.type().equals(DeviceEvent.Type.DEVICE_ADDED)) {
            	Device device = event.subject();
            	LOG.info("Device {} received Event {}", device.id(),event.toString());
                LinkDescription linkDescription = linkDescriptions.get(device.id());
                if (linkDescription != null) {
                    LOG.info("Found link {}", linkDescription);
                    linkProviderService.linkDetected(linkDescription);
                    linkDescriptions.remove(device.id());
                }
            }
        }
    }
  
}

