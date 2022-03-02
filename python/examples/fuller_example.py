#!/usr/bin/env python3

#############################################################################
# Copyright (c) 2014, 2018, 2020 Cirrus Link Solutions and others
#
# This program and the accompanying materials are made available under the
# terms of the Eclipse Public License 2.0 which is available at
# http://www.eclipse.org/legal/epl-2.0.
#
# SPDX-License-Identifier: EPL-2.0
#
# Contributors:
#   Cirrus Link Solutions - initial implementation
#   Justin Brzozoski @ SignalFire Wireless Telemetry - major rewrite
#############################################################################

# These are the basic imports required for a Sparkplug Edge Node.
# The logging import is not explicitly necessary, but is highly recommended.
import logging
logging.basicConfig(level=logging.DEBUG)
logger = logging.getLogger('edge_node_example')
logger.info('Starting Python Sparkplug edge node demonstration')

import time
from datetime import datetime, timezone
import random
import string

import tahu
import tahu.edge

### Commonly configured items
my_group_id = "Tahu Sample"
my_node_name = "Edge Node 1"
my_device_name = "Emulated Device"
# You can define multiple connection setups here, and the edge node will rotate through them
# in response to "Next Server" commands.
my_mqtt_params = [
    #tahu.mqtt_params('localhost', username='admin', password='changeme'),
    #tahu.mqtt_params('securehost', certfile='my_cert.pem', keyfile='my_private.key', tls_enabled=True),
    #tahu.mqtt_params('test.mosquitto.org'),
    tahu.mqtt_params('broker.hivemq.com'),
]

def sample_cmd_handler(tag, context, value):
    """
    Simplest example of a callback for a metric cmd_handler

    This cmd_handler will log the received info and then echo the new value back over Sparkplug

    :param tag: Metric object that this command was received on
    :param context: the optional cmd_context object provided to the Metric when it was created
    :param value: the new value received over Sparkplug

    """
    logger.info('sample_cmd_handler tag={} context={} value={}'.format(tag.name,
                                                                       context,
                                                                       value))
    tag.change_value(value)

def fancier_date_handler(tag, context, value):
    """
    A simple example of a callback for a datetime-based metric cmd_handler

    This cmd_handler will log the received time and then echo back the current time over Sparkplug

    :param tag: Metric object that this command was received on
    :param context: the optional cmd_context object provided to the Metric when it was created
    :param value: the new value received over Sparkplug

    """
    dt = datetime.fromtimestamp(tahu.timestamp_from_sparkplug(value),
                                timezone.utc)
    logger.info('fancier_date_handler received {}'.format(str(dt)))
    tag.change_value(tahu.timestamp_to_sparkplug())

# There is a discrepancy between Ignition as of version 8.1.x (or older) and the Sparkplug spec as of version 2.2.
# The spec says in section 15.2.1 that UInt32 should be stored in the int_value field of the protobuf,
# but Ignition and the reference code have historically stored UInt32 in the long_value field.
#
# Our library is flexible and will accept incoming values from either value field gracefully.
# However, outgoing UInt32 can only be done in one or the other.
#
# When you first setup your node, you can pass a u32_in_long parameter to control this behavior for a
# edge node and all devices under it.
# Setting it to True will work in Ignition's style, setting it to False will match the spec's style.
my_edge_node = tahu.edge.Node(my_mqtt_params, my_group_id,
                              my_node_name,
                              logger=logger, u32_in_long=True)
my_subdevice = tahu.edge.Device(my_edge_node, my_device_name)

# Here are examples of how to define one of each of the basic types.
# The value you pass in when creating the metric just sets the initial value.
# Hold onto the return object to be able to adjust the value later.
s8_test_tag = tahu.edge.Metric(my_subdevice, 'int8_test',
                               tahu.DataType.Int8, value=-1,
                               cmd_handler=sample_cmd_handler)
s16_test_tag = tahu.edge.Metric(my_subdevice, 'int16_test',
                                tahu.DataType.Int16, value=-1,
                                cmd_handler=sample_cmd_handler)
s32_test_tag = tahu.edge.Metric(my_subdevice, 'int32_test',
                                tahu.DataType.Int32, value=-1,
                                cmd_handler=sample_cmd_handler)
s64_test_tag = tahu.edge.Metric(my_subdevice, 'int64_test',
                                tahu.DataType.Int64, value=-1,
                                cmd_handler=sample_cmd_handler)
u8_test_tag = tahu.edge.Metric(my_subdevice, 'uint8_test',
                               tahu.DataType.UInt8, value=1,
                               cmd_handler=sample_cmd_handler)
u16_test_tag = tahu.edge.Metric(my_subdevice, 'uint16_test',
                                tahu.DataType.UInt16, value=1,
                                cmd_handler=sample_cmd_handler)
u32_test_tag = tahu.edge.Metric(my_subdevice, 'uint32_test',
                                tahu.DataType.UInt32, value=1,
                                cmd_handler=sample_cmd_handler)
