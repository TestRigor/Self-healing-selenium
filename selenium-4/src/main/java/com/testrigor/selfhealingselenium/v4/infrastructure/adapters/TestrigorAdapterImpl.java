package com.testrigor.selfhealingselenium.v4.infrastructure.adapters;

import static com.testrigor.selfhealingselenium.commons.application.utils.JsonHelpers.deserializeJson;
import static com.testrigor.selfhealingselenium.commons.application.utils.JsonHelpers.serializeJson;
import static java.lang.String.format;
import static lombok.AccessLevel.PRIVATE;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.testrigor.selfhealingselenium.commons.infrastructure.adapters.TestrigorAdapter;
import com.testrigor.selfhealingselenium.commons.infrastructure.adapters.commands.LocatorsIdentifiersCommand;
import com.testrigor.selfhealingselenium.commons.infrastructure.adapters.commands.ProcessLocatorCommand;
import com.testrigor.selfhealingselenium.commons.infrastructure.adapters.response.HealedLocatorResponse;
import com.testrigor.selfhealingselenium.commons.infrastructure.exceptions.SelfHealingException;
import com.testrigor.selfhealingselenium.commons.domain.model.Action;
import com.testrigor.selfhealingselenium.commons.domain.model.Locator;
import com.testrigor.selfhealingselenium.commons.domain.model.LocatorType;
import com.testrigor.selfhealingselenium.commons.domain.model.NodeInformation;
import com.typesafe.config.Config;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
@FieldDefaults(level = PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class TestrigorAdapterImpl implements TestrigorAdapter {
	private static final TypeReference<List<String>> XPATH_TYPE_REF = new TypeReference<>() {
	};
	private static final String LOCATORS_URL = "/api/v1/locators";
	private static final int TIMEOUT = 30;

	ExecutorService executorService = Executors.newCachedThreadPool();
	Config config;
	String apiToken;

	@Override
	public List<String> getNodesXPaths(String pageSource) {
		val client = client();

		val url = baseUrl() + LOCATORS_URL + "/identifiers";

		val request = requestBuilder(URI.create(url))
			.POST(HttpRequest.BodyPublishers.ofString(serializeJson(new LocatorsIdentifiersCommand(pageSource))))
			.build();

		try {
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

			if (isSuccessful(response.statusCode())) {
				return deserializeJson(response.body(), XPATH_TYPE_REF);
			}
			log.error("The call to {} returned {}", url, response.statusCode());
			throw new SelfHealingException("");
		} catch (IOException e) {
			log.error("Error when calling the API", e);
			throw new SelfHealingException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new SelfHealingException(e);
		}
	}

	@SuppressFBWarnings("SIC_INNER_SHOULD_BE_STATIC_ANON")
	@Override
	public void saveLocator(Action action, List<NodeInformation> pageNodes, String pageSource, String testCaseName) {
		val command = ProcessLocatorCommand.builder()
			.testCaseName(testCaseName)
			.locatorType(action.getLocator().getType())
			.locatorValue(action.getLocator().getValue())
			.pageNodes(pageNodes)
			.pageSource(pageSource)
			.build();

		val request = requestBuilder(URI.create(baseUrl() + LOCATORS_URL))
			.POST(HttpRequest.BodyPublishers.ofString(serializeJson(command)))
			.build();

		client().sendAsync(request, HttpResponse.BodyHandlers.ofString())
			.thenAcceptAsync((response) -> {
				if (!isSuccessful(response.statusCode())) {
					val error = format("The call to %s returned error code %s", LOCATORS_URL, response.statusCode());
					log.error(error);
					throw new SelfHealingException(error);
				}
				log.debug("Action saved correctly");
			});
	}

	@Override
	public Locator getHealedLocator(Locator locator, List<NodeInformation> pageNodes, String pageSource, String testCaseName) {
		val command = ProcessLocatorCommand.builder()
			.testCaseName(testCaseName)
			.locatorType(locator.getType())
			.locatorValue(locator.getValue())
			.pageNodes(pageNodes)
			.pageSource(pageSource)
			.build();

		val client = client();

		val request = requestBuilder(URI.create(baseUrl() + format("%s/healed", LOCATORS_URL)))
			.POST(HttpRequest.BodyPublishers.ofString(serializeJson(command)))
			.build();

		try {

			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

			if (isSuccessful(response.statusCode())) {
				val healedLocatorResponse = deserializeJson(response.body(), HealedLocatorResponse.class);
				return new Locator(
					LocatorType.fromName(healedLocatorResponse.getLocatorType()),
					healedLocatorResponse.getLocatorValue());
			}
			val error = format("The call to %s returned error code %s", LOCATORS_URL, response.statusCode());
			log.error(error);
			throw new SelfHealingException(error);
		} catch (IOException e) {
			log.error("Error when calling {}", LOCATORS_URL, e);
			throw new SelfHealingException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new SelfHealingException(e);
		}
	}

	private String baseUrl() {
		return config.getString("testrigor.api");
	}

	private HttpClient client() {
		return HttpClient.newBuilder()
			.executor(executorService)
			.connectTimeout(Duration.ofSeconds(TIMEOUT))
			.version(HttpClient.Version.HTTP_2)
			.build();
	}

	@SuppressWarnings("checkstyle:MagicNumber")
	private boolean isSuccessful(int status) {
		return status >= 200 && status < 300;
	}

	private HttpRequest.Builder requestBuilder(URI uri) {
		return HttpRequest.newBuilder()
			.uri(uri)
			.header("Content-Type", "application/json; charset=utf-8")
			.header("Api-Token", apiToken);
	}
}
