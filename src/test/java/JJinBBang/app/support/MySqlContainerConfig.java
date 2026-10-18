package JJinBBang.app.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MySQLContainer;

/**
 * 테스트용 MySQL 컨테이너를 띄우고 datasource 로 연결합니다.
 */
@TestConfiguration(proxyBeanMethods = false)
public class MySqlContainerConfig {

	@Bean
	@ServiceConnection
	MySQLContainer<?> mySqlContainer() {
		return new MySQLContainer<>("mysql:8.0.40");
	}
}
