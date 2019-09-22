package hive.kirby.swagger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.HashSet;
import java.util.Set;

@Configuration
@EnableSwagger2
public class SwaggerConfig extends WebMvcConfigurationSupport {

  @Bean
  public Docket apiDocumentation() {
    Set<String> responseContentTypes = new HashSet<>();
    responseContentTypes.add("*/*");
    responseContentTypes.add("text/plain");
    responseContentTypes.add("application/json");
    responseContentTypes.add("image/jpeg");
    return new
        Docket(DocumentationType.SWAGGER_2)
        .select()
        .apis(RequestHandlerSelectors.basePackage("hive.kirby"))
        .build()
        .produces(responseContentTypes)
        .consumes(responseContentTypes)
        .apiInfo(metaData());
  }

  private ApiInfo metaData() {
    return new
        ApiInfoBuilder()
        .title("Kirby endpoints")
        .description("\"Profile image management API\""
            + "\n Repository: https://github.com/hex-g/kirby"
            + "\n Created by: https://github.com/hex-g/")
        .version("v1.0")
        .license("")
        .licenseUrl("")
        .build();
  }

  @Override
  protected void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("swagger-ui.html")
        .addResourceLocations("classpath:/META-INF/resources/");
    registry.addResourceHandler("/webjars/**")
        .addResourceLocations("classpath:/META-INF/resources/webjars/");
  }
}