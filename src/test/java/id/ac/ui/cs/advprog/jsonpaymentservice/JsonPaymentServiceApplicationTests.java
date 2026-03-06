package id.ac.ui.cs.advprog.jsonpaymentservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "app.jwt.secret=test-jwt-secret",
    "app.internal.service-key=test-internal-key"
})
class JsonPaymentServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
