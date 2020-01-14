Sparkplug Client
=========

A client library providing a MQTT client for MQTT device communication using
the Sparkplug Specification from Cirrus Link Solutions.  

https://s3.amazonaws.com/ignition-modules/Current/Sparkplug+Specification.pdf

The client will connect to an MQTT Server and act as an MQTT Edge of Network
(EoN) Node.  It will publish birth certificates (NBIRTH), node data messages
(NDATA), and process node command messages (NCMD) that have been sent from
another MQTT client.

The client also provides and interface for MQTT Device application code to
publish device birth certificates (DBIRTH), device data messages (DDATA),
device death certificates (DDEATH), and receive device command messages (DCMD)
that have been sent from another MQTT client.

## Installation

  npm install sparkplug-client

## Usage

### Creating and configuring a new Sparkplug client

A configuration object is required when creating a new client.  A configuration
must contain the following properties:

* serverUrl: The URL of the MQTT server.
* username: The username for the MQTT server connection.
* password: The password for the MQTT server connection.
* groupId: An ID representing a logical grouping of MQTT EoN Nodes and Devices
  into the infrastructure.
* edgeNode: An ID that uniquely identifies the MQTT EoN Node within the
  infrastructure.
* clientId: A unique ID for the MQTT client connection.
* publishDeath: A flag indicating if a Node DEATH Certificate (NDEATH) should
  be published when the client is stopped (defaults to false).
* version: The Sparkplug version (currently: A or B).  This will indicate how
  the payload of the published Sparkplug messages are formatted.
* keepalive: The MQTT client keep alive interval in seconds (defaults to 30). 

Here is a code example of creating and configuring a new client:

```javascript
var sparkplug = require('sparkplug-client'),
    config = {
        'serverUrl' : 'tcp://localhost:1883',
        'username' : 'username',
        'password' : 'password',
        'groupId' : 'Sparkplug Devices',
        'edgeNode' : 'Test Edge Node',
        'clientId' : 'JavaScriptSimpleEdgeNode',
        'version' : 'spBv1.0'
    },
    client = sparkplug.newClient(config);
```

### Stopping the client

Once a client has been created and configured it will automatically connect to
the MQTT Server.  the client provides a function for stopping the client and
cleanly disconnecting from the MQTT Server.  Once a client has been stopped, a
new client must be created and configured in order to re-establish a connection
with the server.

Here is a code example of stopping a client:

```javascript
// Stop the sparkplug client
client.stop();
```

### Publishing messages

This client provides functions for publishing three types of messages: a device
birth certificate (DBIRTH), device data message (DDATA), device death
certificate (DDEATH)

#### Message Payloads ####

The payload format for Sparkplug messages differs based on the Sparkplug version.
A full description of each versions payload format is beyond the scope of this
readme and can be found in the Sparkplug specification linked above.  The examples
in this readme will be using Sparkplug B.  

Here is a quick summary of the main changes in version B (over A):

* Added more supported data types for metric values
* Added support for generic property sets
* Removed required "position" field
* Change the name of the metrics list field from "metric" to "metrics".

#### Publish Options

Each of the publish methods below can optionally take an object as an additional 
argument.  This object contains any configured options for the publish.

##### Compression Option

A payload can be compressed before it is published by enabling payload compression
in the options object and passing it to a plush command.  For example:

```javascript
var options = {
    "compress" : true
};

// Publish device data
client.publishDeviceData(deviceId, payload, options);
```

Additionally the compression algorithm can be specified as well.  Currently 
supported algorithms are: DEFLATE and GZIP.  DEFLATE will be used if not algorithm
is specified. For example:

```javascript
var options = {
    "compress" : true,
    "algorithm" : "GZIP"
};

// Publish device data
client.publishDeviceData(deviceId, payload, options);
```

#### Edge Node Birth Certificate (NBIRTH)

A Sparkplug node birth certificate (NBIRTH) message will contain all data points,
process variables, and/or metrics for the edge node. The payload for this message
will differ slightly between the different Sparkplug versions.

