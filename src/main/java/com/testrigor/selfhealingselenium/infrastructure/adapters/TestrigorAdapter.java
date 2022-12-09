package com.testrigor.selfhealingselenium.infrastructure.adapters;

import static com.testrigor.selfhealingselenium.application.utils.JsonHelpers.deserializeJson;
import static com.testrigor.selfhealingselenium.application.utils.JsonHelpers.serializeJson;
import static java.lang.String.format;
import static lombok.AccessLevel.PRIVATE;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import com.fasterxml.jackson.core.type.TypeReference;
import com.testrigor.selfhealingselenium.domain.model.Action;
import com.testrigor.selfhealingselenium.domain.model.Locator;
import com.testrigor.selfhealingselenium.domain.model.LocatorType;
import com.testrigor.selfhealingselenium.domain.model.NodeInformation;
import com.testrigor.selfhealingselenium.infrastructure.adapters.commands.LocatorsIdentifiersCommand;
import com.testrigor.selfhealingselenium.infrastructure.adapters.commands.ProcessLocatorCommand;
import com.testrigor.selfhealingselenium.infrastructure.adapters.response.HealedLocatorResponse;
import com.testrigor.selfhealingselenium.infrastructure.exceptions.SelfHealingException;
import com.typesafe.config.Config;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import okhttp3.Authenticator;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;

@Slf4j
@FieldDefaults(level = PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class TestrigorAdapter {
	public static final MediaType JSON
		= MediaType.get("application/json; charset=utf-8");

	private static final TypeReference<List<String>> XPATH_TYPE_REF = new TypeReference<>() {
	};
	private static final String LOCATORS_URL = "/api/v1/locators";
	private static final int TIMEOUT = 30;

	Config config;
	String apiToken;

	public List<String> getNodesXPaths(String pageSource) {
		val client = client();
		RequestBody body = RequestBody.create(JSON, serializeJson(new LocatorsIdentifiersCommand(pageSource)));

		val url = baseUrl() + LOCATORS_URL + "/identifiers";

		Request request = new Request.Builder()
			.url(url)
			.post(body)
			.build();

		try (Response response = client.newCall(request).execute()) {

			if (response.isSuccessful()) {
				return deserializeJson(response.body().string(), XPATH_TYPE_REF);
			}
			log.error("The call to {} returned {}", url, response.code());
			throw new SelfHealingException("");
		} catch (IOException e) {
			log.error("Error when calling the API", e);
			throw new SelfHealingException(e);
		}
	}

	@SuppressFBWarnings("SIC_INNER_SHOULD_BE_STATIC_ANON")
	public void saveLocator(Action action, List<NodeInformation> pageNodes, String pageSource, String testCaseName) {
		val command = ProcessLocatorCommand.builder()
			.testCaseName(testCaseName)
			.locatorType(action.getLocator().getType())
			.locatorValue(action.getLocator().getValue())
			.pageNodes(pageNodes)
			.pageSource(pageSource)
			.build();

		val client = client();
		RequestBody body = RequestBody.create(JSON, serializeJson(command));
		Request request = new Request.Builder()
			.url(baseUrl() + LOCATORS_URL)
			.post(body)
			.build();

		client.newCall(request).enqueue(new Callback() {
			@Override public void onFailure(Call call, IOException exception) {
				log.error("Exception when calling {}", LOCATORS_URL, exception);
			}

			@Override public void onResponse(Call call, Response response) {
				if (!response.isSuccessful()) {
					val error = format("The call to %s returned error code %s", LOCATORS_URL, response.code());
					log.error(error);
					throw new SelfHealingException(error);
				}
				log.debug("Action saved correctly");
			}
		});
	}

	public Locator getHealedLocator(Locator locator, List<NodeInformation> pageNodes, String pageSource, String testCaseName) {
		val command = ProcessLocatorCommand.builder()
			.testCaseName(testCaseName)
			.locatorType(locator.getType())
			.locatorValue(locator.getValue())
			.pageNodes(pageNodes)
			.pageSource(pageSource)
			.build();

		val client = client();
		RequestBody body = RequestBody.create(JSON, serializeJson(command));
		Request request = new Request.Builder()
			.url(baseUrl() + format("%s/healed", LOCATORS_URL))
			.post(body)
			.build();

		try (Response response = client.newCall(request).execute()) {

			if (response.isSuccessful()) {
				val healedLocatorResponse = deserializeJson(response.body().string(), HealedLocatorResponse.class);
				return new Locator(
					LocatorType.fromName(healedLocatorResponse.getLocatorType()),
					healedLocatorResponse.getLocatorValue());
			}
			val error = format("The call to %s returned error code %s", LOCATORS_URL, response.code());
			log.error(error);
			throw new SelfHealingException(error);
		} catch (IOException e) {
			log.error("Error when calling {}", LOCATORS_URL, e);
			throw new SelfHealingException(e);
		}
	}

	private String baseUrl() {
		return config.getString("testrigor.api");
	}

	OkHttpClient client() {
		return new OkHttpClient.Builder()
			.authenticator(new Authenticator() {
				@Nullable @Override public Request authenticate(Route route, Response response) throws IOException {
					if (response.request().header("Api-Token") != null) {
						return null;
					}
					return response.request().newBuilder()
						.header("Api-Token", apiToken)
						.build();
				}
			})
			.connectTimeout(TIMEOUT, TimeUnit.SECONDS)
			.build();
	}
}
