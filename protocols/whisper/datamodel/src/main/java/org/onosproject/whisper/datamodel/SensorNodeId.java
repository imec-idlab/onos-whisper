package org.onosproject.whisper.datamodel;

import org.onlab.packet.MacAddress;				

import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * emunicio
 */
public class SensorNodeId {
    public static final String SCHEME = "whisper";

    private final Logger log = LoggerFactory.getLogger(getClass());
    
    private int simpleId;
    private String smac;
    private MacAddress macAddress;
    private MacAddress macAddressHost;

    
  public SensorNodeId(String mac) {

	  smac=mac;
	  
	  log.info("Creating SensorNodeId with mac:"+mac);

	  StringBuilder sb = new StringBuilder(mac);
	  sb.deleteCharAt(0);
      sb.deleteCharAt(17);
	  String fmac = sb.toString();
	  
	  //this is a trick to assign a virtual mac address to the host within the sensor node
	  String mHost = "00:00:00:00:00:00";
	  String[] macAddressPartsHost = fmac.split(":");
	  Byte[] macaddrHost = new Byte[6];
	  byte[] mbytesHost = new byte[6];	

	  String[] macAddressParts = fmac.split(":");
	  Byte[] macaddr = new Byte[6];
	  byte[] mbytes = new byte[6];	
	  for(int i=0; i<6; i++){
		    Integer hex = Integer.parseInt(macAddressParts[i], 16);
		    macaddr[i] = hex.byteValue();
		    mbytes[i] = macaddr[i].byteValue();
		    
		    if (i>=4){
			    Integer hexHost = Integer.parseInt(macAddressPartsHost[i], 16);
			    macaddrHost[i] = hexHost.byteValue();
			    mbytesHost[i] = macaddrHost[i].byteValue();
		    }
	   } 

	   simpleId=((macaddr[4]*256)+(macaddr[5])); 
	  
	   macAddress = new MacAddress(mbytes);
	   macAddressHost = new MacAddress(mbytesHost);
   }
  
   public MacAddress getMacAddress() {
			return macAddress;  
   }
   
   public MacAddress getMacAddressHost() {
			return macAddressHost;
   }

   public int getSimpleId() {  
			return simpleId;  
   }
 
  public URI uri() {
      
      String schemeSpecificPart = getMacAddress().toString();

      try {
          return new URI(SCHEME, schemeSpecificPart, null);
      } catch (URISyntaxException e) {
          e.printStackTrace();
          return null;
      }

      
   }
}


