package com.meditracker.vitalsservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.meditracker.vitalsservice.dto.VitalAlertEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

	@Value("${spring.kafka.producer.bootstrap-servers}")
	private String bootstrapServers;

	@Bean
	public ObjectMapper objectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		return mapper;
	}

	@Bean
	public ProducerFactory<String, VitalAlertEvent> producerFactory(ObjectMapper objectMapper) {
		Map<String, Object> config = new HashMap<>();
		config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

		JsonSerializer<VitalAlertEvent> serializer = new JsonSerializer<>(objectMapper);
		serializer.setAddTypeInfo(false);

		DefaultKafkaProducerFactory<String, VitalAlertEvent> factory = new DefaultKafkaProducerFactory<>(config);
		factory.setValueSerializer(serializer);
		return factory;
	}

	@Bean
	public KafkaTemplate<String, VitalAlertEvent> kafkaTemplate(
			ProducerFactory<String, VitalAlertEvent> producerFactory) {
		return new KafkaTemplate<>(producerFactory);
	}
}