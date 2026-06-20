import com.acme.client.*;
import com.acme.types.ShowInput;
import com.netflix.graphql.dgs.client.GraphQLResponse;
import com.netflix.graphql.dgs.client.MonoGraphQLClient;
import com.netflix.graphql.dgs.client.WebClientGraphQLClient;
import com.netflix.graphql.dgs.client.codegen.GraphQLQueryRequest;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.intellij.lang.annotations.Language;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
public class Main {
  public static void main(String[] args) {
    // Configure a WebClient for your needs, e.g. including authentication headers and TLS.
    WebClient webClient = WebClient.create("http://localhost:8080/graphql");
    WebClientGraphQLClient client = MonoGraphQLClient.createWithWebClient(webClient);

    getShows(client);
    addShow(client);
    getShows(client);
    getTheaters(client);
  }

  public static void getShows(WebClientGraphQLClient client) {
    @Language("graphql")
    String query =
        new GraphQLQueryRequest(
                ShowsGraphQLQuery.newRequest().queryName("shows").build(),
                new ShowsProjectionRoot<>().id().title())
            .serialize();

    Mono<GraphQLResponse> graphQLResponseMono = client.reactiveExecuteQuery(query);

    // GraphQLResponse has convenience methods to extract fields using JsonPath.
    var response = graphQLResponseMono.map(r -> r.dataAsObject(Map.class));

    // Don't forget to subscribe! The request won't be executed otherwise.
    response.subscribe();
    log.info("shows: {}", response.block());
  }

  public static void addShow(WebClientGraphQLClient client) {
    @Language("graphql")
    String query =
        new GraphQLQueryRequest(
                CreateShowGraphQLQuery.newRequest()
                    .queryName("createShow")
                    .showInput(ShowInput.newBuilder().releaseYear(2025).title("New Show").build())
                    .build(),
                new ShowsProjectionRoot<>().id().title())
            .serialize();

    Mono<GraphQLResponse> graphQLResponseMono = client.reactiveExecuteQuery(query);

    // GraphQLResponse has convenience methods to extract fields using JsonPath.
    var response = graphQLResponseMono.map(r -> r.dataAsObject(Map.class));

    // Don't forget to subscribe! The request won't be executed otherwise.
    response.subscribe();
    log.info("addShow: {}", response.block());
  }

  public static void getTheaters(WebClientGraphQLClient client) {
    @Language("graphql")
    String query =
        new GraphQLQueryRequest(
                TheatersGraphQLQuery.newRequest().queryName("theaters").build(),
                new TheatersProjectionRoot<>().id().name().location())
            .serialize();

    Mono<GraphQLResponse> graphQLResponseMono = client.reactiveExecuteQuery(query);

    // GraphQLResponse has convenience methods to extract fields using JsonPath.
    var response = graphQLResponseMono.map(r -> r.dataAsObject(Map.class));

    // Don't forget to subscribe! The request won't be executed otherwise.
    response.subscribe();
    log.info("theaters: {}", response.block());
  }
}
