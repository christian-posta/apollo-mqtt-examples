/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package example.topic;

import example.util.DefaultCallback;
import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;

import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="http://www.christianposta.com/blog">Christian Posta</a>
 */
@SuppressWarnings("unchecked")
public class Publisher {

    private static final String BROKER_URL = "tcp://localhost:61613";
    private static final String DEFAULT_DESTINATION = "test/messages";
    private static final long DELAY = 100;
    private static final int NUM_MESSAGES_TO_SEND = 100;

    private static boolean USE_CALLBACK_API = false;

    public static void main(String[] args) throws Exception {

        String brokerURL = BROKER_URL;

        if (args.length == 1) {
            brokerURL = args[0].trim();
        }

        System.out.println("Using " + BROKER_URL + " to connect to Apollo");

        MQTT mqtt = new MQTT();
        mqtt.setHost(brokerURL);
        mqtt.setUserName("admin");
        mqtt.setPassword("password");

        if (USE_CALLBACK_API) {
            publishWithCallbackAPI(mqtt);
        } else {
            publishWithBlockingAPI(mqtt);
        }


    }

    private static void publishWithBlockingAPI(MQTT mqtt) throws Exception {
        BlockingConnection connection = mqtt.blockingConnection();

        connection.connect();

        // wait until the connection is established
        while(!connection.isConnected());

        if (connection.isConnected()) {
            String message = null;
            for (int i = 0; i < NUM_MESSAGES_TO_SEND; i++) {
                message = "Message #" + i;
                System.out.println("Sending: " + message);
                connection.publish(DEFAULT_DESTINATION, message.getBytes(), QoS.AT_MOST_ONCE, false);
                TimeUnit.MILLISECONDS.sleep(DELAY);
            }

            System.out.println("Sending END frame");
            connection.publish(DEFAULT_DESTINATION, "END".getBytes(), QoS.AT_MOST_ONCE, false);

            connection.disconnect();
        } else {
            System.out.println("Could not publish. Connection unexpectedly not established.");
        }
    }

    private static void publishWithCallbackAPI(MQTT mqtt) throws Exception{
        final CallbackConnection connection = mqtt.callbackConnection();
        final CountDownLatch finished = new CountDownLatch(1);

        connection.connect(new DefaultCallback(){
            @Override
            public void onSuccess(Object value) {
                String message = null;
                for (int i = 0; i < NUM_MESSAGES_TO_SEND; i++) {
                    message = "Message #" + i;
                    System.out.println("Sending: " + message);
                    connection.publish(DEFAULT_DESTINATION, message.getBytes(), QoS.AT_MOST_ONCE, false, null);

                    try {
                        TimeUnit.MILLISECONDS.sleep(DELAY);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                System.out.println("Sending END frame");
                connection.publish(DEFAULT_DESTINATION, "END".getBytes(), QoS.AT_MOST_ONCE, false, null);
                finished.countDown();
            }
        });

        finished.await();
    }
}
