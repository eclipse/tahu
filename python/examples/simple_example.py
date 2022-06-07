#!/usr/bin/env python3

#############################################################################
# Copyright (c) 2022 Justin Brzozoski
#
# This program and the accompanying materials are made available under the
# terms of the Eclipse Public License 2.0 which is available at
# http://www.eclipse.org/legal/epl-2.0.
#
# SPDX-License-Identifier: EPL-2.0
#
# Contributors:
#   Justin Brzozoski @ SignalFire Wireless Telemetry
#############################################################################

# This is just about the simplest SparkplugB edge node possible:
# It has NO subdevices and all custom metrics are directly on the edge node
# It has NO support setup to handle commands from the server on any custom metrics
# It just sends a loop counter and current system time to the server every 5 seconds

# However, the library handles all this:
# The library sets up and handles well-known metrics like "Rebirth" and "Next Server" for you, including server commands
# The library handles all BIRTH/DEATH messages for you
# The library handles all bdSeq, sequence, and other details for you
# The library tries to stay online and will automatically reconnect as needed

import tahu
import tahu.edge

# We use time to sleep in our main loop
import time

# You can run without a logger, but the edge node is quiet without it.
# Since we're okay with all nodes logging to the console and defining
# their own logging IDs, we only need to setup a basic config here.
# We don't need to hold on to any logger objects or pass them around.
import logging
logging.basicConfig(level=logging.INFO)

# Commonly configured items
# The combination of my_group_id and my_node_name uniquely identify this node to servers
my_group_id = 'Tahu Sample'
my_node_name = 'Simple Node 1'
# This is where you define MQTT connection parameters (hostname, username, password, TLS, etc)
my_mqtt_params = [ tahu.mqtt_params('broker.hivemq.com') ]

# Here is where we setup one edge node with two custom metrics
my_edge_node = tahu.edge.Node(my_mqtt_params, my_group_id,
                              my_node_name,
                              u32_in_long=True)
loop_count_tag = tahu.edge.Metric(my_edge_node, 'loop_count',
                                  datatype=tahu.DataType.UInt32,
                                  value=0)
sys_time_tag = tahu.edge.Metric(my_edge_node, 'sys_time',
                                datatype=tahu.DataType.DateTime,
                                value=tahu.timestamp_to_sparkplug())
# And that's it!

# Now that we've created all of our devices, metrics, and properties,
# we request the edge node connect to the broker:
# Note: This starts a new thread to handle all connectivity work...
my_edge_node.online()

print('Press Ctrl-C when you want to quit.')

# Wait until it is connected
while not my_edge_node.is_connected():
    # TODO - Add some sort of timeout feature?
    time.sleep(0.1)

loop_count = 0
while True:
    # Sit and wait for a moment...
    time.sleep(5)

    loop_count = loop_count + 1
    print(f'On loop {loop_count}')

    # Send the latest loop count and current system time
    loop_count_tag.change_value(loop_count)
    sys_time_tag.change_value(tahu.timestamp_to_sparkplug())

# The loop above will never quit, but if you wanted to close down or go offline gracefully,
# just call this function to shut down the connection and the connectivity worker thread.
my_edge_node.offline()

