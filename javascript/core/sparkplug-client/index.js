"use strict";
var __extends = (this && this.__extends) || (function () {
    var extendStatics = function (d, b) {
        extendStatics = Object.setPrototypeOf ||
            ({ __proto__: [] } instanceof Array && function (d, b) { d.__proto__ = b; }) ||
            function (d, b) { for (var p in b) if (Object.prototype.hasOwnProperty.call(b, p)) d[p] = b[p]; };
        return extendStatics(d, b);
    };
    return function (d, b) {
        if (typeof b !== "function" && b !== null)
            throw new TypeError("Class extends value " + String(b) + " is not a constructor or null");
        extendStatics(d, b);
        function __() { this.constructor = d; }
        d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
    };
})();
exports.__esModule = true;
exports.newClient = void 0;
/**
 * Copyright (c) 2016-2017 Cirrus Link Solutions
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Cirrus Link Solutions
 */
var mqtt = require("mqtt");
var sparkplug = require('sparkplug-payload'), sparkplugbpayload = sparkplug.get("spBv1.0"), events = require('events'), pako = require('pako'), winston = require('winston');
var compressed = "SPBV1.0_COMPRESSED";
// Config for winston logging
var logger = winston.createLogger({
    level: 'warn',
    format: winston.format.json(),
    transports: [
        new winston.transports.File({ filename: 'logfile.log' })
    ]
});
if (process.env.NODE_ENV !== 'production') {
    logger.add(new winston.transports.Console({
        format: winston.format.simple()
    }));
}
function getRequiredProperty(config, propName) {
    if (config[propName] !== undefined) {
        return config[propName];
    }
    throw new Error("Missing required configuration property '" + propName + "'");
}
function getProperty(config, propName, defaultValue) {
    if (config[propName] !== undefined) {
        return config[propName];
    }
    else {
        return defaultValue;
    }
}
/*
 * Sparkplug Client
 */
