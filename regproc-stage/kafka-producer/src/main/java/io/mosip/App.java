package io.mosip;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

/**
 * Hello world!
 */
public final class App {
    private App() {
    }

    /**
     * Says hello to the world.
     * @param args The arguments of the program.
     */
    public static void main(String[] args) {
        System.out.println("Hello World!");
         String bootstrapServers = "127.0.0.1:9092";

        // create Producer properties
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            // create the producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);
                // create a producer record
        String msg = generateMessage("10001100010001420241206041532");
        System.out.println(msg);
        ProducerRecord<String, String> producerRecord =
                new ProducerRecord<>("sample-stage-bus-in",
                "10001100010001420241206041532",
                msg);
                        // send data - asynchronous
        producerRecord.headers().add(
            "RID:","10001100010001420241206041532".getBytes()
            );
        producer.send(producerRecord);
        // flush data - synchronous
        producer.flush();
        // flush and close producer
        producer.close();
    }
    static String generateMessage(String rid){
        //10001100010001420241206041532
        //2025-05-16T11:00:01.000Z
        //2025-05-16T16:09.16Z'
        LocalDateTime now = LocalDateTime.now();
        // Define the format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        // Format the current date and time
        String formattedNow = 
        //ZonedDateTime.now().toString();
        now.format(formatter);

        // Print the formatted date and time
        System.out.println("Current Timestamp:" + formattedNow);
        String message = "{"+
        "\"reg_type\" : \"NEW\"," + 
        "\"rid\" : \"%s\",\n" + 
        "\"isValid\" : true,\n" + 
        "\"internalError\" : false,\n" + 
        "\"retryCount\" : 1,\n" + 
        "\"tags\" :{\"A1\":\"Value1\",\"A2\":\"Value2\"},\n" + 
        "\"source\" : \"RegClient\",\n" +
        "\"Iteration\" :1,\n" + 
        " \"workflowInstanceId\" : \"96d31522-101a-4c2a-949e-807c2b995c73\",\n" +
        "\"lastHopTimestamp\": \"%s\"\n" + 
        "}";

        return String.format(message,rid,formattedNow);

    }
}
