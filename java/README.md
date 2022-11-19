# Tahu Java Libraries and Implementations

These are the Java based Eclipse Sparkplug libraries, implementations, and examples.

# Building

From the git root directory run the following commands

```
cd java
mvn clean install
```

# Eclipse Tahu Java Libraries

The Tahu Java implementation provides the following libraries. These can be used for developing custom Java based Sparkplug applications.

* org.eclipse.tahu:tahu-core
  * This is the core Sparklplug library to use for modeling, encoding, and decoding of Sparkplug topics and payloads
* org.eclipse.tahu:tahu-edge
  * This is the core Sparkplug library to use for implementing Sparkplug Edge Node Applications
* org.eclipse.tahu:tahu-host
  * This is the core Sparkplug library to use for implementing Sparkplug Host Applications

# Eclipse Tahu Java Applications

The Tahu Java implementation provides the following Sparkplug compatible implementations. These are complete implementations that fully pass the Eclipse Sparkplug TCK here: https://github.com/eclipse-sparkplug/sparkplug/blob/master/tck/README.md.

* org.eclipse.tahu:tahu-edge-compat
  * This is a fully compliant Spark plug Edge Node Application that passes the Sparkplug TCK. It uses the RandomDataSimulator implementation of the DataSimulator interface to initially publish BIRTH messages and then periodically send DATA messages to an MQTT Server.
  * To run:
    ```
    java -jar compat_impl/edge/target/tahu-edge-compat-1.0.1-SNAPSHOT.jar 
    ```
  * The following config options exist for the Tahu Edge Node in compat_impl/edge/src/main/java/org/eclipse/tahu/edge/SparkplugEdgeNode.java
    ```
    private static final String COMMAND_LISTENER_DIRECTORY = "/tmp/commands";
    private static final long COMMAND_LISTENER_POLL_RATE = 50L;

    private static final String GROUP_ID = "G1";
    private static final String EDGE_NODE_ID = "E1";
    private static final EdgeNodeDescriptor EDGE_NODE_DESCRIPTOR = new EdgeNodeDescriptor(GROUP_ID, EDGE_NODE_ID);
    private static final List<String> DEVICE_IDS = Arrays.asList("D1");
    private static final String PRIMARY_HOST_ID = "IamHost";
    private static final boolean USE_ALIASES = true;
    private static final Long REBIRTH_DEBOUNCE_DELAY = 5000L;

    private static final MqttServerName MQTT_SERVER_NAME_1 = new MqttServerName("Mqtt Server One");
    private static final String MQTT_CLIENT_ID_1 = "Sparkplug-Tahu-Compatible-Impl-One";
    private static final MqttServerUrl MQTT_SERVER_URL_1 = new MqttServerUrl("tcp://localhost:1883");
    private static final String USERNAME_1 = "admin";
    private static final String PASSWORD_1 = "changeme";
    private static final MqttServerName MQTT_SERVER_NAME_2 = new MqttServerName("Mqtt Server Two");
    private static final String MQTT_CLIENT_ID_2 = "Sparkplug-Tahu-Compatible-Impl-Two";
    private static final MqttServerUrl MQTT_SERVER_URL_2 = new MqttServerUrl("tcp://localhost:1884");
    private static final String USERNAME_2 = "admin";
    private static final String PASSWORD_2 = "changeme";
    private static final int KEEP_ALIVE_TIMEOUT = 30;
    private static final Topic NDEATH_TOPIC = new Topic(SparkplugMeta.SPARKPLUG_B_TOPIC_PREFIX, GROUP_ID, EDGE_NODE_ID, MessageType.NDEATH);
    ```
