package co.ravn.ecommerce;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Full application context load test. Requires PostgreSQL (see application-test.properties).
 * In CI (GitHub Actions), runs only when TEST_DB_URL is set so the pipeline passes without a DB;
 * set repo secrets TEST_DB_URL, TEST_DB_USERNAME, TEST_DB_PASSWORD to run this test in CI.
 */
@SpringBootTest
@ActiveProfiles("test")
@EnabledIf("testDbAvailable")
class EcommerceApplicationTests {

	static boolean testDbAvailable() {
		boolean inCi = "true".equals(System.getenv("GITHUB_ACTIONS"));
		String dbUrl = System.getenv("TEST_DB_URL");
		return !inCi || (dbUrl != null && !dbUrl.isBlank());
	}

	@Test
	void contextLoads() {
	}

}
