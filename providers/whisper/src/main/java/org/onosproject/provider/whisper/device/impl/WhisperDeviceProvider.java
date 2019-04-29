package org.onosproject.provider.whisper.device.impl;

import java.nio.ByteBuffer;						
import java.util.List;

import com.google.common.collect.Lists;

import org.onlab.packet.ChassisId;
import org.onlab.osgi.ServiceDirectory;
import org.onlab.osgi.DefaultServiceDirectory;

import static org.onosproject.net.MastershipRole.MASTER;

import org.onosproject.net.device.DeviceProviderService;
import org.onosproject.net.device.PortDescription;
import org.onosproject.net.device.DefaultPortDescription;
import org.onosproject.net.PortNumber;
import org.onosproject.net.provider.AbstractProvider;
import org.onosproject.net.provider.ProviderId;
import org.onosproject.net.device.DeviceProvider;
import org.onosproject.net.MastershipRole;
import org.onosproject.net.Port;
import org.onosproject.net.link.LinkProvider;	
import org.onosproject.net.device.DefaultDeviceDescription;
import org.onosproject.net.device.DeviceDescription;
import org.onosproject.mastership.MastershipAdminService;
import org.onosproject.net.device.DeviceProviderRegistry;
import org.onosproject.net.Device;	
import org.onosproject.net.DeviceId;
import org.onosproject.cluster.NodeId;
import org.onosproject.cluster.ClusterService;

import org.onosproject.whisper.controller.WhisperController;
import org.onosproject.whisper.controller.WhisperSensorNodeListener;
import org.onosproject.whisper.datamodel.SensorNodeId;
import static org.onosproject.net.DeviceId.deviceId;
import com.fasterxml.jackson.databind.JsonNode;	
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.onosproject.whisper.WhisperApp;

import static org.onlab.util.Tools.toHex;

import org.osgi.service.component.annotations.Activate;	
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * emunicio
 */

@Component(immediate = true)
public class WhisperDeviceProvider  extends AbstractProvider implements DeviceProvider {

    private static final Logger LOG = LoggerFactory.getLogger(WhisperDeviceProvider.class);

    protected int hostCount = 0;

    protected int infrastructurePorts = 10;
           
    protected DeviceProviderService deviceProviderService;
   
    static final String SCHEME = "whisper";
    
    private InternalWhisperDeviceProvider wListener = new InternalWhisperDeviceProvider();
  
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected WhisperController controller;
    
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected DeviceProviderRegistry deviceProviderRegistry;
    
    /**
     * Creates a provider with the supplier identifier.
     */
    public WhisperDeviceProvider() {
        super(new ProviderId("whisper", "org.onosproject.provider.whisper"));
        
        LOG.warn("Initializing Whisper Device Provider ");
    }
    
  
    
    @Activate
    public void activate() {
    	deviceProviderService = deviceProviderRegistry.register(this);
    	
    	LOG.warn("Trying to add Device provider as listener to the controller");
    	controller.addSensorNodeListener(wListener);
    	    	
        LOG.info("Started Whisper Device Provider");
    }

    @Deactivate
    public void deactivate() {

    	deviceProviderRegistry.unregister(this);

    	deviceProviderService = null;
    	controller.removeSensorNodeListener(wListener);

        LOG.warn("Stopped Whisper Device Provider");
    }

     
    /**
     * Internal Packet Provider implementation.
     *
     */
    private class InternalWhisperDeviceProvider implements WhisperSensorNodeListener {

