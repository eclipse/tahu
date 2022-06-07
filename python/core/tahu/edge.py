"""
Edge Node and Device SparkplugB/MQTT library from Eclipse
"""

#/********************************************************************************
# * Copyright (c) 2022 Justin Brzozoski
# *
# * This program and the accompanying materials are made available under the
# * terms of the Eclipse Public License 2.0 which is available at
# * http://www.eclipse.org/legal/epl-2.0.
# *
# * SPDX-License-Identifier: EPL-2.0
# *
# * Contributors:
# *   Justin Brzozoski @ SignalFire Wireless Telemetry
# ********************************************************************************/

import os
import threading
import logging
import enum
import paho.mqtt.client as mqtt
import tahu
from . import sparkplug_b_pb2

def _rebirth_command_handler(tag, context, value):
    """
    Metric command handler for "Node Control/Rebirth"

    This does the well-known action in response to a Rebirth command, and causes the
    edge node's birth messages to be re-sent.

    :param tag: Metric object that this command was received on
    :param context: the optional cmd_context object provided to the Metric when it was created
    :param value: the new value received over Sparkplug

    """
    # TODO - Add support for rebirth requests on subdevices
    tag._logger.info('Rebirth command received')
    assert(isinstance(tag._parent_device, Node))
    # We don't care what value the server wrote to the tag, any write is considered a trigger.
    tag._parent_device._needs_to_send_birth = True

def _next_server_command_handler(tag, context, value):
    """
    Metric command handler for "Node Control/Next Server"

    This does the well-known action in response to a Next Server command, and causes the
    edge node to disconnect from the MQTT broker and immediately reconnect to the next known
    server. This will be the same server if only one is configured.

    :param tag: Metric object that this command was received on
    :param context: the optional cmd_context object provided to the Metric when it was created
    :param value: the new value received over Sparkplug

    """
    tag._logger.info('Next Server command received')
    assert(isinstance(tag._parent_device, Node))
    # We don't care what value the server wrote to the tag, any write is considered a trigger.
    tag._parent_device._mqtt_param_index = (tag._parent_device._mqtt_param_index
                                            + 1) \
        % len(tag._parent_device._mqtt_params)
    tag._parent_device._reconnect_client = True


