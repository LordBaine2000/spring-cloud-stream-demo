package com.example.producer;

import java.util.Date;

import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.support.MessageBuilder;

@SpringBootApplication
@EnableBinding(Source.class)
public class ProducerApplication {
	private static org.slf4j.Logger logger = LoggerFactory.getLogger(ProducerApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(ProducerApplication.class, args);
	}

	@Bean
	@InboundChannelAdapter(value = Source.OUTPUT, poller = @Poller(fixedDelay = "10000", maxMessagesPerPoll = "1"))
	public MessageSource<TimeInfo> timerMessageSource() {
		return () -> {
			final TimeInfo timeInfo = new TimeInfo(new Date().getTime()+"", "Time");

			logger.info("Sent: " + timeInfo.toString());

			return MessageBuilder.withPayload(timeInfo).build();
		};
	}

	public static class TimeInfo {
		private final String time;
		private final String label;

		TimeInfo(String time, String label) {
			this.time = time;
			this.label = label;
		}

		public String getTime() {
			return time;
		}

		public String getLabel() {
			return label;
		}

		@Override
		public String toString() {
			return String.format("[%s] %s", label, time);
		}
	}
}
