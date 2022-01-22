import express from 'express'
import { MongoClient } from "mongodb"

const uri = "mongodb://root:example@192.168.88.133:27017?retryWrites=true&writeConcern=majority"
const client = new MongoClient(uri)
let db
const app = express()

app.get('/log/:gid', async (req, res) => {
  const dbName = req.query.db
  const gid = req.params.gid

  const datasets = {}
  const dataStream = client.db(dbName).collection('log').find({ gid }).sort({ time: 1 }).stream();

  dataStream.on("error", (err) => {
    throw err
  })

  dataStream.on("data", (doc) => {
    if (!datasets[doc.igs]) {
      datasets[doc.igs] = {
        label: doc.igs,
        data: []
      }
    }
    datasets[doc.igs]['data'].push({
      x: doc.time,
      y: doc.rssi
    })
  })

  dataStream.on("end", () => {
    res.send(Object.values(datasets))
  })
})

app.get('/result/:gid', async (req, res) => {
  const dbName = req.query.db
  const gid = req.params.gid

  const datasets = {}
  const dataStream = client.db(dbName).collection('result').find({ gid }).sort({ detectedAt: 1 }).stream();

  dataStream.on("error", (err) => {
    throw err
  })

  dataStream.on("data", (doc) => {
    if (!datasets[doc.logic]) {
      datasets[doc.logic] = {
        label: doc.logic,
        data: []
      }
    }
    datasets[doc.logic]['data'].push({
      x: doc.detectedAt,
      y: -50
    })
  })

  dataStream.on("end", () => {
    res.send(Object.values(datasets))
  })
})

app.get('/health', (req, res) => {
  res.sendStatus(200)
})

app.listen(3000, async () => {
  await client.connect()
  console.log('Listening on http://localhost:3000')
})