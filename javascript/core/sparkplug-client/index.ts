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
import * as mqtt from 'mqtt';
import type { IClientOptions, MqttClient } from 'mqtt';
import events from 'events';
import * as sparkplug from 'sparkplug-payload';
import type { UPayload } from 'sparkplug-payload/lib/sparkplugbpayload';
import type { Reader } from 'protobufjs';
import pako from 'pako';
import createDebug from 'debug';

const sparkplugbpayload = sparkplug.get("spBv1.0");

const compressed = "SPBV1.0_COMPRESSED";

// setup logging
const debugLog = createDebug('sparkplug-client:debug');
const infoLog = createDebug('sparkplug-client:info');
const logger = {
    debug: (formatter: string, ...args: unknown[]) => debugLog(formatter, ...args),
    info: (formatter: string, ...args: unknown[]) => infoLog(formatter, ...args),
}

function getRequiredProperty(config, propName) {
    if (config[propName] !== undefined) {
        return config[propName];
    }
    throw new Error("Missing required configuration property '" + propName + "'");
}

function getProperty<C, P extends keyof C>(config: C, propName: P, defaultValue: C[P]) {
    if (config[propName] !== undefined) {
        return config[propName];
    } else {
        return defaultValue;
    }
}

export type ISparkplugClientOptions = {
    serverUrl: string;
    username: string;
    password: string;
    groupId: string;
    edgeNode: string;
    clientId: string;
    publishDeath?: boolean;
    version?: string;
    keepalive?: number;
    mqttOptions?: Omit<IClientOptions, 'clientId' | 'clean' | 'keepalive' | 'reschedulePings' | 'connectTimeout' | 'username' | 'password' | 'will'>;
}

/*
 * Sparkplug Client
 */
class SparkplugClient extends events.EventEmitter {

    // Constants
    private readonly type_int32: number = 7;
    private readonly type_boolean: number = 11;
    private readonly type_string: number = 12;
    private readonly versionB: string = "spBv1.0";

    // Config Variables
    private serverUrl: string;
    private groupId: string;
    private edgeNode: string;
    private publishDeath: boolean;
    private version: string;
    private mqttOptions: IClientOptions;

    // MQTT Client Variables
    private bdSeq: number;
    private seq: number;
    private devices: any[];
    private client: any;
    private connecting: boolean;
    private connected: boolean;

    constructor(config: ISparkplugClientOptions) {
        super();
        this.groupId = getRequiredProperty(config, "groupId");
        this.edgeNode = getRequiredProperty(config, "edgeNode");
        this.publishDeath = getProperty(config, "publishDeath", false);
        this.version = getProperty(config, "version", this.versionB);
        this.bdSeq = 0;
        this.seq = 0;
        this.devices = [];
        this.client = null;
        this.connecting = false;
        this.connected = false;

        // Client connection options
        this.serverUrl = getRequiredProperty(config, "serverUrl");
        const username = getRequiredProperty(config, "username");
        const password = getRequiredProperty(config, "password");
        const clientId = getRequiredProperty(config, "clientId");
        const keepalive = getProperty(config, "keepalive", 5);
        this.mqttOptions = {
            ...config.mqttOptions || {}, // allow additional options
            clientId,
            clean: true,
            keepalive,
            reschedulePings: false,
            connectTimeout: 30000,
            username,
            password,
            will: {
                topic: this.version + "/" + this.groupId + "/NDEATH/" + this.edgeNode,
                payload: this.encodePayload(this.getDeathPayload()),
                qos: 0,
                retain: false,
            },
        };

        this.init();
    }

    // Increments a sequence number
    private incrementSeqNum(): number {
        if (this.seq == 256) {
            this.seq = 0;
        }
        return this.seq++;
    }

    private encodePayload(payload): any {
        return sparkplugbpayload.encodePayload(payload);
    };

    private decodePayload(payload): any {
        return sparkplugbpayload.decodePayload(payload);
    }

    private addSeqNumber(payload): void {
        payload.seq = this.incrementSeqNum();
    }

    // Get DEATH payload
    private getDeathPayload(): any {
        return {
            "timestamp": new Date().getTime(),
            "metrics": [{
                "name": "bdSeq",
                "value": this.bdSeq,
                "type": "uint64"
            }]
        }
    }