u64_test_tag = tahu.edge.Metric(my_subdevice, 'uint64_test',
                                tahu.DataType.UInt64, value=1,
                                cmd_handler=sample_cmd_handler)
float_test_tag = tahu.edge.Metric(my_subdevice, 'float_test',
                                  tahu.DataType.Float, value=1.01,
                                  cmd_handler=sample_cmd_handler)
double_test_tag = tahu.edge.Metric(my_subdevice, 'double_test',
                                   tahu.DataType.Double,
                                   value=1.02,
                                   cmd_handler=sample_cmd_handler)
boolean_test_tag = tahu.edge.Metric(my_subdevice, 'boolean_test',
                                    tahu.DataType.Boolean,
                                    value=True,
                                    cmd_handler=sample_cmd_handler)
string_test_tag = tahu.edge.Metric(my_subdevice, 'string_test',
                                   tahu.DataType.String,
                                   value="Hello, world!",
                                   cmd_handler=sample_cmd_handler)
datetime_test_tag = tahu.edge.Metric(my_subdevice, 'datetime_test',
                                     tahu.DataType.DateTime,
                                     value=tahu.timestamp_to_sparkplug(),
                                     cmd_handler=fancier_date_handler)
# If you want the current time use tahu.timestamp_to_sparkplug() without parameters.
# If you want to convert from a datetime, pass in the datetime.timestamp like this:
#  sample_datetime = datetime(2006, 11, 21, 16, 30, tzinfo=timezone.utc)
#  alternative_time_value = tahu.timestamp_to_sparkplug(sample_datetime.timestamp())

# Here are examples of how to use properties.
# Properties are attached to a metric after creating it.
# You can define them one at a time with detailed control using MetricProperty.
# If you don't need as much control over datatypes, you can define a group all at once using bulk_properties.
# And there are ignition_x_property functions for adding well-known properties that Ignition looks for.
# If you have a property that you need to adjust later, hold onto the return object when you create it.
property_test_tag = tahu.edge.Metric(my_subdevice, 'property_test',
                                     tahu.DataType.UInt64,
                                     value=23,
                                     cmd_handler=sample_cmd_handler)
tahu.edge.MetricProperty(property_test_tag, 'prop_name',
                         tahu.DataType.UInt64, value=5,
                         report_with_data=False)
tahu.edge.bulk_properties(property_test_tag, {'dictstr':'whatever',
                                              'dictdouble':3.14159,
                                              'dictint64':64738})
tahu.edge.ignition_documentation_property(property_test_tag,
                                          'A tag for demonstrating lots of property samples!')
tahu.edge.ignition_low_property(property_test_tag, 0)
tahu.edge.ignition_high_property(property_test_tag, 10)
tahu.edge.ignition_unit_property(property_test_tag, 'smoots')
property_test_tag_quality = tahu.edge.ignition_quality_property(property_test_tag)

# Here's an example of a dataset tag.
# Locally, they are handled as tahu.DataSet objects.
# To create a dataset, you first pass in a dict listing column names and datatypes.
# You can then manipulate the data with add_rows, get_rows, remove_rows and other methods.
# After that, the dataset object is passed into the metric value as normal.
sample_dataset = tahu.DataSet({'U32Col':tahu.DataType.UInt32,
                               'StrCol':tahu.DataType.String,
                               'DoubleCol':tahu.DataType.Double})
sample_dataset.add_rows([[15, 'Fifteen', 3.14159], [0, 'Zero', 6.07E27],
                         [65535, 'FunFunFun', (2 / 3)]])
dataset_test_tag = tahu.edge.Metric(my_subdevice, 'dataset_sample',
                                    tahu.DataType.DataSet,
                                    value=sample_dataset,
                                    cmd_handler=sample_cmd_handler)

# Now that we've created all of our devices, metrics, and properties,
# we can connect the edge node to the broker.
my_edge_node.online()
while not my_edge_node.is_connected():
    # TODO - Add some sort of timeout feature?
    pass
loop_count = 0
while True:
    # Sit and wait for a moment...
    time.sleep(5)

    # Send some random data on the string_test tag right away... (triggers an immediate data message)
    new_string = ''.join(random.sample(string.ascii_lowercase, 12))
    string_test_tag.change_value(new_string)

    # Next, pile up a few changes all on the same subdevice, and trigger a collected
    # data message containing all of those manually.  (Will not work for tags on different subdevices)

    # Randomly change the quality on the property_test_tag...
    new_quality = random.choice([tahu.edge.IgnitionQualityCode.Good,
                                 tahu.edge.IgnitionQualityCode.Error_IO])
    property_test_tag_quality.change_value(new_quality, send_immediate=False)

    # Report how many times we've gone around this loop in the uint8
    u8_test_tag.change_value(loop_count, send_immediate=False)

    # Send any unsent changes
    my_subdevice.send_data(changed_only=True)
    my_edge_node.send_data(changed_only=True)

    loop_count = loop_count + 1

