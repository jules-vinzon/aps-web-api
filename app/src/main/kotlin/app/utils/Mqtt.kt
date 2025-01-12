package app.utils

import app.App
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttMessage

class Mqtt {
    fun publish(breadcrumb: Breadcrumb, topic: String, msg: String) {
        breadcrumb.log("START MQTT PUBLISH")
        breadcrumb.log("TOPIC $topic")
        val message = MqttMessage(msg.toByteArray())
        breadcrumb.log("MESSAGE $msg")
        message.qos = 2
        val response = App.mqttClient.publish(topic, message)
        App.mqttClient.setCallback(object : MqttCallback {
            override fun messageArrived(topic: String?, message: MqttMessage?) {
                breadcrumb.log("Received message: ${message?.toString()} on topic: $topic")
            }

            override fun connectionLost(cause: Throwable?) {
                breadcrumb.log("Connection lost: ${cause?.message}")
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                breadcrumb.log("Message delivery complete")
            }
        })
        breadcrumb.log("MQTT RESPONSE $response")
        breadcrumb.log("END MQTT PUBLISH")
    }

    fun subscribe(breadcrumb: Breadcrumb, topic: String) {
        breadcrumb.log("START MQTT SUBSCRIBE")
        App.mqttClient.subscribe(topic, 1)

        App.mqttClient.setCallback(object : MqttCallback {
            override fun messageArrived(topic: String?, message: MqttMessage?) {
                breadcrumb.log("Received message: ${message?.toString()} on topic: $topic")
            }

            override fun connectionLost(cause: Throwable?) {
                breadcrumb.log("Connection lost: ${cause?.message}")
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                breadcrumb.log("Message delivery complete")
            }
        })
        breadcrumb.log("Message sent")
    }
}