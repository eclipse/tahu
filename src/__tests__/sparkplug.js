jest.mock('mqtt')
const mqtt = require('mqtt')
const sparkplug = require('../index.js')

beforeAll(() => {
  mqtt.connect.mockImplementation(() => {
    mqtt.MqttClient.prototype.on.mockImplementation((eventId, callback) => {
      if (eventId === 'connect') {
        setTimeout(() => {
          callback()
        }, 50)
      }
      if (eventId === 'message') {
        setTimeout(() => {
          callback('STATE/test', 'ONLINE')
        }, 50)
      }
    })
    const client = new mqtt.MqttClient()
    return client
  })
})

test('Creating new client subscribes to state.', async () => {
  const config = {
    serverUrl: 'tcp://localhost:1883',
    username: 'aUsername',
    password: 'aPassword',
    groupId: 'aGroup',
    edgeNode: 'aNode',
    clientId: 'aNode',
    publishDeath: true,
    version: 'spBv1.0'
  }
  const { hostId, payload } = await new Promise((resolve, reject) => {
    client = sparkplug.newClient(config)
    client.on('state', (hostId, payload) => {
      resolve({ hostId, payload })
    })
  })
  expect(mqtt.MqttClient.prototype.subscribe).toBeCalledWith('STATE/#', {
    qos: 0
  })
  expect(hostId).toBe('test')
  expect(payload).toBe('ONLINE')
  client.stop()
})
