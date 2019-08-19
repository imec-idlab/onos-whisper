/*
 * Copyright 2019-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onosproject.whisper;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableSet;
import org.onosproject.ui.RequestHandler;
import org.onosproject.ui.UiMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.onosproject.whisper.controller.WhisperController;
import org.onosproject.whisper.datamodel.Cell;
import org.onosproject.whisper.datamodel.SensorNode;

import java.util.Collection;

/**
 * Skeletal ONOS UI Custom-View message handler.
 */
public class WhisperUiMessageHandler extends UiMessageHandler {

    private static final String SAMPLE_CUSTOM_DATA_REQ = "sampleCustomDataRequest";
    private static final String SAMPLE_CUSTOM_DATA_RESP = "sampleCustomDataResponse";
        
    private final WhisperController controller;

    private final Logger log = LoggerFactory.getLogger(getClass());

    private int maxchan = 16;
    private int maxts = 101;
    
    
    
    public WhisperUiMessageHandler(WhisperController c){
    	this.controller=c;
    }

    @Override
    protected Collection<RequestHandler> createRequestHandlers() {
        return ImmutableSet.of(
                new SampleCustomDataRequestHandler()
        );
    }

    
    
    // handler for sample data requests
    private final class SampleCustomDataRequestHandler extends RequestHandler {

        private SampleCustomDataRequestHandler() {
            super(SAMPLE_CUSTOM_DATA_REQ);
        }

        @Override
        public void process(ObjectNode payload) {
        	
        	String cellid="cell";
        	int ts=0;
        	int ch=0;
        	String nodeSrc;
        	String nodeDst;
        	      
        	ObjectNode result = objectNode();
        	
        	for (ch=0;ch<maxchan;ch++){ 	
        		for (ts=0;ts<maxts;ts++){
        				result.put(cellid+"_"+ts+"_"+ch, "..");
        		}
        	}
        	
        	ts=0;
        	ch=0;
        	//overwritting used cells
        	Iterable<SensorNode> wNodes = controller.getNodes();
	        
        	for(SensorNode n: wNodes){

        		log.info("checking node "+n.getId().getLastMacByte());
            	
            	Iterable<Cell> wCells = n.getCells();
            	
            	for(Cell c: wCells){
            		log.info("checking cell "+c.getStringId());
            		log.info(" src "+c.getNodeSrc().getLastMacByte());
            		log.info(" dst "+c.getNodeDst().getLastMacByte());
            		log.info(" ts "+c.getTS());
            		log.info(" ch "+c.getCH());
            		ts=c.getTS();
            		ch=c.getCH();
            		nodeSrc=c.getNodeSrc().getLastMacByte();
            		nodeDst=c.getNodeDst().getLastMacByte();
            		if (c.getNodeSrc().getLastMacByte().equals("FF") && c.getNodeDst().getLastMacByte().equals("FF")){
            			log.info("This is the minimal cell "+c.getStringId());
            			result.put(cellid+"_"+ts+"_"+ch, "MIN");
            		}else{
//                		if (c.getNodeDst().getLastMacByte()==null){
//                			nodeDst="FF";
//                		}
//                		if (c.getNodeSrc().getLastMacByte()==null){
//                			nodeSrc="FF";
//                		}
                		result.put(cellid+"_"+ts+"_"+ch, nodeDst+"\n"+nodeSrc+"\n");	
            		}
            	
            	}

            }
            //log.info("data for {} ... {}", SAMPLE_CUSTOM_DATA_RESP,result);
            sendMessage(SAMPLE_CUSTOM_DATA_RESP, result);
        }
    }
}