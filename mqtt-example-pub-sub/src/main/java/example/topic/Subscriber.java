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
import example.util.DefaultConnectionListener;
import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;

import java.util.concurrent.CountDownLatch;

/**
 * @author <a href="http://www.christianposta.com/blog">Christian Posta</a>
 */
public class Subscriber {

    private static final String BROKER_URL = "tcp://localhost:61613";
    private static final String DEFAULT_DESTINATION = "test/messages";
    private static final long DELAY = 100;
    private static final int NUM_MESSAGES_TO_SEND = 100;

    private static boolean USE_CALLBACK_API = true;

    public static void main(String[] args) throws Exception{
        System.out.println("Waiting to receive messages");
        System.out.println("Will wait for END message or Ctrl+C to exit");

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
            subscribeWithCallbackAPI(mqtt);
        } else {
            subscribeWithBlockingAPI(mqtt);
        }
    }

    private static void subscribeWithBlockingAPI(MQTT mqtt) {

    }

    private static void subscribeWithCallbackAPI(MQTT mqtt) throws InterruptedException {
        final CallbackConnection connection = mqtt.callbackConnection();

        final CountDownLatch finished = new CountDownLatch(1);

        connection.listener(new DefaultConnectionListener() {
            @Override
            public void onPublish(UTF8Buffer topic, Buffer body, Runnable ack) {
                String payload = body.length() > 0 ? body.utf8().toString(): "Unavailable: ERROR";
                System.out.println("Received message: " + payload);

                if (payload.trim().equalsIgnoreCase("END")) {
                    finished.countDown();
                }
                ack.run();
            }
        });

        connection.connect(new DefaultCallback(){
            @Override
            public void onSuccess(Object value) {
                Topic destination = new Topic(DEFAULT_DESTINATION, QoS.AT_MOST_ONCE);
                connection.subscribe(new Topic[] {destination}, new DefaultCallback(){
                    @Override
                    public void onSuccess(Object value) {
                        System.out.println("Subscription successful. Waiting for messages...");
                    }
                });
            }
        });

        finished.await();
    }
}