class Metric(object):
    """
    The Metric object manages all aspects of a single metric

    The change_value is used to report new values over Sparkplug, and the cmd_handler provided when created will be called if a new value is received from Sparkplug.

    """
    def __init__(self, parent_device, name, datatype=None, value=None,
                 cmd_handler=None, cmd_context=None):
        """
        Initialize a Metric object

        When the object is created, the parent_device and name must be provided, as well as either a datatype or initial value.

        :param parent_device: the Node or Device object this metric will be attached to
        :param name: the name of this metric within the node or device (must be unique within this device)
        :param datatype: optional tahu.DataType for this metric. if not specified, will be auto-detected from the initial value
        :param value: optional initial value for this metric. if not specified, the datatype parameter is required to specify the type explicitly
        :param cmd_handler: optional handler callback which will be triggered when a NCMD/DCMD message is received for this metric
        :param cmd_context: optional context to pass to the cmd_handler if desired
        """
        # TODO - Protect the name/alias from being changed after creation
        if datatype is None and value is None:
            raise ValueError('Unable to define metric without explicit datatype or initial value')
        self._parent_device = parent_device
        self._logger = parent_device._logger
        self._u32_in_long = parent_device._u32_in_long
        self.name = str(name)
        if datatype:
            self._datatype = tahu.DataType(datatype)
        else:
            self._datatype = tahu._get_datatype_from_type(type(value))
            if self._datatype is None:
                raise ValueError('Need explicit datatype for Python type {}'.format(type(value)))

        self._value = value
        self._last_received = None
        self._last_sent = None
        self._cmd_handler = cmd_handler
        self._cmd_context = cmd_context
        self._properties = []
        self.alias = parent_device._attach_tag(self)

    def _attach_property(self, property):
        """
        Attach a Sparkplug property object to this metric

        This method is normally not called directly, but instead by the init functions of the property object.

        :param property: the property object to attach

        """
        next_index = len(self._properties)
        self._properties.append(property)
        # TODO - Add checking/handling depending if we're connected
        return next_index

    def _fill_in_payload_metric(self, new_metric, birth=False):
        """
        Fill in the Metric message object provided with the metrics most recent values

        :param new_metric: a Sparkplug Payload.Metric message object to fill in
        :param birth: a boolean indicating if this is part of a birth payload and should include all properties. if false, will only include those properties that can change dynamically (Default value = False)

        """
        if birth:
            new_metric.name = self.name
        new_metric.alias = self.alias
        new_metric.datatype = self._datatype
        # Add properties
        for p in self._properties:
            # This chunk could arguably be a method of the property, but I
            # felt it made more sense here because of the way the
            # PropertySet protobuf object works...
            if birth or p._report_with_data:
                new_metric.properties.keys.append(p._name)
                pvalue = new_metric.properties.values.add()
                pvalue.type = p._datatype
                tahu.value_to_sparkplug(pvalue, pvalue.type,
                                        p._value,
                                        self._u32_in_long)
                p._last_sent = p._value
        # Add the current value or set is_null if None
        if self._value is None:
            new_metric.is_null = True
        else:
            tahu.value_to_sparkplug(new_metric, self._datatype,
                                    self._value,
                                    self._u32_in_long)
        self._last_sent = self._value

    def change_value(self, value, send_immediate=True):
        """
        Update the known value of the metric and optionally cause a payload to be sent immediately

        Returns an alias number to use with send_data to trigger a sending of this metric manually

        :param value: the new value of the metric
        :param send_immediate: whether this change should trigger a payload containing this metric to be sent immediately. If true, other unchanged metrics will not be sent with this metric, and this metric will be sent even if the new value is identical to the previous. (Default value = True)

        """
        self._value = value
        if send_immediate:
            self._parent_device.send_data([self.alias])
        return self.alias

    def _handle_sparkplug_command(self, Metric):
        """
        Stub for handling received metrics and calling out to cmd_handler hooks as needed

        :param Metric: the Sparkplug Payload.Metric message received for this metric

        """
        # Note that we enforce OUR expected datatype on the value as we pull it from the metric
        try:
            value = tahu.value_from_sparkplug(Metric,
                                              self._datatype)
        except tahu.SparkplugDecodeError as errmsg:
            self._logger.warning('Sparkplug decode error for tag {}: {}'.format(self.name,
                                                                                errmsg))
            return
        self._logger.debug('Command received for tag {} = {}'.format(self.name,
                                                                     value))
        if self._cmd_handler:
            self._cmd_handler(self, self._cmd_context, value)
        else:
            self._logger.info('Received command for tag {} with no handler. No action taken.'.format(self.name))
        self._last_received = value

    def changed_since_last_sent(self):
        """If the metric value or any of the dynamic properties have changed since the most recent publish, returns true"""
        for p in self._properties:
            if p._report_with_data and p.changed_since_last_sent():
                return True
        return (self._value != self._last_sent)


class MetricProperty(object):
    """
    The MetricProperty object manages all aspects of a single metric property
    """
    def __init__(self, parent_metric, name, datatype, value,
                 report_with_data=False):
        """
        Initialize a MetricProperty object

        When the object is created, the parent_metric, name, datatype, and value must be provided.

        :param parent_metric: the Metric object this property will be attached to
        :param name: the name of this property within the metric (must be unique within this metric)
        :param datatype: tahu.DataType for this property
        :param value: initial value for this property
        :param report_with_data: whether this property should be included in every DATA publish, or only with BIRTH (Default value = False)
        """
        self._parent_metric = parent_metric
        self._name = str(name)
        if datatype:
            self._datatype = tahu.DataType(datatype)
        else:
            self._datatype = tahu._get_datatype_from_type(type(value))
            if self._datatype is None:
                raise ValueError('Need explicit datatype for Python type {}'.format(type(value)))
        self._value = value
        self._report_with_data = bool(report_with_data)
        self._last_sent = None
        self._parent_metric._attach_property(self)

    def changed_since_last_sent(self):
        """If the preoprty value has changed since the most recent publish, returns true"""
        return (self._value != self._last_sent)

    def change_value(self, value, send_immediate=False):
        """
        Update the value of the property and optionally cause a payload to be sent immediately

        Returns an alias number to use with send_data to trigger a sending of the parent metric manually

        :param value: new property value
        :param send_immediate: whether this change should trigger a payload containing the containing metric to be sent immediately. If true, other unchanged metrics will not be sent with this metric, and this metric will be sent even if the new value is identical to the previous. (Default value = False)

        """
        # TODO - Trigger rebirth if someone changes a property that is not report_with_data?
        self._value = value
        if self._report_with_data and send_immediate:
            self._parent_metric._parent_device.send_data([self._parent_metric.alias])
        return self._parent_metric.alias


