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
package org.onosproject.whisper.rest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Map;

import org.onosproject.rest.AbstractWebResource;
import org.onosproject.whisper.controller.WhisperController;
import org.onosproject.whisper.datamodel.SensorNodeId;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.databind.JsonNode;	
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.core.JsonParser;

import static org.onlab.util.Tools.nullIsNotFound;
import static org.onlab.util.Tools.readTreeFromStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

/**
 * Sample web resource for Whisper API.
 * emunicio
 */
@Path("whisper")
public class WhisperWebResource extends AbstractWebResource {
	
    private final Logger log = LoggerFactory.getLogger(getClass());
	
    private final WhisperController controller = get(WhisperController.class);
    
  void WhisperWebResource(){
  	
  	log.info("WhisperWebResource started!");
  }
    
    /**
     * Get hello world greeting.
     *
     * @return 200 OK
     */
    @GET
    @Path("greet")
    public Response getGreeting() {
    	
    	log.info("Hello world GET received");
        ObjectNode node = mapper().createObjectNode().put("hello", "world");
       
        return ok(node).build();
    }
    
    @POST
    @Path("setNode")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setMapping(InputStream stream) {
        ObjectNode root = mapper().createObjectNode();  		
    	mapper().configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
    	mapper().configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);

        try {

            ObjectNode jsonTree = readTreeFromStream(mapper(), stream);
            log.info("SetNode POST received ok: "+jsonTree.toString()+"");
            
            JsonNode macID = jsonTree.get("mac");
            log.info("Creating SensorNode ID "+macID.toString());
            
            SensorNodeId id = new SensorNodeId(macID.toString());    
            log.info("Received info from node "+macID.toString());
            
            controller.addConnectedSensorNode(jsonTree);

            controller.processMessage(jsonTree);
            
        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
        return ok(root).build();
    }

}
