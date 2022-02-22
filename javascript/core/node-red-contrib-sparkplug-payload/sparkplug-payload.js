const sparkplug = require("sparkplug-payload");
const Payload = sparkplug.get("spBv1.0");

module.exports = (RED) => {
    function TranslatePayloadNode(config) {
        RED.nodes.createNode(this, config);
        let node = this;
        let newPayload;
        node.on('input', (msg) => {
            if (typeof (msg.payload === "string")) {
                try { // Check if JSON string and parse
                    msg.payload = JSON.parse(msg.payload);
                } catch {
                    // Payload wasn't a JSON string
                }
            }

            if (Buffer.isBuffer(msg.payload)) {// Payload is a protobuf
                newPayload = Payload.decodePayload(msg.payload);

            } else { // Payload might be an object
                try {
                    newPayload = Payload.encodePayload(msg.payload);

                } catch (e) { // Payload wasn't a valid object
                    this.error(e);
                }
            }
            msg.payload = newPayload;
            node.send(msg);
        })
    }
    RED.nodes.registerType("sparkplug-payload", TranslatePayloadNode);
}