def bulk_properties(parent_metric, property_dict):
    """
    Create multiple property objects and attach them all to the same metric quickly and easily

    This function is useful for creating many properties that will not change value dynamically with simple datatypes. The Sparkplug datatype of each property is autodetected based on the Python type of the corresponding value.

    Returns a list of MetricProperty objects

    :param parent_metric: the Metric object to attach the properties to
    :param property_dict: a dict mapping property names (keys) to values

    """
    return [MetricProperty(parent_metric, name, None, property_dict[name],
                           False) for name in property_dict.keys()]


class _AbstractBaseDevice(object):
    """
    A base object type containing common aspects of both Node and Device objects

    The _AbstractBaseDevice should not be instantiated directly. Use Node or Device instead.
    """
    def __init__(self):
        self._mqtt_client = None
        self._tags = []
        self._needs_to_send_birth = True

    def get_tag_names(self):
        """Return a list of the names of all metrics on this device"""
        return [m.name for m in self._tags]

    def _get_next_seq(self):
        """Returns the Sparkplug `seq` number to use on the next publish"""
        raise NotImplementedError('_get_next_seq not implemented on this class')

    def _attach_tag(self, tag):
        """
        Attach a Metric object to this device

        This method is normally not called directly, but instead by the init functions of the Metric object.

        :param tag: the metric object to attach

        """
        next_index = len(self._tags)
        self._tags.append(tag)
        if self.is_connected():
            self.send_death()
        self._needs_to_send_birth = True
        return next_index

    # TODO - Add another function to remove a tag

    def _get_payload(self, alias_list, birth):
        """
        Create and return a Sparkplug Payload message for this device and the given metric aliases

        Do not call directly. Use the send_data or send_birth device methods instead.

        :param alias_list: list of aliases to include in payload (ignored if birth=True)
        :param birth: bool to indicate if this payload is a birth. includes all metrics and all properties if so.

        """
        tx_payload = sparkplug_b_pb2.Payload()
        tx_payload.timestamp = tahu.timestamp_to_sparkplug()
        tx_payload.seq = self._get_next_seq()
        if birth:
            alias_list = range(len(self._tags))
        for m in alias_list:
            new_metric = tx_payload.metrics.add()
            self._tags[m]._fill_in_payload_metric(new_metric, birth=birth)
        return tx_payload

    def _get_topic(self, cmd_type):
        """
        Return the topic string to use for a command of the type given on this device object

        Not normally called directly.

        :param cmd_type: string indicating message type; usually one of 'BIRTH', 'DEATH', 'DATA', or 'CMD'

        """
        raise NotImplementedError('_get_topic not implemented on this class')

    def send_birth(self):
        """
        Generate and send a birth message for this device.

        Will trigger births on parent device or subdevices as needed.

        Returns the result of calling the `publish` function of the MQTT client library (paho)

        """
        raise NotImplementedError('send_birth not implemented on this class')

    def send_death(self):
        """
        Generate and send a death message for this device.

        Will flag device(s) to send birth on next update as needed.

        Returns the result of calling the `publish` function of the MQTT client library (paho)

        """
        raise NotImplementedError('send_death not implemented on this class')

    def send_data(self, aliases=None, changed_only=False):
        """
        Generate and send a data message for this device.

        Returns the result of calling the `publish` function of the MQTT client library (paho)

        :param aliases: list of metric aliases to include in this payload; if None will include all metrics (Default value = None)
        :param changed_only: whether to filter the metrics to only include those that have changed since the prior publish (Default value = False)

        """
        if not self.is_connected():
            self._logger.warning('Trying to send data when not connected. Skipping.')
            return
        if self._needs_to_send_birth:
            return self.send_birth()
        if aliases is None:
            aliases = range(len(self._tags))
        if changed_only:
            aliases = [x for x in aliases if self._tags[x].changed_since_last_sent()]
        if len(aliases) == 0:
            return

        tx_payload = self._get_payload(aliases, False)
        topic = self._get_topic('DATA')
        return self._mqtt_client.publish(topic,
                                         tx_payload.SerializeToString())

    def get_watched_topic(self):
        """Return the MQTT topic string on which this device expects to receive messages"""
        return self._get_topic('CMD')

    def _handle_payload(self, topic, payload):
        """
        Handle a received Sparkplug payload

        Returns true/false to indicate whether this device was the intended recipient and the message was handled

        :param topic: MQTT topic string the payload was received on
        :param payload: a sparkplug_b_pb2.Payload object containing the decoded payload

        """
        # Check if topic is for this device
        watched_topic = self.get_watched_topic()
        if topic != watched_topic:
            return False
        local_names = self.get_tag_names()
        for pm in payload.metrics:
            if pm.HasField('alias'):
                if pm.alias >= len(self._tags):
                    self._logger.warning('Invalid alias {} for this device. Skipping metric.'.format(pm.alias))
                    continue
                # TODO - If a "name" field was also provided, confirm it matches the expected?
                self._tags[pm.alias]._handle_sparkplug_command(pm)
            elif pm.HasField('name'):
                if not pm.name in local_names:
                    self._logger.warning('Invalid name {} for this device. Skipping metric.'.format(pm.name))
                    continue
                self._tags[local_names.index(pm.name)]._handle_sparkplug_command(pm)
            else:
                self._logger.warning('No name or alias provided. Skipping metric.')
                continue
        # Even if the payload was corrupt/weird, the topic was for us.
        # We can return True to let the caller know it was handled
        return True

    def is_connected(self):
        """Return true/false to indicate if MQTT connection is fully established and ready to use"""
        raise NotImplementedError('is_connected not implemented on this class')


