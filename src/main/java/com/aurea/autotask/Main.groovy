package com.aurea.autotask

import groovy.util.logging.Log4j2
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.Banner
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.ConfigurableApplicationContext

@Log4j2
@SpringBootApplication
@EnableAutoConfiguration
class Main implements CommandLineRunner {

    private InternalReviewRunner internalReviewRunner

    @Autowired
    Main(InternalReviewRunner internalReviewRunner) {
        this.internalReviewRunner = internalReviewRunner
    }

    static void main(String[] args) throws URISyntaxException {
        SpringApplication app = new SpringApplication(Main.class)
        app.setBannerMode(Banner.Mode.OFF)
        ConfigurableApplicationContext context = app.run(args)
        context.close()
    }

    @Override
    void run(String... args) throws Exception {
        internalReviewRunner.run()
    }
}
