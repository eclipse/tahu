/********************************************************************************
 * Copyright (c) 2014, 2018 Cirrus Link Solutions and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Cirrus Link Solutions - initial implementation
 ********************************************************************************/

package org.eclipse.tahu;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Date;
import java.util.Enumeration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.tahu.message.SparkplugBPayloadDecoder;
import org.eclipse.tahu.message.SparkplugBPayloadEncoder;
import org.eclipse.tahu.message.model.Metric;
import org.eclipse.tahu.message.model.MetricDataType;
import org.eclipse.tahu.message.model.SparkplugBPayload;
import org.eclipse.tahu.message.model.Metric.MetricBuilder;
import org.eclipse.tahu.message.model.SparkplugBPayload.SparkplugBPayloadBuilder;

import com.pi4j.component.button.ButtonState;
import com.pi4j.component.button.ButtonStateChangeEvent;
import com.pi4j.component.button.ButtonStateChangeListener;
import com.pi4j.device.pibrella.Pibrella;
import com.pi4j.device.pibrella.PibrellaInput;
import com.pi4j.device.pibrella.PibrellaOutput;
import com.pi4j.device.pibrella.impl.PibrellaDevice;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.system.SystemInfo;

/**
 * An example Sparkplug B application.
 */
public class SparkplugRaspberryPiExample implements MqttCallbackExtended {

	private static final Pibrella pibrella = new PibrellaDevice();

	private static final String NAMESPACE = "spBv1.0";

	// Configuration
	private static final boolean USING_REAL_TLS = false;
	private String serverUrl = "tcp://192.168.1.53:1883"; // Change to point to your MQTT Server
	private String groupId = "Sparkplug B Devices";
	private String edgeNode = "Java Raspberry Pi Example";
	private String deviceId = "Pibrella";
	private String clientId = "SparkplugRaspberryPiExampleEdgeNode";
	private String username = "admin";
	private String password = "changeme";
	private ExecutorService executor;
	private MqttClient client;

	// Some control and parameter points for this demo
	private int configChangeCount = 1;
	private int scanRateMs = 1000;
	private long upTimeStart = System.currentTimeMillis();
	private int buttonCounter = 0;
	private int buttonCounterSetpoint = 10;

	private long bdSeq = 0;
	private long seq = 0;

	private Object lock = new Object();
	
	public static void main(String[] args) {
		SparkplugRaspberryPiExample example = new SparkplugRaspberryPiExample();
		example.run();
	}
	