class Node(_AbstractBaseDevice):
    """
    An object to manage a Sparkplug edge node, including metrics and subdevices and MQTT client connections
    """
    def __init__(self, mqtt_params, group_id, edge_node_id,
                 provide_bdSeq=True, provide_controls=True, logger=None,
                 u32_in_long=False):
        """
        Initializer method for Node

        :param mqtt_params: list of one or more tahu.mqtt_params objects containing MQTT client configurations
        :param group_id: string to use as group ID in MQTT Sparkplug topics
        :param edge_node_id: string to use as edge node ID in MQTT Sparkplug topics
        :param provide_bdSeq: optional boolean to indicate if bdSeq metric should be created/used according to Sparkplug spec (Default value = True)
        :param provide_controls: optional boolean to indicate if well-known control metrics "Node Control/Rebirth" and "Node Control/Next Server" should be created/used according to Sparkplug reference implementations (Default value = True)
        :param logger: optional logging.logger object to handle log messages from this device and all objects attached to it. if None then a new one is created. (Default value = None)
        :param u32_in_long: optional boolean to indicate if metrics/properties with datatype UINT32 should put their values in the long_value or int_value part of the payload. (Default value = False)

        """
        super().__init__()
        self._mqtt_params = list(mqtt_params)
        self._mqtt_param_index = 0
        self._u32_in_long = bool(u32_in_long)
        self._group_id = str(group_id)
        self._edge_node_id = str(edge_node_id)
        node_reference = '{}_{}'.format(self._group_id, self._edge_node_id)
        self._logger = logger if logger else logging.getLogger(node_reference)
        self._mqtt_logger = self._logger.getChild('mqtt')
        self._init_mqtt_client()
        self._sequence = 0
        self._subdevices = []
        self._all_device_topics = [ self.get_watched_topic() ]
        self._thread = None
        self._thread_terminate = True
        self._reconnect_client = False

        if provide_bdSeq:
            # We use the timestamp as our bdSeq since we do not have a persistent counter
            new_tag = Metric(self, 'bdSeq',
                             tahu.DataType.Int64,
                             value=tahu.timestamp_to_sparkplug())
            self._bdseq_alias = new_tag.alias
        else:
            self._bdseq_alias = None
        if provide_controls:
            # We do not support "Node Control/Reboot" since we can't reboot ourselves easily
            #Metric(self, 'Node Control/Reboot', tahu.DataType.Boolean, value=False)
            Metric(self, 'Node Control/Rebirth',
                   tahu.DataType.Boolean, value=False,
                   cmd_handler=_rebirth_command_handler)
            Metric(self, 'Node Control/Next Server',
                   tahu.DataType.Boolean, value=False,
                   cmd_handler=_next_server_command_handler)

    def _get_next_seq(self):
        """Returns the Sparkplug `seq` number to use on the next publish"""
        seq_to_use = self._sequence
        self._sequence = (self._sequence + 1) % 256
        return seq_to_use

    def send_birth(self):
        """
        Generate and send a birth message for this device.

        Will trigger births on subdevices as needed.

        Returns the result of calling the `publish` function of the MQTT client library (paho)

        """
        if not self.is_connected():
            self._logger.warning('Trying to send birth when not connected. Skipping.')
            return
        self._sequence = 0
        tx_payload = self._get_payload(None, True)
        topic = self._get_topic('BIRTH')
        pub_result = self._mqtt_client.publish(topic,
                                               tx_payload.SerializeToString())
        if pub_result.rc != 0:
            return pub_result
        self._needs_to_send_birth = False
        for d in self._subdevices:
            d._needs_to_send_birth = True
        return pub_result

    def _get_death_payload(self, will):
        """
        Create and return a Sparkplug Payload DEATH message for this device

        :param will: boolean to indicate if this message will be used as the MQTT LWT. if True, bdSeq will be updated if available.

        """
        if self._bdseq_alias is not None:
            if will:
                # We use the timestamp as our bdSeq since we do not have a persistent counter
                new_bdseq = tahu.timestamp_to_sparkplug()
                self._logger.debug('Generating new WILL bdSeq={}'.format(new_bdseq))
                self._tags[self._bdseq_alias].change_value(new_bdseq,
                                                           send_immediate=False)
            death_payload = self._get_payload([self._bdseq_alias], False)
            # This timestamp would be wrong when finally sent, so we just remove it
            death_payload.ClearField('timestamp')
            # To be safe, add the name to the bdSeq metric and don't use the alias
            death_payload.metrics[0].name = 'bdSeq'
            death_payload.metrics[0].ClearField('alias')
        else:
            death_payload = self._get_payload([], False)
        return death_payload

    def _get_will_topic_and_payload(self):
        """Returns a tuple of the MQTT topic and payload bytes to use as the LWT for this edge node device"""
        tx_payload = self._get_death_payload(will=True)
        topic = self._get_topic('DEATH')
        return topic, tx_payload.SerializeToString()

    def send_death(self):
        """
        Generate and send a death message for this device.

        Will flag device(s) to send birth on next update as needed.

        Returns the result of calling the `publish` function of the MQTT client library (paho)

        """
        if not self.is_connected():
            self._logger.warning('Trying to send death when not connected. Skipping.')
            return
        tx_payload = self._get_death_payload(will=False)
        topic = self._get_topic('DEATH')
        pub_result = self._mqtt_client.publish(topic,
                                               tx_payload.SerializeToString())
        # Even if this publish didn't succeed, it's safer to rebirth unnecessarily...
        self._needs_to_send_birth = True
        for d in self._subdevices:
            d.needs_to_birth = True
        return pub_result

    def _attach_subdevice(self, subdevice):
        """
        Attach a newly create subdevice to this parent device

        Returns the new subdevice's handle from the parent device context.

        :param subdevice: newly created Device object to attach
        """
        next_index = len(self._subdevices)
        self._subdevices.append(subdevice)
        self._all_device_topics.append(subdevice.get_watched_topic())
        if self.is_connected():
            self.send_death()
        self._needs_to_send_birth = True
        return next_index

    # TODO - Add another function to remove a subdevice

    def _get_topic(self, cmd_type):
        """
        Return the topic string to use for a command of the type given on this device object

        Not normally called directly.

        :param cmd_type: string indicating message type; usually one of 'BIRTH', 'DEATH', 'DATA', or 'CMD'

        """
        return 'spBv1.0/{}/N{}/{}'.format(self._group_id, cmd_type,
                                          self._edge_node_id)

    def _mqtt_subscribe(self):
        """
        Activate MQTT subscriptions for the Node and all Device sub-devices

        This builds the proper topic lists and triggers the subscription command to the MQTT broker.

        Returns the result of calling Paho `subscribe` API command
        """
        # TODO - Add support for 'STATE/#' monitoring and holdoff?
        # Subscribe to all topics for commands related to this device...
        ncmd_subscription = 'spBv1.0/{}/NCMD/{}/#'.format(self._group_id,
                                                          self._edge_node_id)
        dcmd_subscription = 'spBv1.0/{}/DCMD/{}/#'.format(self._group_id,
                                                          self._edge_node_id)
        desired_qos = 0
        topic = [(ncmd_subscription,desired_qos), (dcmd_subscription,
                                                   desired_qos)]
        return self._mqtt_client.subscribe(topic)

    def _mqtt_on_connect(self, client, userdata, flags, rc):
        """Callback handler for Paho (MQTT) on_connect events"""
        if rc != 0:
            self._logger.warning('MQTT connect error rc={}'.format(rc))
            return
        self._is_connected = True
        # A fresh connection implies we have no subscriptions and need to birth
        self._is_subscribed = False
        self._needs_to_send_birth = True
        for d in self._subdevices:
            d._needs_to_send_birth = True
        self._mqtt_subscribe()

    def _mqtt_on_disconnect(self, client, userdata, rc):
        """Callback handler for Paho (MQTT) on_disconnect events"""
        self._logger.warning('MQTT disconnect rc={}'.format(rc))
        self._is_connected = False
        # The thread loop will try reconnecting for us, we just need to setup a new will first
        will_topic, will_payload = self._get_will_topic_and_payload()
        client.will_set(will_topic, will_payload)

    def _mqtt_on_message(self, client, userdata, message):
        """Callback handler for Paho (MQTT) on_message events"""
        if message.topic in self._all_device_topics:
            rx_payload = sparkplug_b_pb2.Payload()
            rx_payload.ParseFromString(message.payload)
            handler_index = self._all_device_topics.index(message.topic)
            if handler_index == 0:
                self._handle_payload(message.topic, rx_payload)
            else:
                self._subdevices[(handler_index
                                  - 1)]._handle_payload(message.topic,
                                                        rx_payload)
        else:
            self._logger.info('Ignoring MQTT message on topic {}'.format(message.topic))

    def _mqtt_on_subscribe(self, client, userdata, mid, granted_qos):
        """Callback handler for Paho (MQTT) on_subscribe events"""
        # TODO - Confirm the mid matches our subscription request before assuming we're good to go?
        self._is_subscribed = True

    def _init_mqtt_client(self, reinit=False):
        """Used to initialize MQTT client from nothing or with reinit=True to forcefully abort a connection and trigger NDEATH LWT payload from MQTT broker"""
        curr_params = self._mqtt_params[self._mqtt_param_index]
        if curr_params['client_id']:
            self._client_id = curr_params['client_id']
        else:
            self._client_id = '{}_{}_{}'.format(self._group_id,
                                                self._edge_node_id,
                                                os.getpid())
        self._logger.info('Initializing MQTT client (client_id={} reinit={})'.format(self._client_id,
                                                                                     reinit))
        if reinit:
            self._mqtt_client.reinitialise(client_id=self._client_id)
        else:
            self._mqtt_client = mqtt.Client(client_id=self._client_id)
        self._mqtt_client.enable_logger(self._mqtt_logger)
        self._mqtt_client.on_connect = self._mqtt_on_connect
        self._mqtt_client.on_disconnect = self._mqtt_on_disconnect
        self._mqtt_client.on_message = self._mqtt_on_message
        self._mqtt_client.on_subscribe = self._mqtt_on_subscribe
        self._is_connected = False
        self._is_subscribed = False

    def _prep_client_connection(self):
        """Used to configure and start a MQTT client connection"""
        if self._is_connected:
            self._logger.error('Attempting to start MQTT connection while already connected. Skipping.')
            return
        curr_params = self._mqtt_params[self._mqtt_param_index]
        if curr_params['username']:
            self._mqtt_client.username_pw_set(curr_params['username'],
                                              curr_params['password'])
        if (curr_params['port']
            == 1883 and curr_params['tls_enabled']) or (curr_params['port']
                                                        == 8883 and not curr_params['tls_enabled']):
            self._logger.warning('Setting up MQTT params on well-known port with unexpected TLS setting. Are you sure you meant to do this?')
        if curr_params['tls_enabled']:
            self._mqtt_client.tls_set(ca_certs=curr_params['ca_certs'],
                                      certfile=curr_params['certfile'],
                                      keyfile=curr_params['keyfile'])
        will_topic, will_payload = self._get_will_topic_and_payload()
        self._mqtt_client.will_set(will_topic, will_payload)
        self._logger.info('Starting MQTT client connection to host={}'.format(curr_params['server']))
        self._mqtt_client.connect(host=curr_params['server'],
                                  port=curr_params['port'],
                                  keepalive=curr_params['keepalive'])

    def _thread_main(self):
        """
        Thread worker loop that coordinates all MQTT recv operations and application interactions

        It maintains MQTT broker connections and handles birth/death of devices and sub-devices as needed.
        """
        # TODO - Add support to timeout bad/failed connections to trigger _reconnect_client
        self._logger.info('MQTT thread started...')
        self._prep_client_connection()
        while not self._thread_terminate:
            self._mqtt_client.loop()
            if self._reconnect_client:
                self._reconnect_client = False
                self._init_mqtt_client(reinit=True)
                self._prep_client_connection()
            elif self.is_connected():
                if self._needs_to_send_birth:
                    self.send_birth()
                else:
                    # Only try to send subdevice births if the top-level device doesn't need it
                    for d in self._subdevices:
                        if d._needs_to_send_birth:
                            d.send_birth()
        # Use the reinit as a trick to force the sockets closed
        self._init_mqtt_client(reinit=True)
        self._logger.info('MQTT thread stopped...')

    def online(self):
        """
        Request Node go online if not already

        Starts a new worker thread if needed.
        """
        if self._thread is not None:
            self._logger.warning('MQTT thread already running!')
            return
        self._thread_terminate = False
        self._thread = threading.Thread(target=self._thread_main)
        self._thread.daemon = True
        self._thread.start()

    def offline(self):
        """
        Request Node go offline if not already

        Blocks until worker thread is stopped if not run from that thread
        """
        self._logger.info('Requesting MQTT thread stop...')
        self._thread_terminate = True
        if self._thread is None:
            self._logger.warning('MQTT thread not running!')
        elif threading.current_thread() != self._thread:
            self._thread.join()
            self._thread = None

    def is_connected(self):
        """Returns True if Node is properly connected to a MQTT broker"""
        return self._is_connected and self._is_subscribed


