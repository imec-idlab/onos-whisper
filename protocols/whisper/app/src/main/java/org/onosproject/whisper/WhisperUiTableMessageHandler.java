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
package org.onosproject.foo;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableSet;
import org.onosproject.ui.RequestHandler;
import org.onosproject.ui.UiMessageHandler;
import org.onosproject.ui.table.TableModel;
import org.onosproject.ui.table.TableRequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.onosproject.whisper.controller.WhisperController;
import org.onosproject.whisper.datamodel.ControllerEntry;


import java.lang.Override;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Skeletal ONOS UI Table-View message handler.
 */
public class WhisperUiTableMessageHandler extends UiMessageHandler {

    private static final String SAMPLE_TABLE_DATA_REQ = "sampleTableDataRequest";
    private static final String SAMPLE_TABLE_DATA_RESP = "sampleTableDataResponse";
    private static final String SAMPLE_TABLES = "sampleTables";

    private static final String SAMPLE_TABLE_DETAIL_REQ = "sampleTableDetailsRequest";
    private static final String SAMPLE_TABLE_DETAIL_RESP = "sampleTableDetailsResponse";
    private static final String DETAILS = "details";

    private static final String NO_ROWS_MESSAGE = "No items found";

    private static final String ID = "id";
    private static final String NODES = "nodes";
    private static final String LINKS = "links";
    private static final String MESSAGES = "messages";
    private static final String COMMENT = "comment";
    private static final String RESULT = "result";
    
    private final WhisperController controller;

    private static final String[] COLUMN_IDS = { ID, NODES, LINKS, MESSAGES };

    private final Logger log = LoggerFactory.getLogger(getClass());

    public WhisperUiTableMessageHandler(WhisperController c){
    	controller=c;
    }

    @Override
    protected Collection<RequestHandler> createRequestHandlers() {
        return ImmutableSet.of(
                new SampleTableDataRequestHandler(),
                new SampleTableDetailRequestHandler()
        );
    }

    // handler for sample table requests
    private final class SampleTableDataRequestHandler extends TableRequestHandler {

        private SampleTableDataRequestHandler() {
            super(SAMPLE_TABLE_DATA_REQ, SAMPLE_TABLE_DATA_RESP, SAMPLE_TABLES);
        }

        // if necessary, override defaultColumnId() -- if it isn't "id"

        @Override
        protected String[] getColumnIds() {
            return COLUMN_IDS;
        }

        // if required, override createTableModel() to set column formatters / comparators

        @Override
        protected String noRowsMessage(ObjectNode payload) {
            return NO_ROWS_MESSAGE;
        }

        @Override
        protected void populateTable(TableModel tm, ObjectNode payload) {

        	Iterable<ControllerEntry> nWhispControllers = controller.getWhisperControllers();
        	for(ControllerEntry wc: nWhispControllers){
        		populateRow(tm.addRow(), wc);
        	}
        	
        }

        private void populateRow(TableModel.Row row, ControllerEntry w) {
        	log.info("Populating with: "+w.id());
            row.cell(ID, w.id())
                    .cell(NODES, w.nodes())
                    .cell(LINKS, w.links())
            		.cell(MESSAGES, w.messages());
        }
    }


    // handler for sample item details requests
    private final class SampleTableDetailRequestHandler extends RequestHandler {

        private SampleTableDetailRequestHandler() {
            super(SAMPLE_TABLE_DETAIL_REQ);
        }

        @Override
        public void process(ObjectNode payload) {
            String id = string(payload, ID, "(none)");

            ControllerEntry item = getItem(id);

            ObjectNode rootNode = objectNode();
            ObjectNode data = objectNode();
            rootNode.set(DETAILS, data);

            if (item == null) {
                rootNode.put(RESULT, "Item with id '" + id + "' not found");
                log.warn("attempted to get item detail for id '{}'", id);

            } else {
                rootNode.put(RESULT, "Found item with id '" + id + "'");

                data.put(ID, item.id());
                data.put(NODES, item.nodes());
                data.put(LINKS, item.links());
                data.put(MESSAGES, item.messages());
            }

            sendMessage(SAMPLE_TABLE_DETAIL_RESP, rootNode);
        }
    }


    // ===================================================================
    // NOTE: The code below this line is to create fake data for this
    //       sample code. Normally you would use existing services to
    //       provide real data.

    // Lookup a single item.
    private ControllerEntry getItem(String id) {
   	
    	Iterable<ControllerEntry> nWhispControllers = controller.getWhisperControllers();
    	for(ControllerEntry item: nWhispControllers){
    		if (item.id().equals(id)) {
    			return item;
    		}
    	}
    	
        return null;
    }
}
