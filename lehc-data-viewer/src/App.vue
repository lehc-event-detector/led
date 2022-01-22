<template>
  <div id="app">
    <Chart :styles="myStyles" :chart-data="datacollection" :options="options"></Chart>
    <button type="button" @click="extend()">拡張</button>
    <div class="form-wrapper"><form><input type="text" v-model="value"><button type="button" @click="fillData()">更新</button></form></div>
  </div>
</template>

<script>
import Chart from './components/Chart.vue'

const dataColors = [
  'rgba(27, 210, 55, 0.8)',
  'rgba(93, 43, 201, 0.8)',
  'rgba(213, 157, 71, 0.8)',
  'rgba(155, 148, 221, 0.8)',
  'rgba(186, 42, 147, 0.8)'
]

const resultColors = [
  'rgba(255, 0, 0, 0.8)',
  'rgba(0, 255, 0, 0.8)',
  'rgba(0, 0, 255, 0.8)'
]

export default {
  name: 'App',
  components: {
    Chart
  },
  data () {
    return {
      datacollection: {},
      options: {
        responsive: true,
        maintainAspectRatio: false,
        // height: 100,
        scales: {
          yAxes: [{
            ticks: {
                min: -75,
                max: 50
            }
          }]
        }
      },
      chartWidth: window.innerWidth-50,
      chartHeight: window.innerHeight-100,
      value: ''
    }
  },
  mounted () {
  },
  methods: {
    extend() {
      this.chartWidth += 100;
    },
    async fillData () {
      const [gid, db] = this.value.split('@')
      const [log, result] = await Promise.all([
        fetch(`/log/${gid}?db=${db}`).then(res => res.json()),
        fetch(`/result/${gid}?db=${db}`).then(res => res.json())
      ])
      let maxWidth = 0;
      log.forEach((d, i) => {
        d['radius'] = 1
        d['backgroundColor'] = dataColors[i%dataColors.length]
        d['borderWidth'] = 0
        maxWidth = maxWidth < d['data'].length * 5 ? d['data'].length * 5 : maxWidth
      })
      result.forEach((d, i) => {
        d['radius'] = 3
        d['backgroundColor'] = resultColors[i%resultColors.length]
        d['borderWidth'] = 0
        maxWidth = maxWidth < d['data'].length * 5 ? d['data'].length * 5 : maxWidth
      })
      this.datacollection = {
        datasets: [...log, ...result]
      }
      this.chartWidth = maxWidth
    }
  },
  computed: {
    myStyles () {
      return {
        width: `${this.chartWidth}px`,
        height: `${this.chartHeight}px`,
        position: 'relative'
      }
    }
  }
}
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
}
.form-wrapper {
  display: inline-block;
  margin-top: 20px;
}
form {
  display: flex;
  background: lavender;
  border-radius: 5px;
  height: 1.8rem;
  width: 500px
}
form input {
  border: none;
  outline: none;
  background: transparent;
  flex: 1;
  padding: 0 5px;
}
form button {
  border: none;
  background: transparent;
  cursor: pointer;
  font-weight: bold;
  color: white;
  background: midnightblue;
  border-radius: 0 5px 5px 0;
  padding: 0 10px;
}
</style>
