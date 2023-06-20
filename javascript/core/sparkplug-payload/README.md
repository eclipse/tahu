Sparkplug Payload
=========

A library that provides tools for encoding and decoding payload objects using
the Sparkplug Google Protocol Buffer Schema described in the Sparkplug 
Specification from Cirrus Link Solutions.

https://s3.amazonaws.com/cirrus-link-com/Sparkplug+Topic+Namespace+and+State+ManagementV2.1+Apendix++Payload+B+format.pdf

## Installation

  npm install sparkplug-b-payload

## Usage

This library supports the Sparkplug Google Protocol Buffer Schemas for the
following Sparkplug namespaces:

* spBv1.0
* spAv1.0 (deprecated)


### Encoding a payload

Here is a code example of encoding a payload:

```javascript
var sparkplug = require('sparkplug-payload').get("spBv1.0"),
    payload = {
        "timestamp" : new Date().getTime(),
        "metrics" : [
            {
                "name" : "intMetric",
                "value" : 1,
                "type" : "Int32"
            }
        ]
    },
    encoded = sparkplug.encodePayload(payload);
```

### Decoding a payload

Here is a code example of decoding an encoded payload:

```javascript
var decoded = sparkplug.decodePayload(encoded);
```

## Release History

* 1.0.0 Initial release
* 1.0.1 Bug fixes
* 1.0.2 Bug fixes, added typescript

## License

Copyright (c) 2017-2023 Cirrus Link Solutions and others

All rights reserved. This program and the accompanying materials
are made available under the terms of the Eclipse Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/epl-v10.html

Contributors: Cirrus Link Solutions and others