    // Publishes DEATH certificates for the edge node
    private publishNDeath(client): void {
        let payload, topic;

        // Publish DEATH certificate for edge node
        logger.info("Publishing Edge Node Death");
        payload = this.getDeathPayload();
        topic = this.version + "/" + this.groupId + "/NDEATH/" + this.edgeNode;
        client.publish(topic, this.encodePayload(payload));
        this.messageAlert("published", topic, payload);
    }

    // Logs a message alert to the console
    private messageAlert(alert, topic, payload): void {
        logger.debug("Message " + alert);
        logger.debug(" topic: " + topic);
        logger.debug(" payload: " + JSON.stringify(payload));
    }

    private compressPayload(payload, options) {
        let algorithm = null,
            compressedPayload,
            resultPayload = {
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
        } else if (algorithm.toUpperCase() === "GZIP") {
            logger.debug("Compressing with GZIP");
            resultPayload.body = pako.gzip(payload);
        } else {
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
    }

    private decompressPayload(payload) {
        let metrics = payload.metrics,
            algorithm = null;

        logger.debug("Decompressing payload");

        if (metrics !== undefined && metrics !== null) {
            for (let i = 0; i < metrics.length; i++) {
                if (metrics[i].name === "algorithm") {
                    algorithm = metrics[i].value;
                }
            }
        }

        if (algorithm === null || algorithm.toUpperCase() === "DEFLATE") {
            logger.debug("Decompressing with DEFLATE!");
            return pako.inflate(payload.body);
        } else if (algorithm.toUpperCase() === "GZIP") {
            logger.debug("Decompressing with GZIP");
            return pako.ungzip(payload.body);
        } else {
            throw new Error("Unknown or unsupported algorithm " + algorithm);
        }
    }

    private maybeCompressPayload(payload, options) {
        if (options !== undefined && options !== null && options.compress) {
            // Compress the payload
            return this.compressPayload(this.encodePayload(payload), options);
        } else {
            // Don't compress the payload
            return payload;
        }
    }

    private maybeDecompressPayload(payload) {
        if (payload.uuid !== undefined && payload.uuid === compressed) {
            // Decompress the payload
            return this.decodePayload(this.decompressPayload(payload));
        } else {
            // The payload is not compressed
            return payload;
        }
    }

    subscribeTopic(topic: string, options = { "qos": 0 }, callback?) {
        logger.info("Subscribing to topic:", topic);
        this.client.subscribe(topic, options, callback);
    }

    unsubscribeTopic(topic: string, options?, callback?) {
        logger.info("Unsubscribing topic:", topic);
        this.client.unsubscribe(topic, options, callback);
    }

    // Publishes Node BIRTH certificates for the edge node
    publishNodeBirth(payload, options) {
        let topic = this.version + "/" + this.groupId + "/NBIRTH/" + this.edgeNode;
        // Reset sequence number
        this.seq = 0;
        // Add seq number
        this.addSeqNumber(payload);
        // Add bdSeq number
        let metrics = payload.metrics
        if (metrics !== undefined && metrics !== null) {
            metrics.push({
                "name": "bdSeq",
                "type": "uint64",
                "value": this.bdSeq
            });
        }

        // Publish BIRTH certificate for edge node
        logger.info("Publishing Edge Node Birth");
        let p = this.maybeCompressPayload(payload, options);
        this.client.publish(topic, this.encodePayload(p));
        this.messageAlert("published", topic, p);
    }

    // Publishes Node Data messages for the edge node
    publishNodeData(payload, options) {
        let topic = this.version + "/" + this.groupId + "/NDATA/" + this.edgeNode;
        // Add seq number
        this.addSeqNumber(payload);
        // Publish
        logger.info("Publishing NDATA");
        this.client.publish(topic, this.encodePayload(this.maybeCompressPayload(payload, options)));
        this.messageAlert("published", topic, payload);
    }

    // Publishes Node BIRTH certificates for the edge node
    publishDeviceData(deviceId, payload, options) {
        let topic = this.version + "/" + this.groupId + "/DDATA/" + this.edgeNode + "/" + deviceId;
        // Add seq number
        this.addSeqNumber(payload);
        // Publish
        logger.info("Publishing DDATA for device " + deviceId);
        this.client.publish(topic, this.encodePayload(this.maybeCompressPayload(payload, options)));
        this.messageAlert("published", topic, payload);
    };

    // Publishes Node BIRTH certificates for the edge node
    publishDeviceBirth(deviceId, payload, options) {
        let topic = this.version + "/" + this.groupId + "/DBIRTH/" + this.edgeNode + "/" + deviceId;
        // Add seq number
        this.addSeqNumber(payload);
        // Publish
        logger.info("Publishing DBIRTH for device " + deviceId);
        let p = this.maybeCompressPayload(payload, options);
        this.client.publish(topic, this.encodePayload(p));
        this.messageAlert("published", topic, p);
    }

    // Publishes Node BIRTH certificates for the edge node
    publishDeviceDeath(deviceId, payload) {
        let topic = this.version + "/" + this.groupId + "/DDEATH/" + this.edgeNode + "/" + deviceId,
            options = {};
        // Add seq number
        this.addSeqNumber(payload);
        // Publish
        logger.info("Publishing DDEATH for device " + deviceId);
        this.client.publish(topic, this.encodePayload(this.maybeCompressPayload(payload, options)));
        this.messageAlert("published", topic, payload);
    }

    stop() {
        logger.debug("publishDeath: " + this.publishDeath);
        if (this.publishDeath) {
            // Publish the DEATH certificate
            this.publishNDeath(this.client);
        }
        this.client.end();
    }

    // Configures and connects the client
    private init() {

        // Connect to the MQTT server
        this.connecting = true;
        logger.debug("Attempting to connect: " + this.serverUrl);
        logger.debug("              options: " + JSON.stringify(this.mqttOptions));
        this.client = mqtt.connect(this.serverUrl, this.mqttOptions);
        logger.debug("Finished attempting to connect");

        /*
         * 'connect' handler
         */
        this.client.on('connect', () => {
            logger.info("Client has connected");
            this.connecting = false;
            this.connected = true;
            this.emit("connect");

            // Subscribe to control/command messages for both the edge node and the attached devices
            logger.info("Subscribing to control/command messages for both the edge node and the attached devices");
            this.client.subscribe(this.version + "/" + this.groupId + "/NCMD/" + this.edgeNode + "/#", { "qos": 0 });
            this.client.subscribe(this.version + "/" + this.groupId + "/DCMD/" + this.edgeNode + "/#", { "qos": 0 });

            // Emit the "birth" event to notify the application to send a births
            this.emit("birth");
        });

        /*
         * 'error' handler
         */
        this.client.on('error', (error) => {
            if (this.connecting) {
                this.emit("error", error);
                this.client.end();
            }
        });

        /*
         * 'close' handler
         */
        this.client.on('close', () => {
            if (this.connected) {
                this.connected = false;
                this.emit("close");
            }
        });

        /*
         * 'reconnect' handler
         */
        this.client.on("reconnect", () => {
            this.emit("reconnect");
        });

        /*
         * 'reconnect' handler
         */
        this.client.on("offline", () => {
            this.emit("offline");
        });

        /*
         * 'packetsend' handler
         */
        this.client.on("packetsend", (packet) => {
            logger.debug("packetsend: " + packet.cmd);
        });

        /*
         * 'packetreceive' handler
         */
        this.client.on("packetreceive", (packet) => {
            logger.debug("packetreceive: " + packet.cmd);
        });

        /*
         * 'message' handler
         */
        this.client.on('message', (topic, message) => {
            let payload = this.maybeDecompressPayload(this.decodePayload(message)),
                timestamp = payload.timestamp,
                splitTopic,
                metrics;

            this.messageAlert("arrived", topic, payload);

            // Split the topic up into tokens
            splitTopic = topic.split("/");
            if (splitTopic[0] === this.version
                && splitTopic[1] === this.groupId
                && splitTopic[2] === "NCMD"
                && splitTopic[3] === this.edgeNode) {
                // Emit the "command" event
                this.emit("ncmd", payload);
            } else if (splitTopic[0] === this.version
                && splitTopic[1] === this.groupId
                && splitTopic[2] === "DCMD"
                && splitTopic[3] === this.edgeNode) {
                // Emit the "command" event for the given deviceId
                this.emit("dcmd", splitTopic[4], payload);
            } else {
                this.emit("message", topic, payload);
            }
        });
    }
}

export function newClient(config: ISparkplugClientOptions): SparkplugClient {
    return new SparkplugClient(config);
}
