package org.springframework.samples.crm.client;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.thrift.ThriftHttpMessageConverter;
import org.springframework.samples.crm.config.CommonConfiguration;
import org.springframework.thrift.crm.Customer;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Demonstrates communication with the server through the MessagePack message converter and RestTemplate
 *
 * @author Josh Long
 */
public class RestClient {

	static private Log log = LogFactory.getLog(RestClient.class);

	@Configuration
	@Import(CommonConfiguration.class)
	public static class ClientConfiguration {

		@Inject private CommonConfiguration commonConfiguration;

		private ClientHttpRequestInterceptor[] requestInterceptors = {
			 new ClientHttpRequestInterceptor() {
				 @Override
				 public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
					 request.getHeaders().setAccept(Arrays.asList(ThriftHttpMessageConverter.MEDIA_TYPE));
					 debug("Request: ", request.getHeaders());
					 return execution.execute(request, body);
				 }
			 }
		};

		@Bean
		public RestTemplate restTemplate() {
			RestTemplate restTemplate = new RestTemplate();
			restTemplate.setMessageConverters(Arrays.<HttpMessageConverter<?>>asList(commonConfiguration.messagePackHttpMessageConverter()));
			restTemplate.setInterceptors(requestInterceptors);
			return restTemplate;
		}
	}

	public static void main(String[] args) throws Throwable {

		AnnotationConfigApplicationContext annotationConfigApplicationContext = new AnnotationConfigApplicationContext(ClientConfiguration.class);
		RestTemplate client = annotationConfigApplicationContext.getBean(RestTemplate.class);

		String url = buildServiceUrl("ws/customers/{customerId}");

		Map<String, Object> mapOfVars = new HashMap<String, Object>();
		mapOfVars.put("customerId", 3);

		Customer customer = client.getForEntity( url, Customer.class , mapOfVars).getBody();
		log.info("response payload: " + ToStringBuilder.reflectionToString(customer));
		client.execute(url, HttpMethod.GET, null, new DebuggingResponseExtractor() , mapOfVars);
	}

	static String buildServiceUrl(String prefix) {
		return "http://localhost:8080/rest/" + (prefix.startsWith("/") ? "" : "/")+ prefix;
	}

	static class DebuggingResponseExtractor implements ResponseExtractor<Object> {
		@Override
		public Object extractData(ClientHttpResponse response) throws IOException {
				debug("Response: ", response.getHeaders());
		 return null;
		}
	}

	static void debug(String title, HttpHeaders headers) {
		System.out.println("============== " + title + " ==============");
		for (String k : headers.keySet()) {
			System.out.println(String.format("%s = %s", k, headers.get(k)))  ;
		}
	}

}