package dev.tenantguard.tenantguard_lab;

import dev.tenantguard.tenantguard_lab.config.LabDataInitializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class TenantguardLabApplicationTests {
	@Autowired
	ApplicationContext context;

	@Test
	void contextLoads() {
	}

	@Test
	void labInitializerIsAbsentWithoutLabProfile() {
		assertTrue(context.getBeansOfType(LabDataInitializer.class).isEmpty());
	}

}
