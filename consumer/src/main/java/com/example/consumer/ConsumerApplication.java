package com.example.consumer;

import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.aws.context.config.annotation.EnableContextCredentials;
import org.springframework.cloud.aws.context.config.annotation.EnableContextRegion;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;

@SpringBootApplication
@EnableBinding(Sink.class)
@EnableContextRegion(region = "${cloud.aws.region.static}")
@EnableContextCredentials(accessKey = "${cloud.aws.credentials.accessKey}", secretKey = "${cloud.aws.credentials.secretKey}")
public class ConsumerApplication {
	private static Logger logger = LoggerFactory.getLogger(ConsumerApplication.class);

	@Autowired
	private JavaMailSender mailSender;
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
		this.mailSender.send(new MimeMessagePreparator() {

			@Override
			public void prepare(MimeMessage mimeMessage) throws Exception {
				MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
				helper.addTo(env.getProperty("cloud.aws.verified-senders.to-email"));
				helper.setFrom(env.getProperty("cloud.aws.verified-senders.from-email"));
				helper.setSubject(timeInfo.getLabel());
				helper.setText(timeInfo.getTime(), false);
			}
		});
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
