package com.example.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.core.env.Environment;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;

@SpringBootApplication
@EnableBinding(Sink.class)
public class ConsumerApplication {
	private static Logger logger = LoggerFactory.getLogger(ConsumerApplication.class);

	@Autowired
	private Environment env;

	public static void main(String[] args) {
		SpringApplication.run(ConsumerApplication.class, args);
	}

	@StreamListener(Sink.INPUT)
	public void log(TimeInfo timeInfo) {
		logger.info("Received: " + timeInfo.toString());

		this.sendMailMessage(timeInfo);

		logger.info(
						String.format("Sent email to: %s",
													this.env.getProperty("cloud.aws.verified-senders.to-email")
						)
		);
	}

	private void sendMailMessage(TimeInfo timeInfo) {
		System.setProperty("aws.accessKeyId", this.env.getProperty("cloud.aws.credentials.accessKey"));
		System.setProperty("aws.secretKey", this.env.getProperty("cloud.aws.credentials.secretKey"));

		AmazonSimpleEmailService client = AmazonSimpleEmailServiceClientBuilder.standard()
						.withRegion(Regions.US_WEST_2).build();

		SendEmailRequest request = new SendEmailRequest()
						.withDestination(
										new Destination()
														.withToAddresses(this.env.getProperty("cloud.aws.verified-senders.to-email"))
						)
						.withMessage(
										new Message()
														.withBody(
																		new Body()
																						.withHtml(
																										new Content()
																														.withCharset("UTF-8")
																														.withData(timeInfo.getTime()))
																						.withText(
																										new Content()
																														.withCharset("UTF-8")
																														.withData(timeInfo.getTime())
																						)
														)
														.withSubject(
																		new Content()
																						.withCharset("UTF-8")
																						.withData(timeInfo.getLabel())
														)
						)
						.withSource(this.env.getProperty("cloud.aws.verified-senders.from-email"));

		client.sendEmail(request);
	}

	public static class TimeInfo {
		private String time;
		private String label;

		String getTime() {
			return time;
		}

		public void setTime(String time) {
			this.time = time;
		}

		String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		@Override
		public String toString() {
			return String.format("[%s] %s", label, time);
		}
	}
}