* timestamp:  A UTC timestamp represented by 64 bit integer.
* metrics:  An array of metric objects. Each metric in the array must contain
  the following:
  * name:  The name of the metric.
  * value:  The value of the metric.
  * type:  The type of the metric.  The following types are supported: int, 
    int8, int16, int32, int64, uint8, uint16, uint32, uint64, float, double, 
    boolean, string, datetime, text, uuid, dataset, bytes, file, or template.

Here is a code example of publishing a NBIRTH message:

```javascript
var payload = {
        "timestamp" : 1465577611580,
        "metrics" : [
            {
                "name" : "my_int",
                "value" : 456,
                "type" : "int32"
            },
            {
                "name" : "my_float",
                "value" : 1.23,
                "type" : "float"
            }
        ]
    };

// Publish device birth
client.publishNodeBirth(payload);
```


#### Device Birth Certificate (DBIRTH)

A Sparkplug device birth certificate (DBIRTH) message will contain all data points,
process variables, and/or metrics for the device. The payload for this message
will differ slightly between the different Sparkplug versions.

* timestamp:  A UTC timestamp represented by 64 bit integer.
* metrics:  An array of metric objects. Each metric in the array must contain
  the following:
  * name:  The name of the metric.
  * value:  The value of the metric.
  * type:  The type of the metric.  The following types are supported: int, 
    int8, int16, int32, int64, uint8, uint16, uint32, uint64, float, double, 
    boolean, string, datetime, text, uuid, dataset, bytes, file, or template.

Here is a code example of publishing a DBIRTH message:

```javascript
var deviceId = "testDevice",
    payload = {
        "timestamp" : 1465577611580,
        "metrics" : [
            {
                "name" : "my_int",
                "value" : 456,
                "type" : "int32"
            },
            {
                "name" : "my_float",
                "value" : 1.23,
                "type" : "float"
            }
        ]
    };

// Publish device birth
client.publishDeviceBirth(deviceId, payload);
```


#### Node Data Message (NDATA)

An edge node data message (NDATA) will look similar to NBIRTH but is not required
to publish all metrics. However, it must publish at least one metric.

Here is a code example of publishing a DBIRTH message:

```javascript
var payload = {
        "timestamp" : 1465456711580,
        "metrics" : [
            {
                "name" : "my_int",
                "value" : 412,
                "type" : "int32"
            }
        ]
    };

// Publish device data
client.publishNodeData(payload);
```


#### Device Data Message (DDATA)

A device data message (DDATA) will look similar to DBIRTH but is not required
to publish all metrics. However, it must publish at least one metric.

Here is a code example of publishing a DBIRTH message:

```javascript
var deviceId = "testDevice",
    payload = {
        "timestamp" : 1465456711580,
        "metrics" : [
            {
                "name" : "my_int",
                "value" : 412,
                "type" : "int32"
            }
        ]
    };

// Publish device data
client.publishDeviceData(deviceId, payload);
```

#### Node Death Certificate (NDEATH)

An edge node death certificate (NDEATH) is published to indicated that the edge
node has gone offline or has lost a connection.  It registered as an MQTT LWT
by the SparkplugClient instance and published on the applications behalf.


#### Device Death Certificate (DDEATH)

A device death certificate (DDEATH) can be published to indicated that the
device has gone offline or has lost a connection.  It should contain only a
timestamp.

Here is a code example of publishing a DDEATH message:

```javascript
var deviceId = "testDevice",
    payload = {
        "timestamp" : 1465456711580
    };

// Publish device death
client.publishDeviceDeath(deviceId, payload);
```

### Receiving events

The client uses an EventEmitter to emit events to device applications.  The
client emits a "rebirth" event, "command" event, and five MQTT connection
events: "connect", "reconnect", "offline", "error", and "close".

#### Birth Event

A "birth" event is used to signal the device application that a DBIRTH 
message is requested.  This event will be be emitted immediately after the 
client initially connects or re-connects with the MQTT Server.

Here is a code example of handling a "birth" event:

