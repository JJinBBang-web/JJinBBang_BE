package JJinBBang.app.domain.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3Config {

	@Value("${cloud.aws.region.static}")
	private String region;

	@Value("${cloud.aws.credentials.access-key:}")
	private String accessKey;

	@Value("${cloud.aws.credentials.secret-key:}")
	private String secretKey;

	@Bean
	public S3Presigner s3Presigner() {
		S3Presigner.Builder builder = S3Presigner.builder()
			.region(Region.of(region));

		if (!accessKey.isBlank() && !secretKey.isBlank()) {
			builder.credentialsProvider(StaticCredentialsProvider.create(
				AwsBasicCredentials.create(accessKey, secretKey)));
		} else {
			builder.credentialsProvider(DefaultCredentialsProvider.builder().build());
		}

		return builder.build();
	}

	@Bean
	public S3Client s3() {
		S3ClientBuilder builder = S3Client.builder()
			.region(Region.of(region));

		if (!accessKey.isEmpty() && !secretKey.isEmpty()) {
			builder.credentialsProvider(StaticCredentialsProvider.create(
				AwsBasicCredentials.create(accessKey, secretKey)));
		} else {
			builder.credentialsProvider(DefaultCredentialsProvider.builder().build());
		}

		return builder.build();
	}
}
