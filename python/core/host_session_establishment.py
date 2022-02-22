"""*******************************************************************************
 * Copyright (c) 2021 Ian Craggs
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * and Eclipse Distribution License v1.0 which accompany this distribution. 
 *
 * The Eclipse Public License is available at 
 *    https://www.eclipse.org/legal/epl-2.0/
 * and the Eclipse Distribution License is available at 
 *   http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *    Ian Craggs - initial API and implementation and/or initial documentation
 *******************************************************************************"""


import paho.mqtt.client as mqtt
import time

"""


"""
broker = "localhost"
port = 1883
host_application_id = "HOSTAPPID"

def control_on_message(client, userdata, msg):
    if msg.topic == "SPARKPLUG_TCK/RESULT":
        print("*** Result ***",  msg.payload)

def control_on_connect(client, userdata, flags, rc):
    print("Control client connected with result code "+str(rc))
    # Subscribing in on_connect() means that if we lose the connection and
    # reconnect then subscriptions will be renewed.
    client.subscribe("SPARKPLUG_TCK/#")

def control_on_subscribe(client, userdata, mid, granted_qos):
    print("Control client subscribed")
    rc = client.publish("SPARKPLUG_TCK/TEST_CONTROL", "NEW host SessionEstablishment " + host_application_id, qos=1)

published = False
def control_on_publish(client, userdata, mid):
    print("Control client published")
    global published
    published = True

control_client = mqtt.Client("sparkplug_control")
control_client.on_connect = control_on_connect
control_client.on_subscribe = control_on_subscribe
control_client.on_publish = control_on_publish
control_client.on_message = control_on_message
control_client.connect(broker, port)
control_client.loop_start()

# wait for publish to complete
while published == False:
    time.sleep(0.1)

def test_on_connect(client, userdata, flags, rc):
    print("Test client connected with result code "+str(rc))
    client.subscribe("spAv1.0/#")

def test_on_subscribe(client, userdata, mid, granted_qos):
    print("Test client subscribed")
    client.publish("STATE/"+host_application_id, "ONLINE", qos=1)

published = False
def test_on_publish(client, userdata, mid):
    print("Test client published")
    global published
    published = True

client = mqtt.Client("clientid", clean_session=True)
client.on_connect = test_on_connect
client.on_subscribe = test_on_subscribe
client.on_publish = test_on_publish
client.will_set(topic="STATE/"+host_application_id, payload="OFFLINE", qos=1, retain=True)
client.connect(broker, port)
client.loop_start()

while published == False:
    time.sleep(0.1)

client.loop_stop()

published = False
control_client.publish("SPARKPLUG_TCK/TEST_CONTROL", "END TEST")
while published == False:
    time.sleep(0.1)

control_client.loop_stop()