```javascript
sparkplugClient.on('birth', function () {
    // Publish Node BIRTH certificate
    sparkplugClient.publishNodeBirth(getNodeBirthPayload());
    // Publish Device BIRTH certificate
    sparkplugClient.publishDeviceBirth(deviceId, getDeviceBirthPayload());
});
```

#### Command Events

A Device Command event is used to communicate a Device Command message (DCMD)
from another MQTT client to a device. A 'dcmd' event will include the device ID 
and a payload containing a list of metrics (as described above).  Any metrics 
included in the payload represent attempts to write a new value to the data 
points or process variables that they represent.  After the device application
processes the request the device application should publish a DDATA message 
containing any metrics that have changed or been updated.

Here is a code example of handling a "dcmd" event:

```javascript
client.on('dcmd', function (deviceId, payload) {
    console.log("received 'dcmd' event");
    console.log("device: " + device);
    console.log("payload: " + payload);

    //
    // Process metrics and create new payload containing changed metrics
    //

    client.publishDeviceData(deviceId, newPayload);
});
```

A Node Command event is used to communicate an Edge Node Command message (DCMD) 
or Edge Node Command message (NCMD) from another MQTT client to a device.  An 
'ncmd' event will include a payload containing a list of metrics (as described 
above).  Any metrics included in the payload may represent attempts to write a 
new value to the data points or process variables that they represent or they
may represent control messages sent to the edge node such as a "rebirth" 
request.

Here is a code example of handling a "ncmd" event:

```javascript
client.on('ncmd', function (payload) {
    console.log("received 'ncmd' event");
    console.log("payload: " + payload);

    //
    // Process metrics and create new payload containing changed metrics
    //

    client.publishNodeData(newPayload);
});
```

#### Connect Event

A "connect" event is emitted when the client has connected to the server.

Here is a code example of handling a "connect" event:

```javascript
client.on('connect', function () {
    console.log("received 'connect' event");
});
```

#### Reconnect Event

A "reconnect" event is emitted when the client is attempting to reconnect to
the server.

Here is a code example of handling a "reconnect" event:

```javascript
client.on('reconnect', function () {
    console.log("received 'reconnect' event");
});
```

#### Offline Event

An "offline" event is emitted when the client loses connection with the server.

Here is a code example of handling an "offline" event:

```javascript
client.on('offline', function () {
    console.log("received 'offline' event");
});
```

#### Error Event

An "error" event is emitted when the client has experienced an error while
trying to connect to the server.

Here is a code example of handling a "error" event:

```javascript
client.on('error', function (error) {
    console.log("received 'error' event: " + error);
});
```

#### Close Event

A "close" event is emitted when the client's connection to the server has been
closed.

Here is a code example of handling a "close" event:

```javascript
client.on('close', function () {
    console.log("received 'close' event");
});
```

## Release History

* 1.0.0 Initial release
* 1.0.2 Bug Fixes
* 1.1.0 Added more emitted events (connect, reconnect, error, close)
* 1.2.0 Added 'publishDeath' config option, updated MQTT.js version
* 2.0.0 Added support for Sparkplug B and made the version configurable.
* 3.0.0 Added events for Node Birth/Command events. Renamed 'command' event
        to distiguish between 'dcmd' (device commands) and 'ncmd' (node 
        commands). Renamed 'rebirth' event to 'birth'. Updated dependency
        versions and removed bytebuffer as a dependency.
* 3.1.0 Added support for payload compression/decompression with DEFLATE
        and Gzip algorithms, added logging with Winston to replace console
        logging, and other minor bug fixes. Moved sparkplug payload libraries
        to their own project and updated dependecies.
* 3.2.0 Added new 'offline' emitted event, added configurable keep alive,
        updated log messages and set default level to 'info', and disabled 
        ping rescheduling within the client.
* 3.2.1 Updated License and repo links, cleaned up logging.
* 3.2.2 Bug Fixes

## License

Copyright (c) 2016-2018 Cirrus Link Solutions

All rights reserved. This program and the accompanying materials
are made available under the terms of the Eclipse Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/epl-v10.html

Contributors: Cirrus Link Solutions
