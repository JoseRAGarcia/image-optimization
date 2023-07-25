package br.com.dehdo.imageoptimization;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class ImageOptimizationApplication {

	public static void main(String[] args) {
		//SpringApplication.run(ImageOptimizationApplication.class, args);

		SpringApplicationBuilder builder = new SpringApplicationBuilder(ImageOptimizationApplication.class);
		builder.headless(false);
		ConfigurableApplicationContext context = builder.run(args);
	}

}
