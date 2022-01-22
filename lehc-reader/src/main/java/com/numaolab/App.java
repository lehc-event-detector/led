package com.numaolab;

import com.impinj.octane.BitPointers;
import com.impinj.octane.ImpinjReader;
import com.impinj.octane.MemoryBank;
import com.impinj.octane.OctaneSdkException;
import com.impinj.octane.Settings;
import com.impinj.octane.TagFilterMode;
import com.impinj.octane.TagReport;
import com.impinj.octane.TagReportListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class App implements TagReportListener {

    MqttClient client;

    App() {
        // MQTT Clientの初期化
        String broker = "tcp://mosquitto:1883";
        String clientId = "JavaSample";
        MemoryPersistence persistence = new MemoryPersistence();
        try {
            this.client = new MqttClient(broker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            System.out.println("Connecting to broker: " + broker);
            this.client.connect(connOpts);
            System.out.println("Connected");
        } catch (MqttException me) {
            me.printStackTrace();
        }

        // Readerの初期化
        try {
            ImpinjReader reader = new ImpinjReader();
            String address = "192.168.11.17";
            reader.connect(address);
            Settings settings = reader.queryDefaultSettings();
            settings.getReport().setIncludePeakRssi(true); // RSSIの設定
            settings.getReport().setIncludePhaseAngle(true); // Phaseの設定
            settings.getReport().setIncludeFirstSeenTime(true); // FirstSeenTimeの設定
            settings.getReport().setIncludeLastSeenTime(true); // LastSeenTimeの設定
            settings.getFilters().getTagFilter1().setMemoryBank(MemoryBank.Epc);
            settings.getFilters().getTagFilter1().setBitPointer(BitPointers.Epc);
            settings.getFilters().getTagFilter1().setTagMask("02");
            settings.getFilters().getTagFilter1().setBitCount(8);
            settings.getFilters().setMode(TagFilterMode.OnlyFilter1);
            reader.applySettings(settings);
            reader.setTagReportListener(this);
            reader.start();
        } catch (OctaneSdkException ex) {
            System.out.println(ex.getMessage());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace(System.out);
        }
    }

    @Override
    public void onTagReported(ImpinjReader reader, TagReport report) {
        report.getTags().parallelStream().forEach(t -> {
            // System.out.println(t.getEpc());
            // epcを2進数に変換
            String bin = "";
            String[] splited = t.getEpc().toString().split(" ");
            for (int i = 0; i < splited.length; i++) {
                int dec = Integer.parseInt(splited[i], 16);
                String b = Integer.toBinaryString(dec);
                if (b.length() < 16) {
                    int need = 16 - b.length();
                    for (int j = 0; j < need; j++) {
                        b = '0' + b;
                    }
                }
                bin = bin + b;
            }
            // json文字列を作成
            String template = "{\"header\":\"%s\",\"other\":\"%s\",\"env\":\"%s\",\"eri\":\"%s\",\"logic\":\"%s\",\"k\":\"%s\",\"kd\":\"%s\",\"gid\":\"%s\",\"mbit\":\"%s\",\"igs\":\"%s\",\"rssi\":\"%s\",\"phase\":\"%s\",\"time\":\"%s\"}";
            String jsonString = String.format(template, bin.substring(0, 8), bin.substring(0, 12), bin.substring(12, 20), bin.substring(20, 24), bin.substring(24, 40), bin.substring(40, 44), bin.substring(44, 48), bin.substring(48, 80), bin.substring(80, 81), bin.substring(81, 96), t.getPeakRssiInDbm(), t.getPhaseAngleInRadians(), t.getFirstSeenTime().ToString());
            // MQTTに配信
            try {
                client.publish("all", jsonString.getBytes(), 0, false);
            } catch (MqttPersistenceException e) {} catch (MqttException e) {}
        });
    }

    public static void main( String[] args ) {
        new App();
    }
}
