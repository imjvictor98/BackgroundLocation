package com.joaovictor.backgroundlocation;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.text.TextUtils;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;


//ACTION_DEVICE_INFO_SETTINGS

public class MQTTManager implements MqttCallbackExtended {
  
  public SharedPreferences sharedPreferences;
  
  
  private String topicTelemtry;
  private String topicCommands;
  private String topicConfig;
  private String topicStatus;
  
  
  private MqttAndroidClient mqttAndroidClient;
  private String serialId;
  private String clientID = "07a84855d68740c19f7db4f3f793c88b";
  private final int connectionTimeOut = 30;
  private final int keepAliveInterval = 60;
  private final Boolean isCleanSession = true;
  private final Boolean isAutoRecconect = true;
  private String userMqtt;
  private String passwordMqtt;
  private String mqttUrl;
  private String topic = "/iot4decision/location";
  private static final String TAG = "testeMQTT";
  public boolean isConnected = false;
  
  
  //Construtor do MQTT
  public MQTTManager(Context context) {
    initMqttClient(context);
  }
  
  private void initMqttClient(Context context) {
    try {
      initPreferences();
      mqttAndroidClient = new MqttAndroidClient(context, mqttUrl, clientID);
      mqttAndroidClient.setCallback(this);
      connect();
    } catch (Exception e) {
      e.printStackTrace();
      mqttAndroidClient = null;
    }
  }
  
  private void connect() {
    MqttConnectOptions myConnectOption = new MqttConnectOptions();
    myConnectOption.setCleanSession(isCleanSession);
    myConnectOption.setAutomaticReconnect(isAutoRecconect);
    myConnectOption.setConnectionTimeout(connectionTimeOut);
    myConnectOption.setKeepAliveInterval(keepAliveInterval);
    myConnectOption.setUserName(userMqtt);
    myConnectOption.setPassword(passwordMqtt.toCharArray());
    try {
      mqttAndroidClient.connect(myConnectOption, null, new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
          
          DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
          disconnectedBufferOptions.setBufferEnabled(false);
          disconnectedBufferOptions.setBufferSize(100);
          disconnectedBufferOptions.setPersistBuffer(false);
          disconnectedBufferOptions.setDeleteOldestMessages(true);
          mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
          Log.d(TAG, "onMQTTSuccess: Conectou com o cliente MQTT");
          //          returnState();
          //          returnLocation();
        }
        
        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
          Log.w(TAG, "Falha ao conectar: " + mqttUrl + exception.toString());
        }
      });
    } catch (MqttException e) {
      e.printStackTrace();
    }
  }
  
  private void subscribeToTopic() {
    try {
      mqttAndroidClient.subscribe(topic, 1, null, new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
          
          Log.w(TAG, "Sucessuful subscribed to: " + topic);
          
        }
        
        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
          
          Log.w(TAG, "Nao foi possivel subscribe!");
        }
      });
    } catch (MqttException e) {
      e.printStackTrace();
    }
  }
  
  public void publishTelemtry(String message) {
    Log.i("onPublish", message);
    
    String topic = "/iot4decision/location";
    
    Log.d("onPublishTelemetry", "If there is a topic: " + topic);
    try {
      if (mqttAndroidClient != null)
        if (mqttAndroidClient.isConnected()) {
          mqttAndroidClient.publish(topic, new MqttMessage(message.getBytes()));
          Log.d("onPublishTelemetry", "I DID IT");
        }
      
    } catch (MqttException e) {
      e.printStackTrace();
    }
  }
  
  private void publishStatus(String message) {
    
    try {
      if (mqttAndroidClient != null)
        if (mqttAndroidClient.isConnected())
          mqttAndroidClient.publish(topic, new MqttMessage(message.getBytes()));
    } catch (MqttException e) {
      e.printStackTrace();
    }
  }
  
  private void initPreferences() {
    userMqtt = "admin";
    passwordMqtt = "admin";
    mqttUrl = "tcp://broker.hivemq.com:1883";
  }
  

  
  
  public void setMqttCallback(MqttCallbackExtended cb) {
    mqttAndroidClient.setCallback(cb);
  }
  
  public MqttAndroidClient getMqttAndroidClient() {
    return this.mqttAndroidClient;
  }
  
  public void disconnectMqtt() {
    if (mqttAndroidClient != null) {
      try {
        mqttAndroidClient.disconnect();
        mqttAndroidClient = null;
      } catch (MqttException e) {
        e.printStackTrace();
      }
      
    }
  }
  
  public void returnLocation(Location location) {
    JSONObject body = new JSONObject();
    JSONObject locationJSON = new JSONObject();
    
    try {
      locationJSON.put("latitude", location.getLatitude());
      locationJSON.put("longitude", location.getLongitude());
      body.put("location", location);
    } catch (JSONException e) {
      e.printStackTrace();
    }
    publishTelemtry(body.toString());
    
  }
  
  private void returnState() {
    JSONObject body = new JSONObject();
    try {
      body.put("battery", "100%");
    } catch (JSONException e) {
      e.printStackTrace();
    }
    publishStatus(body.toString());
  }
  
  
  @Override
  public void connectComplete(boolean reconnect, String serverURI) {
    //Toast.makeText(App.getInstance(), "MQTT Conectado com sucesso", Toast.LENGTH_LONG).show();
    //    String topicC = "/iot4decision/" + db.getCliente() + "/gateway/"+ db.getSerial() +"/" +
    //    topicConfig;
    //    String topic = "/iot4decision/" + db.getCliente() + "/gateway/"+ db.getSerial() +"/" +
    //    topicCommands;
    //    subscribeToTopic(topicC);
    subscribeToTopic();
  }
  
  @Override
  public void connectionLost(Throwable cause) {
    //Toast.makeText(App.getInstance(), "Perdeu conexão com o MQTT", Toast.LENGTH_LONG).show();
    Log.e("onMQTTLost", cause.toString());
  }
  
  @Override
  public void messageArrived(String topic, MqttMessage message) throws Exception {
    try {
      JSONObject jsonObject = new JSONObject(message.toString());
      String command = jsonObject.getString("command");
      if (TextUtils.equals(command, "returnState")) {
        //                Toast.makeText(App.getInstance(), "Recebeu comando \"returnSate\"",
        //                Toast.LENGTH_LONG).show();
        returnState();
      } else if (TextUtils.equals(command, "returnLocation")) {
        //                Toast.makeText(App.getInstance(), "Recebeu comando \"returnLocation\"",
        //                Toast.LENGTH_LONG).show();
        //        returnLocation();
      }
    } catch (JSONException err) {
      Log.d("Error", err.toString());
    }
    
  }
  
  @Override
  public void deliveryComplete(IMqttDeliveryToken token) {
  
  }
}
