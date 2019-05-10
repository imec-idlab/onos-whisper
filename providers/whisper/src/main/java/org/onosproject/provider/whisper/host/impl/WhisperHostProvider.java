package org.onosproject.provider.whisper.host.impl;

import java.nio.ByteBuffer;				
import java.util.List;

import com.google.common.collect.Lists;

//import org.onlab.packet.ChassisId;
//import org.onlab.osgi.ServiceDirectory;
//import org.onlab.osgi.DefaultServiceDirectory;
//
//import static org.onosproject.net.MastershipRole.MASTER;

//import org.onosproject.net.device.DeviceProviderService;
//import org.onosproject.net.device.PortDescription;
//import org.onosproject.net.device.DefaultPortDescription;
//import org.onosproject.net.PortNumber;
import org.onosproject.net.provider.AbstractProvider;
import org.onosproject.net.provider.ProviderId;
//import org.onosproject.net.device.DeviceProvider;
//import org.onosproject.net.MastershipRole;
//import org.onosproject.net.Port;
//import org.onosproject.net.link.LinkProvider;	
//import org.onosproject.net.device.DefaultDeviceDescription;
//import org.onosproject.net.device.DeviceDescription;
//import org.onosproject.mastership.MastershipAdminService;
//import org.onosproject.net.device.DeviceProviderRegistry;
//import org.onosproject.net.Device;	
//import org.onosproject.net.DeviceId;
//import org.onosproject.cluster.NodeId;
//import org.onosproject.cluster.ClusterService;


import org.onosproject.whisper.datamodel.SensorNodeId;
import org.onosproject.whisper.datamodel.SensorNode;
import static org.onosproject.net.DeviceId.deviceId;
import com.fasterxml.jackson.databind.JsonNode;	
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.onosproject.whisper.WhisperApp;
import org.onosproject.whisper.controller.WhisperController;
import org.onosproject.whisper.controller.WhisperHostListener;



import org.osgi.service.component.annotations.Activate;	
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import org.onosproject.net.host.DefaultHostDescription;
import org.onosproject.net.HostLocation;
import org.onosproject.net.Host;
import java.net.URI;
import org.onosproject.net.host.HostProviderRegistry;
import org.onosproject.net.host.HostProvider;
import org.onosproject.net.host.HostProviderService;
import org.onosproject.net.HostId;
import java.util.HashSet;
import org.onlab.packet.VlanId;
import org.onlab.packet.IpAddress;
import java.util.Set;
import org.onosproject.net.ConnectPoint;
import org.onlab.packet.MacAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * emunicio
 */

@Component(immediate = true)
public class WhisperHostProvider extends AbstractProvider implements HostProvider {

    private static final Logger LOG = LoggerFactory.getLogger(WhisperHostProvider.class);

//    protected int hostCount = 0;

//    protected int infrastructurePorts = 50;
//           
//    protected DeviceProviderService deviceProviderService;
    
    protected HostProviderService hostProviderService;
   
    static final String SCHEME = "whisper";
    
//    private InternalWhisperDeviceProvider wListener = new InternalWhisperDeviceProvider();
    
    private InternalHostProvider hostProvider = new InternalHostProvider();
  
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected WhisperController controller;
    
//    @Reference(cardinality = ReferenceCardinality.MANDATORY)
//    protected DeviceProviderRegistry deviceProviderRegistry;
    
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected HostProviderRegistry hostProviderRegistry;
    
    
    /**
     * Creates a provider with the supplier identifier.
     */
    public WhisperHostProvider() {
        super(new ProviderId("whisper", "org.onosproject.provider.whisper.host"));
        
        LOG.info("Initializing Whisper Host Provider ");
    }
    
  
    
    @Activate
    public void activate() {   	
    	hostProviderService = hostProviderRegistry.register(this);
    	
    	LOG.warn("Trying to add Host provider as listener to the controller");
    	controller.addHostListener(hostProvider);
    	    	
        LOG.info("Started Whisper Host Provider");
    }

    @Deactivate
    public void deactivate() {

    	hostProviderRegistry.unregister(this);
    	hostProviderService = null;
    	//controller.removeHostListener(hostProvider);

        LOG.warn("Stopped Whisper Device Provider");
    }
    
//    public void createAndAddHost(SensorNode node) {
//    	
//    	LOG.info("Creating Host");
//        //URI location;
//        //HostProviderRegistry hostProviderRegistry = get(HostProviderRegistry.class);
//        
//        //try {
//            // Parse the input stream
//            //ObjectNode root = readTreeFromStream(mapper(), stream);
//
//    	String loc=node.getId().uri().toString();
//        HostId hostId = hostProvider.parseHost(node);
//
////            UriBuilder locationBuilder = uriInfo.getBaseUriBuilder()
////                    .path("hosts")
////                    .path(hostId.mac().toString())
////                    .path(hostId.vlanId().toString());
////            location = locationBuilder.build();
////        } catch (IOException ex) {
////            throw new IllegalArgumentException(ex);
////        } finally {
////            hostProviderRegistry.unregister(hostProvider);
////        }
//        hostProviderRegistry.unregister(hostProvider);
//        LOG.info("Virtual Host created");
////        return Response
////                .created(location)
////                .build();
//    }

    
    public void triggerProbe(Host host) {
        // Not implemented since there is no need to check for hosts on network
    }
    

    /**
     * Internal host provider that provides host events.
     */
    private class InternalHostProvider implements WhisperHostListener {
      
        @Override
        public void addVirtualHost(SensorNode node) {
        	
        	LOG.info("Adding virtual Host to sensor");
            MacAddress mac = node.getId().getMacAddressHost();
            VlanId vlanId = VlanId.vlanId((short) -1);
            
//            if (null == node.get("locations")) {
//                throw new IllegalArgumentException("location isn't specified");
//            }

            String porthost="1";
            //Iterator<JsonNode> locationNodes = node.get("locations").elements();
            Set<HostLocation> locations = new HashSet<>();
//            while (locationNodes.hasNext()) {
//                JsonNode locationNode = locationNodes.next();
                
                String deviceAndPort = node.getId().uri().toString() + "/" + porthost;
                
                LOG.info("Host deviceAndPort "+deviceAndPort);
                HostLocation hostLocation = new HostLocation(ConnectPoint.deviceConnectPoint(deviceAndPort), 0);
                locations.add(hostLocation);
            //}

            //Iterator<JsonNode> ipNodes = node.get("ipAddresses").elements();
            Set<IpAddress> ips = new HashSet<>();
            //while (ipNodes.hasNext()) {
            LOG.info("Host with IPv6 "+node.getStringIpv6());
            //LOG.info("Host with IPv4 "+node.getStringIpv4());
            ips.add(IpAddress.valueOf(node.getStringIpv6().replace("/", "")));
            //}

            // try to remove elements from json node after reading them
            //SparseAnnotations annotations = annotations(removeElements(node, REMOVAL_KEYS));
            // Update host inventory
            
            LOG.info("Host mac "+mac.toString());
            LOG.info("Host vlanId "+vlanId.toString());
            LOG.info("Host locations "+locations.toString());
            LOG.info("Host ips "+ips.toString());
            
            HostId hostId = HostId.hostId(mac, vlanId);
            DefaultHostDescription desc = new DefaultHostDescription(mac, vlanId, locations, ips, false);
            hostProviderService.hostDetected(hostId, desc, false);
            
        }


    }
    

}