class Device(_AbstractBaseDevice):
    """An object to manage a Sparkplug sub-device, including metrics and parent edge node reference"""
    def __init__(self, parent_device, name):
        super().__init__()
        # TODO - Protect the name from being changed after creation
        self.name = str(name)
        self._parent_device = parent_device
        self._logger = parent_device._logger.getChild(self.name)
        self._mqtt_client = parent_device._mqtt_client
        self._mqtt_logger = parent_device._mqtt_logger
        self._u32_in_long = parent_device._u32_in_long
        self._parent_index = self._parent_device._attach_subdevice(self)

    def _get_next_seq(self):
        """Returns the Sparkplug `seq` number to use on the next publish"""
        return self._parent_device._get_next_seq()

    def send_birth(self):
        """
        Generate and send a birth message for this device.

        Will trigger births on parent device as needed.

        Returns the result of calling the `publish` function of the MQTT client library (paho)

        """
        if not self.is_connected():
            self._logger.warning('Trying to send birth when not connected. Skipping.')
            return
        # If the parent device also needs to birth, do that first!
        if self._parent_device._needs_to_send_birth:
            return self._parent_device.send_birth()
        tx_payload = self._get_payload(None, True)
        topic = self._get_topic('BIRTH')
        pub_result = self._mqtt_client.publish(topic,
                                               tx_payload.SerializeToString())
        if pub_result.rc == 0:
            self._needs_to_send_birth = False
        return pub_result

    def send_death(self):
        """
        Generate and send a death message for this device.

        Will flag device(s) to send birth on next update as needed.

        Returns the result of calling the `publish` function of the MQTT client library (paho)

        """
        if not self.is_connected():
            self._logger.warning('Trying to send death when not connected. Skipping.')
            return
        tx_payload = self._get_payload([], False)
        topic = self._get_topic('DEATH')
        pub_result = self._mqtt_client.publish(topic,
                                               tx_payload.SerializeToString())
        # Even if this publish didn't succeed, it's safer to rebirth unnecessarily...
        self._needs_to_send_birth = True
        return pub_result

    def _get_topic(self, cmd_type):
        """
        Return the topic string to use for a command of the type given on this device object

        Not normally called directly.

        :param cmd_type: string indicating message type; usually one of 'BIRTH', 'DEATH', 'DATA', or 'CMD'

        """
        return 'spBv1.0/{}/D{}/{}/{}'.format(self._parent_device._group_id,
                                             cmd_type,
                                             self._parent_device._edge_node_id,
                                             self.name)

    def is_connected(self):
        """Returns True if the parent Node is properly connected to a MQTT broker"""
        return self._parent_device.is_connected()


