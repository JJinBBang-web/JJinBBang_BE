package JJinBBang.app.domain.common.config;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3Config {

	@Value("${cloud.aws.region.static}")
	private String region;

	@Value("${cloud.aws.credentials.access-key:}")
	private String accessKey;

	@Value("${cloud.aws.credentials.secret-key:}")
	private String secretKey;

	@Value("${cloud.aws.s3.endpoint:}")
	private String endpoint;

	@Value("${cloud.aws.s3.path-style-access-enabled:false}")
	private boolean pathStyleAccessEnabled;

	@Bean
	public S3Presigner s3Presigner() {
		S3Presigner.Builder builder = S3Presigner.builder()
			.region(Region.of(region));
		configureEndpoint(builder);

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
		configureEndpoint(builder);

		if (!accessKey.isEmpty() && !secretKey.isEmpty()) {
			builder.credentialsProvider(StaticCredentialsProvider.create(
				AwsBasicCredentials.create(accessKey, secretKey)));
		} else {
			builder.credentialsProvider(DefaultCredentialsProvider.builder().build());
		}

		return builder.build();
	}

	private void configureEndpoint(S3Presigner.Builder builder) {
		if (!endpoint.isBlank()) {
			builder.endpointOverride(URI.create(endpoint))
				.serviceConfiguration(S3Configuration.builder()
					.pathStyleAccessEnabled(pathStyleAccessEnabled)
					.build());
		}
	}

	private void configureEndpoint(S3ClientBuilder builder) {
		if (!endpoint.isBlank()) {
			builder.endpointOverride(URI.create(endpoint))
				.forcePathStyle(pathStyleAccessEnabled);
		}
	}
}