* org.eclipse.tahu:tahu-host-compat
  * This is a fully compliant Sparkplug Host Application that passes the Sparkplug TCK. It receives BIRTH and DATA messages and logs them to the console. It also handles message reordering and will send 'Node Control/Rebirth' requests in the event of invalid or out of order messages.
  * To run:
    ```
    java -jar compat_impl/host/target/tahu-host-compat-1.0.1-SNAPSHOT.jar 
    ```
  * The following config options exist for the Tahu Edge Node in compat_impl/host/src/main/java/org/eclipse/tahu/host/SparkplugHostApplication.java
    ```
    private static final String COMMAND_LISTENER_DIRECTORY = "/tmp/commands";
    private static final long COMMAND_LISTENER_POLL_RATE = 50L;

    private static final String HOST_ID = "IamHost";
    private static final String MQTT_SERVER_NAME_1 = "Mqtt Server One";
    private static final String MQTT_CLIENT_ID_1 = "Tahu_Host_Application";
    private static final String MQTT_SERVER_URL_1 = "tcp://localhost:1883";
    private static final String USERNAME_1 = "admin";
    private static final String PASSWORD_1 = "changeme";
    private static final String MQTT_SERVER_NAME_2 = "Mqtt Server Two";
    private static final String MQTT_CLIENT_ID_2 = "Tahu_Host_Application";
    private static final String MQTT_SERVER_URL_2 = "tcp://localhost:1884";
    private static final String USERNAME_2 = null;
    private static final String PASSWORD_2 = null;
    private static final int KEEP_ALIVE_TIMEOUT = 30;
    ```
  * The Sparkplug Tahu Host compatible implementation is capable of sending CMD messages to Edge Nodes. This is done using the filesystem and the configuration. By default, the Tahu Host Application looks for files in the '/tmp/commands' directory for files that fit a format to convert into a CMD message. This file location can be changed using the 'COMMAND_LISTENER_DIRECTORY' static variable. If this directory location is left unchanged, the following Linux scripts can used to send CMD messages.
    * Send an Edge Node 'Node Control/Rebirth' request
      ```console
      #!/bin/sh

      TIMESTAMP=`date +%s`
      TIMESTAMP=${TIMESTAMP}000
      echo ${TIMESTAMP}

      PAYLOAD="{\"topic\":{\"namespace\":\"spBv1.0\",\"edgeNodeDescriptor\":\"G1/E1\",\"groupId\":\"G1\",\"edgeNodeId\":\"E1\",\"type\":\"NCMD\"},\"payload\":{\"timestamp\":"${TIMESTAMP}",\"metrics\":[{\"name\":\"Node Control/Rebirth\",\"timestamp\": "${TIMESTAMP}",\"dataType\":\"Boolean\",\"value\":true}]}}"
      echo ${PAYLOAD}

      echo ${PAYLOAD} > /tmp/commands/rebirth.json
      ```

    * Send an Edge Node NCMD message
      ```console
      #!/bin/sh

      TIMESTAMP=`date +%s`
      TIMESTAMP=${TIMESTAMP}000
      echo ${TIMESTAMP}

      PAYLOAD="{\"topic\":{\"namespace\":\"spBv1.0\",\"edgeNodeDescriptor\":\"G1/E1\",\"groupId\":\"G1\",\"edgeNodeId\":\"E1\",\"type\":\"NCMD\"},\"payload\":{\"timestamp\":"${TIMESTAMP}",\"metrics\":[{\"name\":\"TCK_metric/Boolean\",\"timestamp\": "${TIMESTAMP}",\"dataType\":\"Boolean\",\"value\":true}]}}"
      echo ${PAYLOAD}

      echo ${PAYLOAD} > /tmp/commands/edge_metric.json
      ```

    * Send an Device Rebirth request message
      ```console
      #!/bin/sh

      TIMESTAMP=`date +%s`
      TIMESTAMP=${TIMESTAMP}000
      echo ${TIMESTAMP}

      PAYLOAD="{\"topic\":{\"namespace\":\"spBv1.0\",\"edgeNodeDescriptor\":\"G1/E1\",\"groupId\":\"G1\",\"edgeNodeId\":\"E1\",\"deviceId\":\"D1\",\"type\":\"DCMD\"},\"payload\":{\"timestamp\":"${TIMESTAMP}",\"metrics\":[{\"name\":\"Device Control/Rebirth\",\"timestamp\": "${TIMESTAMP}",\"dataType\":\"Boolean\",\"value\":true}]}}"
      echo ${PAYLOAD}

      echo ${PAYLOAD} > /tmp/commands/rebirth.json
      ```

    * Send an Device Rebirth request message
      ```console
      #!/bin/sh

      TIMESTAMP=`date +%s`
      TIMESTAMP=${TIMESTAMP}000
      echo ${TIMESTAMP}

      PAYLOAD="{\"topic\":{\"namespace\":\"spBv1.0\",\"edgeNodeDescriptor\":\"G1/E1\",\"groupId\":\"G1\",\"edgeNodeId\":\"E1\",\"deviceId\":\"D1\",\"type\":\"DCMD\"},\"payload\":{\"timestamp\":"${TIMESTAMP}",\"metrics\":[{\"name\":\"Inputs/0\",\"timestamp\": "${TIMESTAMP}",\"dataType\":\"Boolean\",\"value\":true}]}}"
      echo ${PAYLOAD}

      echo ${PAYLOAD} > /tmp/commands/edge_metric.json
      ```