        @Override
        public void handleNewSensorNode(SensorNodeId nodeId, boolean isRoot) {
        	LOG.warn("Internal Whisper handleNewSensorNode called from sensor node listener");
	
            if (deviceProviderService == null) {
            	LOG.error("Error! deviceProviderService is null");
                return;
            }
            LOG.warn("Creating device");
            
            DeviceId did = deviceId(nodeId.uri());
            int chassisId = 5;

            String hw = "0.0.1";
            String sw =  "0.0.1"; 
            int portCount = hostCount + infrastructurePorts;
            DeviceDescription desc;
            if (isRoot){
				desc =
				new DefaultDeviceDescription(nodeId.uri(), Device.Type.OTHER, " IDLab", hw, sw, "1234",
				                            new ChassisId(chassisId));
            }else{
				desc =
				new DefaultDeviceDescription(nodeId.uri(), Device.Type.OTHER, " IDLab", hw, sw, "1234",
				                            new ChassisId(chassisId));
            }

			deviceProviderService.deviceConnected(did, desc);
			deviceProviderService.updatePorts(did, buildPorts(portCount));
			LOG.warn("Device created");

        }
        
        
        
        

        @Override
        public void handleDeprecatedSensorNode(SensorNodeId nodeId){
        	LOG.warn("Internal Whisper handleDeprecatedSensorNode called from the listener");
        }

    }    
    
    /**
     * Generates a list of a configured number of ports.
     *
     * @param portCount number of ports
     * @return list of ports
     */
    protected List<PortDescription> buildPorts(int portCount) {
        List<PortDescription> ports = Lists.newArrayList();
        for (int i = 1; i <= portCount; i++) {
            ports.add(DefaultPortDescription.builder()
                    .withPortNumber(PortNumber.portNumber(i))
                    .isEnabled(true)
                    .type(Port.Type.COPPER)
                    .portSpeed(0)
                    .build());
        }
        return ports;
    } 
    
    /**
     * Triggers an asynchronous probe of the specified device, intended to
     * determine whether the device is present or not. An indirect result of this
     * should be invocation of
     * {@link org.onosproject.net.device.DeviceProviderService#deviceConnected} )} or
     * {@link org.onosproject.net.device.DeviceProviderService#deviceDisconnected}
     * at some later point in time.
     *
     * @param deviceId ID of device to be probed
     */
       
    @Override
    public void triggerProbe(DeviceId deviceId){
    	LOG.info("Device "+deviceId.toString()+" triggered");
    }

    /**
     * Notifies the provider of a mastership role change for the specified
     * device as decided by the core.
     *
     * @param deviceId device identifier
     * @param newRole  newly determined mastership role
     */
    @Override
    public void roleChanged(DeviceId deviceId, MastershipRole newRole){
    	LOG.info("Device "+deviceId.toString()+" change role to "+newRole.toString());
    }

    /**
     * Checks the reachability (connectivity) of a device from this provider.
     * Reachability, unlike availability, denotes whether THIS particular node
     * can send messages and receive replies from the specified device.
     * <p>
     * Implementations are encouraged to check for reachability by using only
     * internal provider state, i.e., without blocking execution.
     *
     * @param deviceId device identifier
     * @return true if reachable, false otherwise
     */
    
    @Override
    public boolean isReachable(DeviceId deviceId){
    	LOG.info("Device "+deviceId.toString()+" is reachable");
    	return true;
    }

    /**
     * Administratively enables or disables a port.
     *
     * @param deviceId   device identifier
     * @param portNumber port number
     * @param enable     true if port is to be enabled, false to disable
     */
    @Override
    public void changePortState(DeviceId deviceId, PortNumber portNumber,
                         boolean enable){
    	LOG.info("changing port state}");
    }


    /**
     * Administratively triggers 'disconnection' from the device. This is meant
     * purely in logical sense and is intended to apply equally to implementations
     * relying on connectionless control protocols.
     *
     * An indirect result of this should be invocation of
     * {@link org.onosproject.net.device.DeviceProviderService#deviceDisconnected}
     * if the device was presently 'connected' and
     * {@link org.onosproject.net.device.DeviceProviderService#deviceConnected}
     * at some later point in time if the device is available and continues to
     * be permitted to reconnect or if the provider continues to discover it.
     *
     * @param deviceId device identifier
     */
    @Override
    public void triggerDisconnect(DeviceId deviceId) {
        throw new UnsupportedOperationException(id() + " does not implement this feature");
    }
}