	public void run() {
		try {
			// Create the Raspberry Pi Pibrella board listeners
			createPibrellaListeners();

			// Thread pool for outgoing published messages
			executor = Executors.newFixedThreadPool(1);
			
			// Establish the session with autoreconnect = true;
			establishMqttSession();

			// Wait for 'ctrl c' to exit
			while (true) {
				//
				// This is a very simple loop for the demo that keeps the MQTT Session 
				// up, and publishes the Up Time metric based on the current value of
				// the scanRateMs process variable.
				//
				if (client.isConnected()) {
					synchronized (lock) {
						SparkplugBPayload payload = new SparkplugBPayloadBuilder(getNextSeqNum())
								.setTimestamp(new Date())
								.addMetric(new MetricBuilder("Up Time ms",
										MetricDataType.Int64,
										System.currentTimeMillis() - upTimeStart)
										.createMetric())
								.createPayload();

						// Publish current Up Time
						executor.execute(new Publisher(NAMESPACE + "/" + groupId + "/NDATA/" + edgeNode, payload));
					}
				} else {
					System.out.println("Connection is not established - not sending data");
				}
				Thread.sleep(scanRateMs);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Establish an MQTT Session with Sparkplug defined Death Certificate. It may not be
	 * Immediately intuitive that the Death Certificate is created prior to publishing the
	 * Birth Certificate, but the Death Certificate is actually part of the MQTT Session 
	 * establishment. For complete details of the actual MQTT wire protocol refer to the 
	 * latest OASyS MQTT V3.1.1 standards at:
	 * http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/mqtt-v3.1.1.html
	 * 
	 * @return true = MQTT Session Established
	 */
	public boolean establishMqttSession() {
		try {

			//
			// Setup the MQTT connection parameters using the Paho MQTT Client.
			//
			MqttConnectOptions options = new MqttConnectOptions();
			
			if (USING_REAL_TLS) {
				SocketFactory sf = SSLSocketFactory.getDefault();
				options.setSocketFactory(sf);
			}
			
			// Autoreconnect enable
			options.setAutomaticReconnect(true);
			// MQTT session parameters Clean Start = true
			options.setCleanSession(true);
			// Session connection attempt timeout period in seconds
			options.setConnectionTimeout(10);
			// MQTT session parameter Keep Alive Period in Seconds
			options.setKeepAliveInterval(30);
			// MQTT Client Username
			options.setUserName(username);
			// MQTT Client Password
			options.setPassword(password.toCharArray());
			//
			// Build up the Death Certificate MQTT Payload. Note that the Death
			// Certificate payload sequence number
			// is not tied to the normal message sequence numbers.
			//
			SparkplugBPayload payload = new SparkplugBPayloadBuilder(getNextSeqNum())
					.setTimestamp(new Date())
					.addMetric(new MetricBuilder("bdSeq",
							MetricDataType.Int64,
							bdSeq)
							.createMetric())
					.createPayload();
			byte[] bytes = new SparkplugBPayloadEncoder().getBytes(payload);
			//
			// Setup the Death Certificate Topic/Payload into the MQTT session
			// parameters
			//
			options.setWill(NAMESPACE + "/" + groupId + "/NDEATH/" + edgeNode, bytes, 0, false);

			//
			// Create a new Paho MQTT Client
			//
			client = new MqttClient(serverUrl, clientId);
			//
			// Using the parameters set above, try to connect to the define MQTT
			// server now.
			//
			System.out.println("Trying to establish an MQTT Session to the MQTT Server @ :" + serverUrl);
			client.connect(options);
			System.out.println("MQTT Session Established");
			client.setCallback(this);
			//
			// With a successful MQTT Session in place, now issue subscriptions
			// for the EoN Node and Device "Command" Topics of 'NCMD' and 'DCMD'
			// defined in Sparkplug
			//
			client.subscribe(NAMESPACE + "/" + groupId + "/NCMD/" + edgeNode + "/#", 0);
			client.subscribe(NAMESPACE + "/" + groupId + "/DCMD/" + edgeNode + "/#", 0);
		} catch (Exception e) {
			System.out.println("Error Establishing an MQTT Session:");
			e.printStackTrace();
			return false;
		}
		return true;
	}

	
	/**
	 * Publish the EoN Node Birth Certificate and the Device Birth Certificate
	 * per the Sparkplug Specification
	 */
	public void publishBirth() {
		try {
			synchronized (lock) {
				
				// Since this is a birth - reset the seq number
				// Note that message sequence numbers will appear in
				// the "Node Metrics" folder in Ignition.
				seq = 0;

				//
				// Create the NBIRTH Certificate per the Sparkplug
				// specification
				//

				//
				// Create the EoN Node BIRTH payload with any number of
				// read/write properties for this node. These parameters will
				// appear in
				// folders under this Node in the Ignition tag structure.
				//
				SparkplugBPayloadBuilder payloadBuilder = new SparkplugBPayloadBuilder(getNextSeqNum())
						.setTimestamp(new Date())
						.addMetric(new MetricBuilder("bdSeq",
								MetricDataType.Int64,
								bdSeq)
								.createMetric())
						.addMetric(new MetricBuilder("Up Time ms",
								MetricDataType.Int64,
								System.currentTimeMillis() - upTimeStart)
								.createMetric())
						.addMetric(new MetricBuilder("Node Control/Next Server",
								MetricDataType.Boolean,
								false)
								.createMetric())
						.addMetric(new MetricBuilder("Node Control/Rebirth",
								MetricDataType.Boolean,
								false)
								.createMetric())
						.addMetric(new MetricBuilder("Node Control/Reboot",
								MetricDataType.Boolean,
								false)
								.createMetric())
						.addMetric(new MetricBuilder("Node Control/Scan Rate ms",
								MetricDataType.Int32,
								scanRateMs)
								.createMetric())
						.addMetric(new MetricBuilder("Properties/Board Type",
								MetricDataType.String,
								SystemInfo.getBoardType().toString())
								.createMetric())
						.addMetric(new MetricBuilder("Properties/Hardware",
								MetricDataType.String,
								SystemInfo.getHardware())
								.createMetric())
						.addMetric(new MetricBuilder("Properties/OS FW Build",
								MetricDataType.String,
								SystemInfo.getOsFirmwareBuild())
								.createMetric())
						.addMetric(new MetricBuilder("Config Change Count",
								MetricDataType.Int32,
								configChangeCount)
								.createMetric());
				
				// Increment the bdSeq number for the next use
				incrementBdSeqNum();
								
				try {
					// Add the Raspberry Pi's real network addresses
					Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
					while (e.hasMoreElements()) {
					    NetworkInterface n = (NetworkInterface) e.nextElement();
					    Enumeration<InetAddress> ee = n.getInetAddresses();
					    while (ee.hasMoreElements()) {
					        InetAddress i = (InetAddress) ee.nextElement();
					        if (i instanceof Inet4Address) {
					        	payloadBuilder.addMetric(
					        			new MetricBuilder("Properties/IP Addresses/" + n.getName() + "/" + "IPV4",
					        								MetricDataType.String,
					        								i.getHostAddress()).createMetric());
					        } else if (i instanceof Inet6Address) {
					        	payloadBuilder.addMetric(
					        			new MetricBuilder("Properties/IP Addresses/" + n.getName() + "/" + "IPV6",
					        					MetricDataType.String,
					        					i.getHostAddress().substring(0, i.getHostAddress().indexOf("%")))
					        				.createMetric());
					        }
					    }
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				//
				// Now publish the EoN Node Birth Certificate.
				// Note that the required "Sequence Number" metric 'seq' needs
				// to
				// be RESET TO A VALUE OF ZERO for the message. The 'timestamp'
				// metric
				// is added into the payload by the Publisher() thread.
				//
				executor.execute(new Publisher(NAMESPACE + "/" + groupId + "/NBIRTH/" + edgeNode, payloadBuilder.createPayload()));

				//
				// Create the Device BIRTH Certificate now. The tags defined
				// here will appear in a
				// folder hierarchy under the associated Device.
				//
				SparkplugBPayload payload = new SparkplugBPayloadBuilder(getNextSeqNum())
						.setTimestamp(new Date())
						// Create an "Inputs" folder of process variables
						.addMetric(new MetricBuilder("Inputs/a",
								MetricDataType.Boolean,
								pibrella.getInputPin(PibrellaInput.A).isHigh())
								.createMetric())
						.addMetric(new MetricBuilder("Inputs/b",
								MetricDataType.Boolean,
								pibrella.getInputPin(PibrellaInput.B).isHigh())
								.createMetric())
						.addMetric(new MetricBuilder("Inputs/c",
								MetricDataType.Boolean,
								pibrella.getInputPin(PibrellaInput.C).isHigh())
								.createMetric())
						.addMetric(new MetricBuilder("Inputs/d",
								MetricDataType.Boolean,
								pibrella.getInputPin(PibrellaInput.D).isHigh())
								.createMetric())
						// Create an "Outputs" folder of process variables
						.addMetric(new MetricBuilder("Outputs/e",
								MetricDataType.Boolean,
								pibrella.getOutputPin(PibrellaOutput.E).isHigh())
								.createMetric())
						.addMetric(new MetricBuilder("Outputs/f",
								MetricDataType.Boolean,
								pibrella.getOutputPin(PibrellaOutput.F).isHigh())
								.createMetric())
						.addMetric(new MetricBuilder("Outputs/g",
								MetricDataType.Boolean,
								pibrella.getOutputPin(PibrellaOutput.G).isHigh())
								.createMetric())
						.addMetric(new MetricBuilder("Outputs/h",
								MetricDataType.Boolean,
								pibrella.getOutputPin(PibrellaOutput.H).isHigh())
								.createMetric())
						// Create an additional folder under "Outputs" called "LEDs"
						.addMetric(new MetricBuilder("Outputs/LEDs/green",
								MetricDataType.Boolean,
								pibrella.getOutputPin(PibrellaOutput.LED_GREEN).isHigh())
								.createMetric())
						.addMetric(new MetricBuilder("Outputs/LEDs/red",
								MetricDataType.Boolean,
								pibrella.getOutputPin(PibrellaOutput.LED_RED).isHigh())
								.createMetric())
						.addMetric(new MetricBuilder("Outputs/LEDs/yellow",
								MetricDataType.Boolean,
								pibrella.getOutputPin(PibrellaOutput.LED_YELLOW).isHigh())
								.createMetric())
						// Place the button process variables at the root level of the
						// tag hierarchy
						.addMetric(new MetricBuilder("button",
								MetricDataType.Boolean,
								pibrella.getInputPin(PibrellaInput.Button).isHigh())
								.createMetric())
						.addMetric(new MetricBuilder("button count",
								MetricDataType.Int32,
								buttonCounter)
								.createMetric())
						.addMetric(new MetricBuilder("button count setpoint",
								MetricDataType.Int32,
								buttonCounterSetpoint)
								.createMetric())
						.addMetric(new MetricBuilder("buzzer",
								MetricDataType.Boolean,
								false)
								.createMetric())
						.createPayload();

				// Publish the Device BIRTH Certificate now
				executor.execute(new Publisher(NAMESPACE + "/" + groupId + "/DBIRTH/" + edgeNode + "/" + deviceId, payload));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Used to get the sequence number
	private void incrementBdSeqNum() throws Exception {
		if (bdSeq == 256) {
			bdSeq = 0;
		} else {
			bdSeq++;
		}
	}

	// Used to get the sequence number
	private long getNextSeqNum() throws Exception {
		long retSeq = seq;
		if (seq == 256) {
			seq = 0;
		} else {
			seq++;
		}
		
		return retSeq;
	}
	
	@Override
	public void connectComplete(boolean reconnect, String serverURI) {
		System.out.println("Connected! - publishing birth");
		publishBirth();
	}

	public void connectionLost(Throwable cause) {
		System.out.println("The MQTT Connection was lost!");
	}

	/**
	 * Based on our subscriptions to the MQTT Server, the messageArrived() callback is
	 * called on all arriving MQTT messages. Based on the Sparkplug Topic Namespace, 
	 * each message is parsed and an appropriate action is taken.
	 * 
	 */
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		System.out.println("Message Arrived on topic " + topic);

		// Initialize the outbound payload if required.
		SparkplugBPayloadBuilder outboundPayloadBuilder = new SparkplugBPayloadBuilder(getNextSeqNum())
				.setTimestamp(new Date());

		String[] splitTopic = topic.split("/");
		if (splitTopic[0].equals(NAMESPACE) && splitTopic[1].equals(groupId) && splitTopic[2].equals("NCMD")
				&& splitTopic[3].equals(edgeNode)) {
			
			SparkplugBPayload inboundPayload = new SparkplugBPayloadDecoder().buildFromByteArray(message.getPayload());

			for (Metric metric : inboundPayload.getMetrics()) {
				System.out.println("Metric: " + metric.getName() + " :: " + metric.getValue());
				
				if (metric.getName().equals("Node Control/Next Server")) {
					System.out.println("Received a Next Server command.");
				} else if (metric.getName().equals("Node Control/Rebirth")) {
					publishBirth();
				} else if (metric.getName().equals("Node Control/Reboot")) {
					System.out.println("Received a Reboot command.");
				} else if (metric.getName().equals("Node Control/Scan Rate ms")) {
					scanRateMs = (Integer) metric.getValue();
					if (scanRateMs < 100) {
						// Limit Scan Rate to a minimum of 100ms
						scanRateMs = 100;
					}
					outboundPayloadBuilder.addMetric(new MetricBuilder("Node Control/Scan Rate ms",
											MetricDataType.Int32,
											scanRateMs).createMetric());
					
					// Publish the message in a new thread
					synchronized (lock) {
						executor.execute(new Publisher(NAMESPACE + "/" + groupId + "/NDATA/" + edgeNode, 
												outboundPayloadBuilder.createPayload()));
					}
				}
			}
		} else if (splitTopic[0].equals(NAMESPACE) && splitTopic[1].equals(groupId) && splitTopic[2].equals("DCMD")
				&& splitTopic[3].equals(edgeNode)) {
			synchronized (lock) {
				System.out.println("Command recevied for device: " + splitTopic[4] + " on topic: " + topic);

				// Get the incoming metric key and value
				SparkplugBPayload inboundPayload = new SparkplugBPayloadDecoder().buildFromByteArray(message.getPayload());

				for (Metric metric : inboundPayload.getMetrics()) {
					System.out.println("Metric: " + metric.getName() + " :: " + metric.getValue());
					
					if (metric.getName().equals("Outputs/e")) {
						pibrella.getOutputPin(PibrellaOutput.E).setState((Boolean) metric.getValue());
						outboundPayloadBuilder.addMetric(new MetricBuilder("Outputs/e", MetricDataType.Boolean, 
													pibrella.getOutputPin(PibrellaOutput.E).isHigh()).createMetric());
					} else if (metric.getName().equals("Outputs/f")) {
						pibrella.getOutputPin(PibrellaOutput.F).setState((Boolean) metric.getValue());
						outboundPayloadBuilder.addMetric(new MetricBuilder("Outputs/f", MetricDataType.Boolean, 
													pibrella.getOutputPin(PibrellaOutput.F).isHigh()).createMetric());
					} else if (metric.getName().equals("Outputs/g")) {
						pibrella.getOutputPin(PibrellaOutput.G).setState((Boolean) metric.getValue());
						outboundPayloadBuilder.addMetric(new MetricBuilder("Outputs/g", MetricDataType.Boolean, 
													pibrella.getOutputPin(PibrellaOutput.G).isHigh()).createMetric());
					} else if (metric.getName().equals("Outputs/h")) {
						pibrella.getOutputPin(PibrellaOutput.H).setState((Boolean) metric.getValue());
						outboundPayloadBuilder.addMetric(new MetricBuilder("Outputs/h", MetricDataType.Boolean, 
													pibrella.getOutputPin(PibrellaOutput.H).isHigh()).createMetric());
					} else if (metric.getName().equals("Outputs/LEDs/green")) {
						if (((Boolean) metric.getValue()) == true) {
							pibrella.ledGreen().on();
						} else {
							pibrella.ledGreen().off();
						}
						outboundPayloadBuilder.addMetric(new MetricBuilder("Outputs/LEDs/green", MetricDataType.Boolean, 
													pibrella.ledGreen().isOn()).createMetric());
					} else if (metric.getName().equals("Outputs/LEDs/red")) {
						if (((Boolean) metric.getValue()) == true) {
							pibrella.ledRed().on();
						} else {
							pibrella.ledRed().off();
						}
						outboundPayloadBuilder.addMetric(new MetricBuilder("Outputs/LEDs/red", MetricDataType.Boolean, 
													pibrella.ledRed().isOn()).createMetric());
					} else if (metric.getName().equals("Outputs/LEDs/yellow")) {
						if (((Boolean) metric.getValue()) == true) {
							pibrella.ledYellow().on();
						} else {
							pibrella.ledYellow().off();
						}
						outboundPayloadBuilder.addMetric(new MetricBuilder("Outputs/LEDs/yellow", 
												MetricDataType.Boolean, pibrella.ledYellow().isOn()).createMetric());
					} else if (metric.getName().equals("button count setpoint")) {
						buttonCounterSetpoint = (Integer) metric.getValue();
						outboundPayloadBuilder.addMetric(new MetricBuilder("button count setpoint", 
								MetricDataType.Int32, buttonCounterSetpoint).createMetric());
					} else if (metric.getName().equals("buzzer")) {
						pibrella.getBuzzer().buzz(100, 2000);
					} else {
						System.out.println("Received unknown command for metric: " + metric.getName());
					}						
				}

				// Publish the message in a new thread
				executor.execute(
						new Publisher(NAMESPACE + "/" + groupId + "/DDATA/" + edgeNode + "/" + deviceId, 
								outboundPayloadBuilder.createPayload()));
			}
		}
	}

	public void deliveryComplete(IMqttDeliveryToken token) {
		// System.out.println("Published message: " + token);
	}

	private class Publisher implements Runnable {

		private String topic;
		private SparkplugBPayload payload;

		public Publisher(String topic, SparkplugBPayload payload) {
			this.topic = topic;
			this.payload = payload;
		}

		public void run() {
			try {
				byte[] bytes = new SparkplugBPayloadEncoder().getBytes(payload);
				client.publish(topic, bytes, 0, false);
			} catch (MqttPersistenceException e) {
				e.printStackTrace();
			} catch (MqttException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void createPibrellaListeners() {
		pibrella.button().addListener(new ButtonStateChangeListener() {
			public void onStateChange(ButtonStateChangeEvent event) {
				try {
					synchronized (lock) {
						SparkplugBPayloadBuilder outboundPayloadBuilder = new SparkplugBPayloadBuilder(getNextSeqNum())
								.setTimestamp(new Date());

						
						
						if (event.getButton().getState() == ButtonState.PRESSED) {
							outboundPayloadBuilder.addMetric(new MetricBuilder("button", MetricDataType.Boolean,
									true).createMetric());
							buttonCounter++;
							if (buttonCounter > buttonCounterSetpoint) {
								buttonCounter = 0;
							}
							outboundPayloadBuilder.addMetric(new MetricBuilder("button count", MetricDataType.Int32,
									buttonCounter).createMetric());
						} else {
							outboundPayloadBuilder.addMetric(new MetricBuilder("button", MetricDataType.Boolean,
									false).createMetric());
						}
						byte[] bytes = new SparkplugBPayloadEncoder().getBytes(outboundPayloadBuilder.createPayload());
						client.publish(NAMESPACE + "/" + groupId + "/DDATA/" + edgeNode + "/" + deviceId, bytes,
								0, false);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		pibrella.inputA().addListener(new GpioPinListenerDigital() {
			public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
				try {
					synchronized (lock) {
						SparkplugBPayloadBuilder outboundPayloadBuilder = new SparkplugBPayloadBuilder(getNextSeqNum())
								.setTimestamp(new Date());
						if (event.getState() == PinState.HIGH) {
							outboundPayloadBuilder.addMetric(new MetricBuilder("Inputs/a", MetricDataType.Boolean,
									true).createMetric());
						} else {
							outboundPayloadBuilder.addMetric(new MetricBuilder("Inputs/a", MetricDataType.Boolean,
									false).createMetric());
						}
						byte[] bytes = new SparkplugBPayloadEncoder().getBytes(outboundPayloadBuilder.createPayload());
						client.publish(NAMESPACE + "/" + groupId + "/DDATA/" + edgeNode + "/" + deviceId, 
								bytes, 0, false);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		pibrella.inputB().addListener(new GpioPinListenerDigital() {
			public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
				try {
					synchronized (lock) {
						SparkplugBPayloadBuilder outboundPayloadBuilder = new SparkplugBPayloadBuilder(getNextSeqNum())
								.setTimestamp(new Date());
						if (event.getState() == PinState.HIGH) {
							outboundPayloadBuilder.addMetric(new MetricBuilder("Inputs/b", MetricDataType.Boolean,
									true).createMetric());
						} else {
							outboundPayloadBuilder.addMetric(new MetricBuilder("Inputs/b", MetricDataType.Boolean,
									false).createMetric());
						}
						byte[] bytes = new SparkplugBPayloadEncoder().getBytes(outboundPayloadBuilder.createPayload());
						client.publish(NAMESPACE + "/" + groupId + "/DDATA/" + edgeNode + "/" + deviceId, 
								bytes, 0, false);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		pibrella.inputC().addListener(new GpioPinListenerDigital() {
			public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
				try {
					synchronized (lock) {
						SparkplugBPayloadBuilder outboundPayloadBuilder = new SparkplugBPayloadBuilder(getNextSeqNum())
								.setTimestamp(new Date());
						if (event.getState() == PinState.HIGH) {
							outboundPayloadBuilder.addMetric(new MetricBuilder("Inputs/c", MetricDataType.Boolean,
									true).createMetric());
						} else {
							outboundPayloadBuilder.addMetric(new MetricBuilder("Inputs/c", MetricDataType.Boolean,
									false).createMetric());
						}
						byte[] bytes = new SparkplugBPayloadEncoder().getBytes(outboundPayloadBuilder.createPayload());
						client.publish(NAMESPACE + "/" + groupId + "/DDATA/" + edgeNode + "/" + deviceId, 
								bytes, 0, false);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		pibrella.inputD().addListener(new GpioPinListenerDigital() {
			public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
				try {
					synchronized (lock) {
						SparkplugBPayloadBuilder outboundPayloadBuilder = new SparkplugBPayloadBuilder(getNextSeqNum())
								.setTimestamp(new Date());
						if (event.getState() == PinState.HIGH) {
							outboundPayloadBuilder.addMetric(new MetricBuilder("Inputs/d", MetricDataType.Boolean,
									true).createMetric());
						} else {
							outboundPayloadBuilder.addMetric(new MetricBuilder("Inputs/d", MetricDataType.Boolean,
									false).createMetric());
						}
						byte[] bytes = new SparkplugBPayloadEncoder().getBytes(outboundPayloadBuilder.createPayload());
						client.publish(NAMESPACE + "/" + groupId + "/DDATA/" + edgeNode + "/" + deviceId, 
								bytes, 0, false);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
