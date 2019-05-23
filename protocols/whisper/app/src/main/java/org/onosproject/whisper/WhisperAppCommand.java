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
@Command(scope = "onos", name = "change-parent",
         description = "Sample Apache Karaf CLI command")
public class WhisperAppCommand extends AbstractShellCommand{

	private final Logger log = LoggerFactory.getLogger(getClass());
       
	private final WhisperController controller = get(WhisperController.class);
       
	
    @Argument(index = 0, name = "targetNode", description = "ID of target Sensor",
            required = true, multiValued = false)
    @Completion(DeviceIdCompleter.class)
    String targetNode = null;

    @Argument(index = 1, name = "parentNode", description = "ID of the desired parent of that Sensor",
            required = true, multiValued = false)
    @Completion(DeviceIdCompleter.class)
    String parentNode = null;
	
	
    @Override
    protected void doExecute() {
        print("Received command!");
        
    	ObjectMapper mapper = new ObjectMapper();
    	ObjectNode jNode = mapper.createObjectNode();
    	((ObjectNode) jNode).put("target", targetNode);
    	((ObjectNode) jNode).put("oldparent", parentNode);
    	
        print("Sending message to Whisper controller...");        
        controller.sendWhisperSouthBandMessage("change-parent",jNode.toString());
    }

}
