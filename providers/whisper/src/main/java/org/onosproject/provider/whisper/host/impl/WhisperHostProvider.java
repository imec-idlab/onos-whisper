package org.onosproject.provider.whisper.host.impl;

import java.nio.ByteBuffer;
import java.util.List;

import com.google.common.collect.Lists;
import org.onosproject.net.provider.AbstractProvider;
import org.onosproject.net.provider.ProviderId;

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

import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.intent.Constraint;
import org.onosproject.net.intent.HostToHostIntent;
import org.onosproject.net.intent.IntentService;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.intent.Key;

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
    
    protected HostProviderService hostProviderService;
   
    static final String SCHEME = "whisper";
    
    private InternalHostProvider hostProvider = new InternalHostProvider();

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected WhisperController controller;

    //intent related
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected IntentService intentService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected HostProviderRegistry hostProviderRegistry;
 
    private ApplicationId appId;
    
    /**
     * Creates a provider with the supplier identifier.
     */
    public WhisperHostProvider() {
        super(new ProviderId("whisper", "org.onosproject.provider.whisper.host"));      
        LOG.info("Initializing Whisper Host Provider ");
    }
      
    @Activate
    public void activate() {   	
    	
    	LOG.warn("Activating HOST Provider");
    	hostProviderService = hostProviderRegistry.register(this);
    	appId = coreService.registerApplication("org.onosproject.whisper");
    	controller.addHostListener(hostProvider);  	
        LOG.info("Started Whisper Host Provider");
    }

    @Deactivate
    public void deactivate() {

    	hostProviderRegistry.unregister(this);
    	hostProviderService = null;

        LOG.warn("Stopped Whisper Device Provider");
    }
    
    public void triggerProbe(Host host) {
        // Not implemented since there is no need to check for hosts on network
    }
    

    /**
     * Internal host provider that provides host events.
     */
    private class InternalHostProvider implements WhisperHostListener  {
      
        @Override
        public void addVirtualHost(SensorNode node) {
        	
            LOG.info("Adding virtual Host to sensor");
            MacAddress mac = node.getId().getMacAddressHost();
            VlanId vlanId = VlanId.vlanId((short) -1);

            String porthost="1";
            Set<HostLocation> locations = new HashSet<>();
                
            String deviceAndPort = node.getId().uri().toString() + "/" + porthost;
            LOG.info("Host deviceAndPort "+deviceAndPort);
            HostLocation hostLocation = new HostLocation(ConnectPoint.deviceConnectPoint(deviceAndPort), 0);
            locations.add(hostLocation);

            Set<IpAddress> ips = new HashSet<>();
            LOG.info("Host with IPv6 "+node.getStringIpv6());
            ips.add(IpAddress.valueOf(node.getStringIpv6().replace("/", "")));
          
            LOG.info("Host mac "+mac.toString());
            LOG.info("Host vlanId "+vlanId.toString());
            LOG.info("Host locations "+locations.toString());
            LOG.info("Host ips "+ips.toString());
            
            HostId hostId = HostId.hostId(mac, vlanId);
            DefaultHostDescription desc = new DefaultHostDescription(mac, vlanId, locations, ips, false);
            hostProviderService.hostDetected(hostId, desc, false);
             
            //creating intent
            LOG.info("Trying to add INTENT");
            
            //fixed as dst host
            HostId dstId = HostId.hostId("CA:FE:BA:BE:CA:FE/None");
            
            LOG.info("dest");
            
            TrafficSelector selector = DefaultTrafficSelector.emptySelector();
            
            LOG.info("traffic selector");
            TrafficTreatment treatment = DefaultTrafficTreatment.emptyTreatment();
            LOG.info("KEY");
            Key key = Key.of(hostId.toString() +"-"+ dstId.toString(), appId);

            HostToHostIntent intent = (HostToHostIntent) intentService.getIntent(key);
            
            LOG.info("Host to Host INTENT");
            HostToHostIntent hostIntent = HostToHostIntent.builder()
                    .appId(appId)
                    .key(key)
                    .one(hostId)
                    .two(dstId)
                    .selector(selector)
                    .treatment(treatment)
                    .build();
            LOG.info("Host to Host submitting intent...\n"+ hostIntent.toString());
            intentService.submit(hostIntent);   

            LOG.info("Host to Host intent submitted:\n"+ hostIntent.toString());
        }
    }
}



