const mqtt = require('mqtt')
const client = mqtt.connect('mqtt://192.168.88.133:1883')

const topics = ['all']

client.on('connect', () => {
  topics.forEach(e => {
    client.subscribe(e, (err) => {
      if (err) console.error(err)
    })
  })
})

client.on('message', (topic, message) => {
  // count++
  console.log(`${topic}: ${message.toString()}`)
})