class IgnitionQualityCode(enum.IntEnum):
    """A list of values for the quality property that are understood by Ignition"""
    Bad = -2147483136
    Bad_AccessDenied = -2147483134
    Bad_AggregateNotFound = -2147483127
    Bad_DatabaseNotConnected = -2147483123
    Bad_Disabled = -2147483133
    Bad_Failure = -2147483121
    Bad_GatewayCommOff = -2147483125
    Bad_LicenseExceeded = -2147483130
    Bad_NotConnected = -2147483126
    Bad_NotFound = -2147483129
    Bad_OutOfRange = -2147483124
    Bad_ReadOnly = -2147483122
    Bad_ReferenceNotFound = -2147483128
    Bad_Stale = -2147483132
    Bad_TrialExpired = -2147483131
    Bad_Unauthorized = -2147483135
    Bad_Unsupported = -2147483120
    Error = -1073741056
    Error_Configuration = -1073741055
    Error_CycleDetected = -1073741044
    Error_DatabaseQuery = -1073741051
    Error_Exception = -1073741048
    Error_ExpressionEval = -1073741054
    Error_Formatting = -1073741046
    Error_IO = -1073741050
    Error_InvalidPathSyntax = -1073741047
    Error_ScriptEval = -1073741045
    Error_TagExecution = -1073741053
    Error_TimeoutExpired = -1073741049
    Error_TypeConversion = -1073741052
    Good = 192
    Good_Initial = 201
    Good_Provisional = 200
    Good_Unspecified = 0
    Good_WritePending = 2
    Uncertain = 1073742080
    Uncertain_DataSubNormal = 1073742083
    Uncertain_EngineeringUnitsExceeded = 1073742084
    Uncertain_IncompleteOperation = 1073742085
    Uncertain_InitialValue = 1073742082
    Uncertain_LastKnownValue = 1073742081