var SparkplugClient = /** @class */ (function (_super) {
    __extends(SparkplugClient, _super);
    function SparkplugClient(config) {
        var _this = _super.call(this) || this;
        // Constants
        _this.type_int32 = 7;
        _this.type_boolean = 11;
        _this.type_string = 12;
        _this.versionB = "spBv1.0";
        _this.groupId = getRequiredProperty(config, "groupId");
        _this.edgeNode = getRequiredProperty(config, "edgeNode");
        _this.publishDeath = getProperty(config, "publishDeath", false);
        _this.version = getProperty(config, "version", _this.versionB);
        _this.bdSeq = 0;
        _this.seq = 0;
        _this.devices = [];
        _this.client = null;
        _this.connecting = false;
        _this.connected = false;
        // MQTT Connection options
        _this.serverUrl = getRequiredProperty(config, "serverUrl");
        var username = getRequiredProperty(config, "username");
        var password = getRequiredProperty(config, "password");
        var clientId = getRequiredProperty(config, "clientId");
        var keepalive = getProperty(config, "keepalive", 5);
        // Client connection options
        _this.mqttOptions = getProperty(config, "mqttOptions", {
            clientId: clientId,
            clean: true,
            keepalive: keepalive,
            reschedulePings: false,
            connectTimeout: 30,
            username: username,
            password: password,
            will: {
                topic: _this.version + "/" + _this.groupId + "/NDEATH/" + _this.edgeNode,
                payload: _this.encodePayload(_this.getDeathPayload()),
                qos: 0,
                retain: false
            }
        });
        _this.init();
        return _this;
    }
    // Increments a sequence number
    SparkplugClient.prototype.incrementSeqNum = function () {
        if (this.seq == 256) {
            this.seq = 0;
        }
        return this.seq++;
    };
    SparkplugClient.prototype.encodePayload = function (payload) {
        return sparkplugbpayload.encodePayload(payload);
    };
    ;
    SparkplugClient.prototype.decodePayload = function (payload) {
        return sparkplugbpayload.decodePayload(payload);
    };
    SparkplugClient.prototype.addSeqNumber = function (payload) {
        payload.seq = this.incrementSeqNum();
    };
    // Get DEATH payload
    SparkplugClient.prototype.getDeathPayload = function () {
        return {
            "timestamp": new Date().getTime(),
            "metrics": [{
                    "name": "bdSeq",
                    "value": this.bdSeq,
                    "type": "uint64"
                }]
        };
    };
    // Publishes DEATH certificates for the edge node
    SparkplugClient.prototype.publishNDeath = function (client) {
        var payload, topic;
        // Publish DEATH certificate for edge node
        logger.info("Publishing Edge Node Death");
        payload = this.getDeathPayload();
        topic = this.version + "/" + this.groupId + "/NDEATH/" + this.edgeNode;
        client.publish(topic, this.encodePayload(payload));
        this.messageAlert("published", topic, payload);
    };
    // Logs a message alert to the console
    SparkplugClient.prototype.messageAlert = function (alert, topic, payload) {
        logger.debug("Message " + alert);
        logger.debug(" topic: " + topic);
        logger.debug(" payload: " + JSON.stringify(payload));
    };
    SparkplugClient.prototype.compressPayload = function (payload, options) {
        var algorithm = null, compressedPayload, resultPayload = {
            "uuid": compressed,
            "body": "",
            "metrics": []
        };
        logger.debug("Compressing payload " + JSON.stringify(options));
        // See if any options have been set
        if (options !== undefined && options !== null) {
            // Check algorithm
            if (options['algorithm']) {
                algorithm = options['algorithm'];
            }
        }
        if (algorithm === null || algorithm.toUpperCase() === "DEFLATE") {
            logger.debug("Compressing with DEFLATE!");
            resultPayload.body = pako.deflate(payload);
        }
        else if (algorithm.toUpperCase() === "GZIP") {
            logger.debug("Compressing with GZIP");
            resultPayload.body = pako.gzip(payload);
        }
        else {
            throw new Error("Unknown or unsupported algorithm " + algorithm);
        }
        // Create and add the algorithm metric if is has been specified in the options
        if (algorithm !== null) {
            resultPayload.metrics = [{
                    "name": "algorithm",
                    "value": algorithm.toUpperCase(),
                    "type": "string"
                }];
        }
        return resultPayload;
    };
    SparkplugClient.prototype.decompressPayload = function (payload) {
        var metrics = payload.metrics, algorithm = null;
        logger.debug("Decompressing payload");
        if (metrics !== undefined && metrics !== null) {
            for (var i = 0; i < metrics.length; i++) {
                if (metrics[i].name === "algorithm") {
                    algorithm = metrics[i].value;
                }
            }
        }
        if (algorithm === null || algorithm.toUpperCase() === "DEFLATE") {
            logger.debug("Decompressing with DEFLATE!");
            return pako.inflate(payload.body);
        }
        else if (algorithm.toUpperCase() === "GZIP") {
            logger.debug("Decompressing with GZIP");
            return pako.ungzip(payload.body);
        }
        else {
            throw new Error("Unknown or unsupported algorithm " + algorithm);
        }
    };
    SparkplugClient.prototype.maybeCompressPayload = function (payload, options) {
        if (options !== undefined && options !== null && options.compress) {
            // Compress the payload
            return this.compressPayload(this.encodePayload(payload), options);
        }
        else {
            // Don't compress the payload
            return payload;
        }
    };
    SparkplugClient.prototype.maybeDecompressPayload = function (payload) {
        if (payload.uuid !== undefined && payload.uuid === compressed) {
            // Decompress the payload
            return this.decodePayload(this.decompressPayload(payload));
        }
        else {
            // The payload is not compressed
            return payload;
        }
    };
    SparkplugClient.prototype.subscribeTopic = function (topic, options, callback) {
        if (options === void 0) { options = { "qos": 0 }; }
        logger.info("Subscribing to topic:", topic);
        this.client.subscribe(topic, options, callback);
    };
    SparkplugClient.prototype.unsubscribeTopic = function (topic, options, callback) {
        logger.info("Unsubscribing topic:", topic);
        this.client.unsubscribe(topic, options, callback);
    };
    // Publishes Node BIRTH certificates for the edge node
    SparkplugClient.prototype.publishNodeBirth = function (payload, options) {
        var topic = this.version + "/" + this.groupId + "/NBIRTH/" + this.edgeNode;
        // Reset sequence number
        this.seq = 0;
        // Add seq number
        this.addSeqNumber(payload);
        // Add bdSeq number
        var metrics = payload.metrics;
        if (metrics !== undefined && metrics !== null) {
            metrics.push({
                "name": "bdSeq",
                "type": "uint64",
                "value": this.bdSeq
            });
        }
        // Publish BIRTH certificate for edge node
        logger.info("Publishing Edge Node Birth");
        var p = this.maybeCompressPayload(payload, options);
        this.client.publish(topic, this.encodePayload(p));
        this.messageAlert("published", topic, p);
    };
    // Publishes Node Data messages for the edge node
    SparkplugClient.prototype.publishNodeData = function (payload, options) {
        var topic = this.version + "/" + this.groupId + "/NDATA/" + this.edgeNode;
        // Add seq number
        this.addSeqNumber(payload);
        // Publish
        logger.info("Publishing NDATA");
        this.client.publish(topic, this.encodePayload(this.maybeCompressPayload(payload, options)));
        this.messageAlert("published", topic, payload);
    };
    // Publishes Node BIRTH certificates for the edge node
    SparkplugClient.prototype.publishDeviceData = function (deviceId, payload, options) {
        var topic = this.version + "/" + this.groupId + "/DDATA/" + this.edgeNode + "/" + deviceId;
        // Add seq number
        this.addSeqNumber(payload);
        // Publish
        logger.info("Publishing DDATA for device " + deviceId);
        this.client.publish(topic, this.encodePayload(this.maybeCompressPayload(payload, options)));
        this.messageAlert("published", topic, payload);
    };
    ;
    // Publishes Node BIRTH certificates for the edge node
    SparkplugClient.prototype.publishDeviceBirth = function (deviceId, payload, options) {
        var topic = this.version + "/" + this.groupId + "/DBIRTH/" + this.edgeNode + "/" + deviceId;
        // Add seq number
        this.addSeqNumber(payload);
        // Publish
        logger.info("Publishing DBIRTH for device " + deviceId);
        var p = this.maybeCompressPayload(payload, options);
        this.client.publish(topic, this.encodePayload(p));
        this.messageAlert("published", topic, p);
    };
    // Publishes Node BIRTH certificates for the edge node
    SparkplugClient.prototype.publishDeviceDeath = function (deviceId, payload) {
        var topic = this.version + "/" + this.groupId + "/DDEATH/" + this.edgeNode + "/" + deviceId, options = {};
        // Add seq number
        this.addSeqNumber(payload);
        // Publish
        logger.info("Publishing DDEATH for device " + deviceId);
        this.client.publish(topic, this.encodePayload(this.maybeCompressPayload(payload, options)));
        this.messageAlert("published", topic, payload);
    };
    SparkplugClient.prototype.stop = function () {
        logger.debug("publishDeath: " + this.publishDeath);
        if (this.publishDeath) {
            // Publish the DEATH certificate
            this.publishNDeath(this.client);
        }
        this.client.end();
    };
    // Configures and connects the client
    SparkplugClient.prototype.init = function () {
        var _this = this;
        // Connect to the MQTT server
        this.connecting = true;
        logger.debug("Attempting to connect: " + this.serverUrl);
        logger.debug("              options: " + JSON.stringify(this.mqttOptions));
        this.client = mqtt.connect(this.serverUrl, this.mqttOptions);
        logger.debug("Finished attempting to connect");
        /*
         * 'connect' handler
         */
        this.client.on('connect', function () {
            logger.info("Client has connected");
            _this.connecting = false;
            _this.connected = true;
            _this.emit("connect");
            // Subscribe to control/command messages for both the edge node and the attached devices
            logger.info("Subscribing to control/command messages for both the edge node and the attached devices");
            _this.client.subscribe(_this.version + "/" + _this.groupId + "/NCMD/" + _this.edgeNode + "/#", { "qos": 0 });
            _this.client.subscribe(_this.version + "/" + _this.groupId + "/DCMD/" + _this.edgeNode + "/#", { "qos": 0 });
            // Emit the "birth" event to notify the application to send a births
            _this.emit("birth");
        });
        /*
         * 'error' handler
         */
        this.client.on('error', function (error) {
            if (_this.connecting) {
                _this.emit("error", error);
                _this.client.end();
            }
        });
        /*
         * 'close' handler
         */
        this.client.on('close', function () {
            if (_this.connected) {
                _this.connected = false;
                _this.emit("close");
            }
        });
        /*
         * 'reconnect' handler
         */
        this.client.on("reconnect", function () {
            _this.emit("reconnect");
        });
        /*
         * 'reconnect' handler
         */
        this.client.on("offline", function () {
            _this.emit("offline");
        });
        /*
         * 'packetsend' handler
         */
        this.client.on("packetsend", function (packet) {
            logger.debug("packetsend: " + packet.cmd);
        });
        /*
         * 'packetreceive' handler
         */
        this.client.on("packetreceive", function (packet) {
            logger.debug("packetreceive: " + packet.cmd);
        });
        /*
         * 'message' handler
         */
        this.client.on('message', function (topic, message) {
            var payload = _this.maybeDecompressPayload(_this.decodePayload(message)), timestamp = payload.timestamp, splitTopic, metrics;
            _this.messageAlert("arrived", topic, payload);
            // Split the topic up into tokens
            splitTopic = topic.split("/");
            if (splitTopic[0] === _this.version
                && splitTopic[1] === _this.groupId
                && splitTopic[2] === "NCMD"
                && splitTopic[3] === _this.edgeNode) {
                // Emit the "command" event
                _this.emit("ncmd", payload);
            }
            else if (splitTopic[0] === _this.version
                && splitTopic[1] === _this.groupId
                && splitTopic[2] === "DCMD"
                && splitTopic[3] === _this.edgeNode) {
                // Emit the "command" event for the given deviceId
                _this.emit("dcmd", splitTopic[4], payload);
            }
            else {
                _this.emit("message", topic, payload);
            }
        });
    };
    return SparkplugClient;
}(events.EventEmitter));
function newClient(config) {
    return new SparkplugClient(config);
}
exports.newClient = newClient;
