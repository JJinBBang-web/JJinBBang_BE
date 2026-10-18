package JJinBBang.app.support;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Testcontainers;

import JJinBBang.app.global.config.QuerydslConfig;

/**
 * 운영과 같은 MySQL(Testcontainers)로 JPA 슬라이스 테스트를 실행합니다.
 * Docker 가 없으면 테스트를 건너뜁니다.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@DataJpaTest(showSql = false, properties = "spring.config.location=classpath:/jpa-test.yml")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({MySqlContainerConfig.class, QuerydslConfig.class})
@Testcontainers(disabledWithoutDocker = true)
public @interface MySqlJpaTest {
}
