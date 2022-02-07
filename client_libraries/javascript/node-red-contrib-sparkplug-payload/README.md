node-red-contrib-sparkplug-payload
=========

A node that provides tools for encoding and decoding payload objects and 
strings using the [Sparkplug Google Protocol Buffer Schema](https://www.eclipse.org/tahu/spec/Sparkplug%20Topic%20Namespace%20and%20State%20ManagementV2.2-with%20appendix%20B%20format%20-%20Eclipse.pdf).

This node is designed to facilitate the creation of your own Sparkplug nodes in
 node red for adding data consumers for dashboarding or for implementing your 
 own Sparkplug client using the built in MQTT node.

## Installation

  npm install node-red-contrib-sparkplug-payload

## Usage

Simply hook up the input to a source of Sparkplug formatted JSON strings or 
objects and the encoded protobuf will be provided at the output.

Similarly if the input is hooked up to a source of protobufs encoded in the 
Sparkplug format, a decoded payload object will be provided at the output.