package com.dauducbach.identity_service.configuration;

import com.dauducbach.event.NotificationEvent;
import com.dauducbach.event.ProfileCreationEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonSerializer;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {
    @Bean
    public KafkaSender<String, NotificationEvent> kafkaSender(){
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");  // Đọc từ application.yaml
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        SenderOptions<String, NotificationEvent> senderOptions = SenderOptions.create(props);
        return KafkaSender.create(senderOptions);
    }

    @Bean
    public KafkaSender<String, ProfileCreationEvent> kafkaSenderProfile() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");  // Đọc từ application.yaml
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        SenderOptions<String, ProfileCreationEvent> senderOptions = SenderOptions.create(props);

        return KafkaSender.create(senderOptions);
    }
}
