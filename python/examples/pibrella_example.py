#!/usr/bin/env python3

#############################################################################
# Copyright (c) 2014, 2018, 2020, 2022 Cirrus Link Solutions and others
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

# We setup a basic logger for the top-level application, which indirectly
# enables logging output for the tahu modules
import logging
logging.basicConfig(level=logging.DEBUG)

import tahu
import tahu.edge

import pibrella
import time
import subprocess

### Commonly configured items
my_group_id = 'sfdev'
my_node_name = 'Python Raspberry Pi'
my_device_name = 'Pibrella'

# You can define multiple connection setups here, and the edge node will rotate through them
# in response to "Next Server" commands.
my_mqtt_params = [
    #tahu.mqtt_params('192.168.1.25', username='admin', password='changeme'),
    #tahu.mqtt_params('securehost', certfile='my_cert.pem', keyfile='my_private.key', tls_enabled=True),
    tahu.mqtt_params('broker.hivemq.com'),
]

# Some handlers for NCMD/DCMD messages from Sparkplug
def cmd_context_write_read(tag, context, value):
    logger.info('cmd_context_write_read tag={} context={} value={}'.format(tag.name,
                                                                           context,
                                                                           value))
    context.write(value)
    tag.change_value(context.read(), send_immediate=False)

def cmd_buzzer_fail(tag, context, value):
    logger.info('cmd_buzzer_fail tag={} context={} value={}'.format(tag.name,
                                                                    context,
                                                                    value))
    pibrella.buzzer.fail()
    tag.change_value(value, send_immediate=False)

def cmd_buzzer_success(tag, context, value):
    logger.info('cmd_buzzer_success tag={} context={} value={}'.format(tag.name,
                                                                       context,
                                                                       value))
    pibrella.buzzer.success()
    tag.change_value(value, send_immediate=False)

my_edge_node = tahu.edge.Node(my_mqtt_params, my_group_id,
                              my_node_name, u32_in_long=True)

# Find some interesting info about our system to report
uname_args = 'uname -a'.split()
uname_output = subprocess.check_output(uname_args)
uname_output.strip()
hardware_info = ''
revision_info = ''
serial_info = ''
with open('/proc/cpuinfo', 'r') as cpuinfo:
    for line in cpuinfo:
        if 'Hardware' in line:
            hardware_info = hardware_info + line
        elif 'Revision' in line:
            revision_info = revision_info + line
        elif 'Serial' in line:
            serial_info = serial_info + line
tahu.edge.Metric(my_edge_node, 'Parameters/sw_version',
                 tahu.DataType.String, value=uname_output)
tahu.edge.Metric(my_edge_node, 'Parameters/hw_version',
                 tahu.DataType.String, value=hardware_info)
tahu.edge.Metric(my_edge_node, 'Parameters/hw_revision',
                 tahu.DataType.String, value=revision_info)
tahu.edge.Metric(my_edge_node, 'Parameters/hw_serial',
                 tahu.DataType.String, value=serial_info)

# Map all the pibrealla ins and outs to metrics
# Save the metric references on inputs so we can use them in the pibrella event handlers
my_subdevice = tahu.edge.Device(my_edge_node, my_device_name)
in_a_metric = tahu.edge.Metric(my_subdevice, 'Inputs/a',
                               tahu.DataType.Boolean,
                               value=pibrella.input.a.read())
in_b_metric = tahu.edge.Metric(my_subdevice, 'Inputs/b',
                               tahu.DataType.Boolean,
                               value=pibrella.input.b.read())
in_c_metric = tahu.edge.Metric(my_subdevice, 'Inputs/c',
                               tahu.DataType.Boolean,
                               value=pibrella.input.c.read())
in_d_metric = tahu.edge.Metric(my_subdevice, 'Inputs/d',
                               tahu.DataType.Boolean,
                               value=pibrella.input.d.read())
button_metric = tahu.edge.Metric(my_subdevice, 'button',
                                 tahu.DataType.Boolean,
                                 value=pibrella.button.read())
# Setup cmd_handler and cmd_context on outputs to support CMD messages from host applications
tahu.edge.Metric(my_subdevice, 'Outputs/e', tahu.DataType.Boolean,
                 value=pibrella.output.e.read(),
                 cmd_handler=cmd_context_write_read,
                 cmd_context=pibrella.output.e)
tahu.edge.Metric(my_subdevice, 'Outputs/f', tahu.DataType.Boolean,
                 value=pibrella.output.f.read(),
                 cmd_handler=cmd_context_write_read,
                 cmd_context=pibrella.output.f)
tahu.edge.Metric(my_subdevice, 'Outputs/g', tahu.DataType.Boolean,
                 value=pibrella.output.g.read(),
                 cmd_handler=cmd_context_write_read,
                 cmd_context=pibrella.output.g)
tahu.edge.Metric(my_subdevice, 'Outputs/h', tahu.DataType.Boolean,
                 value=pibrella.output.h.read(),
                 cmd_handler=cmd_context_write_read,
                 cmd_context=pibrella.output.h)
tahu.edge.Metric(my_subdevice, 'Outputs/LEDs/green',
                 tahu.DataType.Boolean,
                 value=pibrella.light.green.read(),
                 cmd_handler=cmd_context_write_read,
                 cmd_context=pibrella.light.green)
tahu.edge.Metric(my_subdevice, 'Outputs/LEDs/red',
                 tahu.DataType.Boolean,
                 value=pibrella.light.red.read(),
                 cmd_handler=cmd_context_write_read,
                 cmd_context=pibrella.light.red)
tahu.edge.Metric(my_subdevice, 'Outputs/LEDs/yellow',
                 tahu.DataType.Boolean,
                 value=pibrella.light.yellow.read(),
                 cmd_handler=cmd_context_write_read,
                 cmd_context=pibrella.light.yellow)
tahu.edge.Metric(my_subdevice, 'buzzer_fail', tahu.DataType.Boolean,
                 value=False, cmd_handler=cmd_buzzer_fail)
tahu.edge.Metric(my_subdevice, 'buzzer_success',
                 tahu.DataType.Boolean, value=False,
                 cmd_handler=cmd_buzzer_success)

# Set up the pibrella input event handlers
pibrella.button.changed(lambda pin: button_metric.change_value(pin.read(),
                                                               send_immediate=False))
pibrella.input.a.changed(lambda pin: in_a_metric.change_value(pin.read(),
                                                              send_immediate=False))
pibrella.input.b.changed(lambda pin: in_b_metric.change_value(pin.read(),
                                                              send_immediate=False))
pibrella.input.c.changed(lambda pin: in_c_metric.change_value(pin.read(),
                                                              send_immediate=False))
pibrella.input.d.changed(lambda pin: in_d_metric.change_value(pin.read(),
                                                              send_immediate=False))

# Now that we've created all of our devices, metrics, and event handlers,
# we can connect the edge node to the broker.
my_edge_node.online()

while not my_edge_node.is_connected():
    # TODO - Add some sort of timeout feature?
    time.sleep(0.1)
    pass

while True:
    # Sit and wait for a moment...
    time.sleep(0.1)

    # Send any unsent changes
    my_subdevice.send_data(changed_only=True)
    my_edge_node.send_data(changed_only=True)

