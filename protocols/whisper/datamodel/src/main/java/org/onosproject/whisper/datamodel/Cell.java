package org.onosproject.whisper.datamodel;

import java.util.List;
import com.fasterxml.jackson.databind.JsonNode;	
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.concurrent.ConcurrentHashMap;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.onosproject.net.DeviceId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * emunicio
 * 
 * Cell (timeslot and channel)
 * 
 */
public class Cell {
	
    private final Logger log = LoggerFactory.getLogger(getClass());
	
    private SensorNodeId nodeSrc=null;
    private SensorNodeId nodeDst=null;
    private String type;
    private String idm;
    private int ts;
    private int ch;
    double pdr;
           
    public Cell(SensorNodeId n1,SensorNodeId n2, int t, int c, String typeCell) {
    	
    	nodeSrc=n1;
    	nodeDst=n2;   	
	type=typeCell;
	ts=t;
	ch=c;
    	pdr=1.0;
	idm=nodeSrc.uri().toString()+"-"+nodeDst.uri().toString()+"_"+Integer.toString(ts)+"-"+Integer.toString(ch)+"_"+type;
	log.info("Creating Cell... {} ",idm);
	log.info("From node {} to node {}",n1.getLastMacByte(),n2.getLastMacByte() );
    }

    public String getStringId(){

    	return idm;
    }
    
    public SensorNodeId getNodeSrc(){
    	return this.nodeSrc;
    }
    
    public int getTS(){
    	return this.ts;
    }
    
    public int getCH(){
    	return this.ch;
    }

    public SensorNodeId getNodeDst(){
    	return this.nodeDst;
    }

    public String getCellType(){
    	return this.type;
    }

    public double getPDR(){
    	return this.pdr;
    }

}
