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

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.whisper.controller.WhisperController;
import org.onosproject.mastership.MastershipAdminService;
import org.onlab.osgi.ServiceDirectory;
import org.onlab.osgi.DefaultServiceDirectory;

import org.onosproject.cli.net.DeviceIdCompleter;
import org.onosproject.cli.net.SixtopPCellTypeCompleter;
import org.onosproject.cli.net.SixtopPOperationCompleter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Sample Apache Karaf CLI command
 * emunicio
 */
@Service
@Command(scope = "onos", name = "schedule-cell",
         description = "Sample Apache Karaf CLI command")
public class WhisperCommandScheduleCells extends AbstractShellCommand{

	private final Logger log = LoggerFactory.getLogger(getClass());
       
	private final WhisperController controller = get(WhisperController.class);
       
	
    @Argument(index = 0, name = "operation", description = "6P Command: ADD,DEL,CLEAR,etc.",
            required = true, multiValued = false)
    @Completion(SixtopPOperationCompleter.class)
    String operation = null;

    @Argument(index = 1, name = "srcNode", description = "ID of the src node",
            required = true, multiValued = false)
    @Completion(DeviceIdCompleter.class)
    String srcNode = null;
    
    @Argument(index = 2, name = "dstNode", description = "ID of the dst node. FF if multicast",
            required = true, multiValued = false)
    @Completion(DeviceIdCompleter.class)
    String dstNode = null;
	
    @Argument(index = 3, name = "type", description = "Cell type: TX,RX,TXRX",
            required = true, multiValued = false)
    @Completion(SixtopPCellTypeCompleter.class)
    String type = null;
    
    @Argument(index = 4, name = "ts", description = "Timeslot: from 0 to 100",
            required = true, multiValued = false)
    //@Completion(DeviceIdCompleter.class)
    String ts = null;
    
    @Argument(index = 5, name = "ch", description = "Channel: from 0 to 15",
            required = true, multiValued = false)
    //@Completion(DeviceIdCompleter.class)
    String ch = null;
    
    @Override
    protected void doExecute() {
        print("Received command!");
        
    	ObjectMapper mapper = new ObjectMapper();
    	ObjectNode jNode = mapper.createObjectNode();
    	
    	if (srcNode != dstNode){
    	
    		((ObjectNode) jNode).put("operation", operation);
	    	((ObjectNode) jNode).put("srcNode", srcNode);
	    	((ObjectNode) jNode).put("dstNode", dstNode);
	    	((ObjectNode) jNode).put("type", type);
	    	((ObjectNode) jNode).put("ts", ts);
	    	((ObjectNode) jNode).put("ch", ch);
	    	
	        print("Sending message to Whisper controller...");        
	        controller.sendWhisperSouthBandMessage("schedule-cell",jNode.toString());

    	}else{
    		print("Src node and Dst parent can't be the same");   
    	}
    }

}
