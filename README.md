# Eclipse Tahu

Eclipse Tahu provide client libraries and reference implementations in various languages and for various devices
to show how the device/remote application must connect and disconnect from the MQTT server using the Sparkplug
specification explained below.  This includes device lifecycle messages such as the required birth and last will &
testament messages that must be sent to ensure the device lifecycle state and data integrity.

# Sparkplug

Sparkplug is a specification for MQTT enabled devices and applications to send and receive messages in a stateful way.
While MQTT is stateful by nature it doesn't ensure that all data on a receiving MQTT application is current or valid.
Sparkplug provides a mechanism for ensuring that remote device or application data is current and valid.

Sparkplug A was the original version of the Sparkplug specification and used Eclipse Kura's protobuf definition for
payload encoding.  However, it was quickly determined that this definition was too limited to handle the metadata that
typical Sparkplug payloads require.  As a result, Sparkplug B was developed to add additional features and capabilities
that were not possible in the original Kura payload definition.  These features include:
* Complex data types using templates
* Datasets
* Richer metrics with the ability to add property metadata for each metric
* Metric alias support to maintain rich metric naming while keeping bandwidth usage to a minimum
* Historical data
* File data

Sparkplug B Specification:
https://www.eclipse.org/tahu/spec/Sparkplug%20Topic%20Namespace%20and%20State%20ManagementV2.2-with%20appendix%20B%20format%20-%20Eclipse.pdf

Tutorials showing how to use this reference code can be found here:
https://docs.chariot.io/display/CLD79/Sparkplug+Developer+Docs
