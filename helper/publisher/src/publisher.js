
const mqtt = require('mqtt')
const client = mqtt.connect(`mqtt://${process.env.HOST}:${process.env.PORT}`)
const topic = process.env.TOPIC
const header = process.env.HEADER
const env = process.env.ENV

// setInterval(() => {
//   const message = JSON.stringify({"igs":"000000000000000","mbit":"1","rssi":"60","phase":"0","time":Date.now().toString() + "000","gid":"00000000000000000000000000000000","k":"0010","kd":"0001","logic":"0000010","header":"00000010","other":"0000000000000","env":"00000001","eri":"0000"})
//   client.publish(topic, message)
// }, 800)
// setInterval(() => {
//   const message = JSON.stringify({"igs":"000000000000001","mbit":"0","rssi":"30","phase":"0","time":Date.now().toString() + "000","gid":"00000000000000000000000000000000","k":"0010","kd":"0001","logic":"0000010","header":"00000010","other":"0000000000000","env":"00000001","eri":"0000"})
//   client.publish(topic, message)
// }, 700)
setInterval(() => {
  const message = JSON.stringify({"igs":"000000000000001","mbit":"0","rssi":"30","phase":"0","time":Date.now().toString() + "000","gid":"00000000000000000000000000000000","k":"0010","kd":"0001","logic":"0000010","header":header,"other":"0000000000000","env":env,"eri":"0000"})
  client.publish(topic, message)
}, 10)
// setInterval(() => {
//   const message = JSON.stringify({"igs":"000000000000000","mbit":"1","rssi":"30","phase":"0","time":Date.now().toString(),"gid":"00000000000000000000000000000001","k":"0001","kd":"0001","logic":"0000000","header":"00000000","other":"0000000000000","env":"10011001","eri":"0000"})
//   client.publish(topic, message)
// }, 1)
// setInterval(() => {
//   const message = JSON.stringify({"igs":"000000000000000","mbit":"1","rssi":"30","phase":"0","time":Date.now().toString(),"gid":"00000000000000000000000000000010","k":"0001","kd":"0001","logic":"0000000","header":"00000000","other":"0000000000000","env":"10011001","eri":"0000"})
//   client.publish(topic, message)
// }, 1)
// setInterval(() => {
//   const message = JSON.stringify({"igs":"000000000000000","mbit":"1","rssi":"30","phase":"0","time":Date.now().toString(),"gid":"00000000000000000000000000000011","k":"0001","kd":"0001","logic":"0000000","header":"00000000","other":"0000000000000","env":"10011001","eri":"0000"})
//   client.publish(topic, message)
// }, 1)
// setInterval(() => {
//   const message = JSON.stringify({"igs":"000000000000000","mbit":"1","rssi":"30","phase":"0","time":Date.now().toString(),"gid":"00000000000000000000000000000100","k":"0001","kd":"0001","logic":"0000000","header":"00000000","other":"0000000000000","env":"10011001","eri":"0000"})
//   client.publish(topic, message)
// }, 1)
// setInterval(() => {
//   const message = JSON.stringify({"igs":"000000000000000","mbit":"1","rssi":"30","phase":"0","time":Date.now().toString(),"gid":"00000000000000000000000000000101","k":"0001","kd":"0001","logic":"0000000","header":"00000000","other":"0000000000000","env":"10011001","eri":"0000"})
//   client.publish(topic, message)
// }, 1)
// setInterval(() => {
//   const message = JSON.stringify({"igs":"000000000000000","mbit":"1","rssi":"30","phase":"0","time":Date.now().toString(),"gid":"00000000000000000000000000001000","k":"0001","kd":"0001","logic":"0000000","header":"00000000","other":"0000000000000","env":"10011001","eri":"0000"})
//   client.publish(topic, message)
// }, 1)
// setInterval(() => {
//   const message = JSON.stringify({"igs":"000000000000000","mbit":"1","rssi":"30","phase":"0","time":Date.now().toString(),"gid":"00001000000000000000000000000000","k":"0001","kd":"0001","logic":"0000000","header":"00000000","other":"0000000000000","env":"10011001","eri":"0000"})
//   client.publish(topic, message)
// }, 1)
// setInterval(() => {
//   const message = JSON.stringify({"igs":"000000000000000","mbit":"1","rssi":"30","phase":"0","time":Date.now().toString(),"gid":"00000000000000010000000000000000","k":"0001","kd":"0001","logic":"0000000","header":"00000000","other":"0000000000000","env":"10011001","eri":"0000"})
//   client.publish(topic, message)
// }, 1)
// setInterval(() => {
//   const message = JSON.stringify({"igs":"000000000000000","mbit":"1","rssi":"30","phase":"0","time":Date.now().toString(),"gid":"00000000010000000000000000000000","k":"0001","kd":"0001","logic":"0000000","header":"00000000","other":"0000000000000","env":"10011001","eri":"0000"})
//   client.publish(topic, message)
// }, 1)