def ignition_quality_property(parent_metric, value=IgnitionQualityCode.Good):
    """
    Create a dynamic tag quality property that will be understood by Ignition

    :param parent_metric: the Metric object to attach the property to
    :param value: the current ignition.QualityCode value (Default value = ignition.QualityCode.Good)

    """
    return MetricProperty(parent_metric, 'Quality',
                          tahu.DataType.Int32, value,
                          True)

def ignition_low_property(parent_metric, value):
    """
    Create a tag low-range (engLow) property that will be understood by Ignition

    Uses the same Sparkplug datatype on the property as the parent metric

    :param parent_metric: the Metric object to attach the property to
    :param value: the low range of the metric

    """
    return MetricProperty(parent_metric, 'engLow',
                          parent_metric._datatype,
                          value, False)

def ignition_high_property(parent_metric, value):
    """
    Create a tag high-range (engHigh) property that will be understood by Ignition

    Uses the same Sparkplug datatype on the property as the parent metric

    :param parent_metric: the Metric object to attach the property to
    :param value: the high range of the metric

    """
    return MetricProperty(parent_metric, 'engHigh',
                          parent_metric._datatype,
                          value, False)

def ignition_unit_property(parent_metric, value):
    """
    Create a tag units (engUnit) property that will be understood by Ignition

    :param parent_metric: the Metric object to attach the property to
    :param value: the units of the metric

    """
    return MetricProperty(parent_metric, 'engUnit',
                          tahu.DataType.String, value,
                          False)

def ignition_documentation_property(parent_metric, value):
    """
    Create a tag documentation property that will be understood by Ignition

    :param parent_metric: the Metric object to attach the property to
    :param value: the documentation of the metric

    """
    return MetricProperty(parent_metric, 'Documentation',
                          tahu.DataType.String, value,
                          False)

