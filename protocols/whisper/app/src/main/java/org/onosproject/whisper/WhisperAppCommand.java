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
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.whisper.controller.WhisperController;
import org.onosproject.mastership.MastershipAdminService;
import org.onlab.osgi.ServiceDirectory;
import org.onlab.osgi.DefaultServiceDirectory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



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
       
    @Override
    protected void doExecute() {
        print("Received command!");
        
        print("Sending message to Whisper controller...");        
        controller.sendWhisperMessage("Hello Whisper");
    }

}
