package com.luanvv.springboot.rest.configuration;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Properties;

import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.stereotype.Component;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.amazonaws.services.secretsmanager.model.InvalidParameterException;
import com.amazonaws.services.secretsmanager.model.InvalidRequestException;
import com.amazonaws.services.secretsmanager.model.ResourceNotFoundException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.luanvv.springboot.rest.exceptions.DataSourceSecretDoesNotExistException;
import com.luanvv.springboot.rest.exceptions.DataSourceSecretEmptyOrNullException;
import com.luanvv.springboot.rest.exceptions.DataSourceSecretInvalidException;

@Component
public class DatabasePropertiesListener implements ApplicationListener<ApplicationPreparedEvent> {

	private static final String AWS_SECRETS_REGION = "spring.aws.secretsmanager.region";
//	private static final String AWS_SECRETS_ENDPOINT = "spring.aws.secretsmanager.endpoint";
	private static final String AWS_SECRET_NAME = "spring.aws.secretsmanager.secretName";
	private final static String SPRING_DATASOURCE_USERNAME = "spring.datasource.username";
	private final static String SPRING_DATASOURCE_PASSWORD = "spring.datasource.password";
	private final static String SPRING_DATASOURCE_URL = "spring.datasource.url";

	@Override
	public void onApplicationEvent(ApplicationPreparedEvent event) {
		ConfigurableEnvironment env = event.getApplicationContext().getEnvironment();
		String secretName = env.getProperty(AWS_SECRET_NAME);
//		String endpoints = env.getProperty(AWS_SECRETS_ENDPOINT);
		String AWSRegion = env.getProperty(AWS_SECRETS_REGION);
//		AwsClientBuilder.EndpointConfiguration config = new AwsClientBuilder.EndpointConfiguration(endpoints,
//				AWSRegion);
//		AWSSecretsManagerClientBuilder clientBuilder = AWSSecretsManagerClientBuilder.standard();
//		clientBuilder.setEndpointConfiguration(config);
		AWSSecretsManager client = AWSSecretsManagerClientBuilder.standard()
                .withRegion(AWSRegion)
                .build();
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode secretsJson = null;
		GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest().withSecretId(secretName);
		GetSecretValueResult getSecretValueResponse = null;
		try {
			getSecretValueResponse = client.getSecretValue(getSecretValueRequest);
		} catch (ResourceNotFoundException e) {
			throw new DataSourceSecretInvalidException("The requested secret " + secretName + " was not found", e);
		} catch (InvalidRequestException e) {
			throw new DataSourceSecretInvalidException("The request was invalid", e);
		} catch (InvalidParameterException e) {
			throw new DataSourceSecretInvalidException("The request had invalid params", e);
		}
		if (getSecretValueResponse == null) {
			throw new DataSourceSecretDoesNotExistException();
		}
		String secret = getSecretValueResponse.getSecretString();
		if (secret != null) {
			try {
				secretsJson = objectMapper.readTree(secret);
			} catch (IOException e) {
				throw new DataSourceSecretInvalidException("Exception while retrieving secret values", e);
			}
		} else {
			throw new DataSourceSecretEmptyOrNullException("The Secret String returned is null");
		}
		
		String engine = secretsJson.get("engine").textValue();
		String host = secretsJson.get("host").textValue();
		Number port = secretsJson.get("port").numberValue();
		String dbname = secretsJson.get("dbInstanceIdentifier").textValue();
		String username = secretsJson.get("username").textValue();
		String password = secretsJson.get("password").textValue();
		String url = MessageFormat.format(
				"jdbc:{0}://{1}:{2}/{3}?useSSL=false&useUnicode=yes&characterEncoding=UTF-8", engine, host, port, dbname);
		Properties props = new Properties();
		props.put(SPRING_DATASOURCE_USERNAME, username);
		props.put(SPRING_DATASOURCE_PASSWORD, password);
		props.put(SPRING_DATASOURCE_URL, url);
		env.getPropertySources().addFirst(new PropertiesPropertySource(AWS_SECRET_NAME, props));

	}

}
