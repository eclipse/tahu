node-red-contrib-sparkplug
=========

A node for an MQTT Edge Node client for MQTT device communication using the
Sparkplug Specification from Cirrus Link Solutions.  

https://s3.amazonaws.com/ignition-modules/Current/Sparkplug+Specification.pdf

The client will connect to an MQTT Server and act as an MQTT Edge of Network
(EoN) Node.  It will publish birth certificates (NBIRTH), node data messages
(NDATA), and process node command messages (NCMD) that have been sent from
another MQTT client.

The client also provides and interface for other nodes to publish device birth
certificates (DBIRTH), device data messages (DDATA), device death certificates
(DDEATH), and receive device command messages (DCMD) that have been sent from
another MQTT client.

Additional details on the example payloads below can be found here:

https://www.npmjs.com/package/sparkplug-client

## Installation

  npm install node-red-contrib-sparkplug

## Usage

### Configuring the Sparkplug Node

When editing the Sparkplug Node the following properties are configurable:

* ServerUrl: The URL of the MQTT server.
* Port: The port of the MQTT server.
* Username: The username for the MQTT server connection.
* Password: The password for the MQTT server connection.
* Client ID: A unique client ID for the MQTT server connection.
* Group ID: An ID representing a logical grouping of MQTT EoN Nodes and Devices
  into the infrastructure.
* Edge Node: An ID that uniquely identifies the MQTT EoN Node within the
  infrastructure.
* Version: The Sparkplug version (currently: A or B).
* Enable Cache: Whether to enable EoN node caching.
* Publish Death: Whether to publish the edge node's death certicate when the 
  client cleanly disconnects

Upon deploying the flow, the Sparkplug Node it will automatically connect to
the MQTT Server. When the flow is stopped, the Sparkplug Node will cleanly close
down the client connection by disconnecting from the MQTT Server.

### Sparkplug Node Inputs

The Sparkplug Node expects input messages to be received on topics of the
format:  *deviceId*/*type*.

Acceptable values for each token in the topic are :

 * *deviceId*: A unique device ID string that does not contain the following
   reserved characters: '/', '#', "+".
 * *type*: DDATA | DBIRTH | DDEATH

The payload of each message will depend on the message type.  The data types 
supported for metrics of payload differ based on the Sparkplug version. A full 
description of each versions format and data type support is beyond the scope of 
this readme and can be found in the Sparkplug specification linked above. The 
examples in this ready will use Sparkplug B.

#### DBIRTH message

Topic:  *deviceId*/DBIRTH  
Payload:  An object with a "timestamp" (required), array of ALL "metric" objects
         (required).  
Example:

```javascript
{
    "timestamp" : 1465577611580
    "metrics" : [
        {
            "name" : "my_int",
            "value" : 456,
            "type" : "int32"
        },
        {
            "name" : "my_float",
            "value" : 456,
            "type" : "float"
        }
    ]
}
```

#### DDATA message

Topic: *deviceId*/DDATA  
Payload: An object with a "timestamp" (required), array of one or more "metric"
         objects (required), and "position" (optional).  
Example:

```javascript
{
    "timestamp" : 1465577611580,
    "metrics" : [
        {
            "name" : "my_int",
            "value" : 456,
            "type" : "int32"
        }
    ]
}
```

#### DDEATH message

Topic: *deviceId*/DDEATH  
Payload: An object with a "timestamp" (required).  
Example:

```javascript
{
    "timestamp" : 1465577611580
}
```

For each metric included in the payloads, the following types are supported:
int, long, float, double, boolean, string, bytes.

### Sparkplug Node Outputs

The Sparkplug Node sends output messages in order to notify other nodes of a
'rebirth' request or to send a device command (DCMD) request.

#### 'rebirth' message

Topic: rebirth  
Payload: {}

The Sparkplug Node sends a 'rebirth' message in order to force all device nodes
resend DBIRTH messages. This message is send once upon the deployment of the
flow, after the Sparkplug Node has connected with the MQTT Server, and also
every time the Sparkplug Node receives a node command (NCMD) message requesting
a rebirth from itself all all devices.

#### command message

Topic: *deviceId*  
Payload: An object with an array of one or more "metric" objects (required).  
Example:

```javascript
{
    "metrics" : [
        {
            "name" : "my_int",
            "value" : 456,
            "type" : "int32"
        },
        {
            "name" : "my_float",
            "value" : 456,
            "type" : "float"
        }
    ]
}
```

A Sparkplug Node sends a command message every time it receives a device command
(DCMD) message requesting write operations to the metrics of a specific device.
The message will contain a single device ID in the topic and the payload will
specify the metrics/values to write to the device. The device node specified by
the device ID should process the command message and then send a DDATA message
containing any metric values that have changed or been successfully written to.

## Release History

* 1.0.0 Initial release
* 1.0.2 Bug Fixes
* 1.1.0 Added connection status indicator, changed category
* 1.2.0 Added 'Publish Death' config option, and mouseover config descriptions
* 1.2.1 Added "node-red" keyword
* 2.0.0 Added support for Sparkplug B and made version configurable
* 2.1.0 Updated sparkplug-client version to 3.0.0
* 2.1.1 Updated License and repo links

## License

Copyright (c) 2016 Cirrus Link Solutions

All rights reserved. This program and the accompanying materials
are made available under the terms of the Eclipse Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/epl-v10.html

Contributors: Cirrus Link Solutions
