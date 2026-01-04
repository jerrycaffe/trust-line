package trustline

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@EnableJpaAuditing
class TrustlineApplication

fun main(args: Array<String>) {
	runApplication<TrustlineApplication>(*args)
}
