package org.onosproject.whisper.datamodel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Simple model class to provide sample data
public class ControllerEntry {
        private  String id;
        private  int nodes;
        private  int links;
        private  int messages;
        private final Logger log = LoggerFactory.getLogger(getClass());

        public ControllerEntry(String id, int nodes, int links, int messages) {
            this.id = id;
            this.nodes = nodes;
            this.links = links;
            this.messages = messages;
            log.info("New Controller Entry: "+this.id);
        }

       public String id() { return this.id; }
       public int nodes() { return this.nodes; }
       public int links() { return this.links; }
       public int messages() { return this.messages; }
        
       public void setNodes(int v) { this.nodes=v; }
       public void setLinks(int v) { this.links=v; }
       public void setMessages(int v) { this.messages=v; }
}
