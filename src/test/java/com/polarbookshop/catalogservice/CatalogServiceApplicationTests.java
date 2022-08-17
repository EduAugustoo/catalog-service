package com.polarbookshop.catalogservice;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.polarbookshop.catalogservice.domain.Book;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import lombok.Getter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration")
@Testcontainers(disabledWithoutDocker = true)
class CatalogServiceApplicationTests {

    private static KeycloakToken bjornTokens;
    private static KeycloakToken isabelleTokens;

    @Autowired
    private WebTestClient webTestClient;

    @Container
    private static final KeycloakContainer keycloakContainer =
            new KeycloakContainer("quay.io/keycloak/keycloak:18.0")
                    .withRealmImportFile("test-realm-config.json");

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri",
                () -> keycloakContainer.getAuthServerUrl() + "realms/PolarBookshop");
    }

    @BeforeAll
    static void generateAccessTokens() {
        WebClient webClient = WebClient.builder()
                .baseUrl(keycloakContainer.getAuthServerUrl() + "realms/PolarBookshop/protocol/openid-connect/token")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .build();

        isabelleTokens = authenticateWith("isabelle", "password", webClient);
        bjornTokens = authenticateWith("bjorn", "password", webClient);
    }

    @BeforeEach
    void cleanUp() {
        webTestClient
                .delete()
                .uri("/books/1231231230")
                .headers(headers -> headers.setBearerAuth(isabelleTokens.getAccessToken()))
                .exchange();
    }

    @Test
    void whenGetRequestEmployeeRoleWithIdThenBookReturned() {
        String bookIsbn = "1231231230";
        Book bookToCreate = Book.build(bookIsbn, "Title", "Author", 9.90, "Polarsophia");
        Book expectedBook = webTestClient
                .post()
                .uri("/books")
                .headers(headers -> headers.setBearerAuth(isabelleTokens.getAccessToken()))
                .bodyValue(bookToCreate)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Book.class).value(book -> assertThat(book).isNotNull())
                .returnResult().getResponseBody();

        webTestClient
                .get()
                .uri("/books/" + bookIsbn)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(Book.class).value(actualBook -> {
                    assertThat(actualBook).isNotNull();
                    assertThat(actualBook.getIsbn()).isEqualTo(expectedBook.getIsbn());
                });
    }

    @Test
    void whenGetRequestCustomerRoleWithIdThenBookReturned() {
        String bookIsbn = "1231231230";
        Book bookToCreate = Book.build(bookIsbn, "Title", "Author", 9.90, "Polarsophia");
        Book expectedBook = webTestClient
                .post()
                .uri("/books")
                .headers(headers -> headers.setBearerAuth(isabelleTokens.getAccessToken()))
                .bodyValue(bookToCreate)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Book.class).value(book -> assertThat(book).isNotNull())
                .returnResult().getResponseBody();

        webTestClient
                .get()
                .uri("/books/" + bookIsbn)
                .headers(headers -> headers.setBearerAuth(isabelleTokens.getAccessToken()))
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(Book.class).value(actualBook -> {
                    assertThat(actualBook).isNotNull();
                    assertThat(actualBook.getIsbn()).isEqualTo(expectedBook.getIsbn());
                });
    }

    @Test
    void whenPostRequestThenBookCreated() {
        Book expectedBook = Book.build("1231231230", "Title", "Author", 9.90, "Polarsophia");

        webTestClient
                .post()
                .uri("/books")
                .headers(headers -> headers.setBearerAuth(isabelleTokens.getAccessToken()))
                .bodyValue(expectedBook)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Book.class).value(actualBook -> {
                    assertThat(actualBook).isNotNull();
                    assertThat(actualBook.getIsbn()).isEqualTo("1231231230");
                });
    }

    @Test
    void whenPostRequestUnauthorizedThen403() {
        Book expectedBook = Book.build("1231231230", "Title", "Author", 9.90, "Polarsophia");

        webTestClient
                .post()
                .uri("/books")
                .headers(headers -> headers.setBearerAuth(bjornTokens.getAccessToken()))
                .bodyValue(expectedBook)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void whenPostRequestUnauthenticatedThen401() {
        Book expectedBook = Book.build("1231231230", "Title", "Author", 9.90, "Polarsophia");

        webTestClient
                .post()
                .uri("/books")
                .bodyValue(expectedBook)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void whenPutRequestThenBookUpdated() {
        String bookIsbn = "1231231230";
        Book bookToCreate = Book.build(bookIsbn, "Title", "Author", 9.90, "Polarsophia");
        Book expectedBook = webTestClient
                .post()
                .uri("/books")
                .headers(headers -> headers.setBearerAuth(isabelleTokens.getAccessToken()))
                .bodyValue(bookToCreate)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Book.class).value(book -> assertThat(book).isNotNull())
                .returnResult().getResponseBody();

        Book bookToUpdate = new Book(
                expectedBook.getId(),
                expectedBook.getIsbn(),
                expectedBook.getTitle(),
                expectedBook.getAuthor(),
                7.95,
                expectedBook.getPublisher(),
                expectedBook.getCreatedDate(),
                expectedBook.getLastModifiedDate(),
                expectedBook.getVersion());

        webTestClient
                .put()
                .uri("/books/" + bookIsbn)
                .headers(headers -> headers.setBearerAuth(isabelleTokens.getAccessToken()))
                .bodyValue(bookToUpdate)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Book.class).value(actualBook -> {
                    assertThat(actualBook).isNotNull();
                    assertThat(actualBook.getPrice()).isEqualTo(bookToUpdate.getPrice());
                });
    }

    @Test
    void whenPutRequestUnauthorizedThen403() {
        String bookIsbn = "1231231230";
        Book bookToCreate = Book.build(bookIsbn, "Title", "Author", 9.90, "Polarsophia");
        Book expectedBook = webTestClient
                .post()
                .uri("/books")
                .headers(headers -> headers.setBearerAuth(isabelleTokens.getAccessToken()))
                .bodyValue(bookToCreate)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Book.class).value(book -> assertThat(book).isNotNull())
                .returnResult().getResponseBody();

        Book bookToUpdate = new Book(
                expectedBook.getId(),
                expectedBook.getIsbn(),
                expectedBook.getTitle(),
                expectedBook.getAuthor(),
                7.95,
                expectedBook.getPublisher(),
                expectedBook.getCreatedDate(),
                expectedBook.getLastModifiedDate(),
                expectedBook.getVersion());

        webTestClient
                .put()
                .uri("/books/" + bookIsbn)
                .headers(headers -> headers.setBearerAuth(bjornTokens.getAccessToken()))
                .bodyValue(bookToUpdate)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void whenPutRequestUnauthenticatedThen403() {
        String bookIsbn = "1231231230";
        Book bookToCreate = Book.build(bookIsbn, "Title", "Author", 9.90, "Polarsophia");
        Book expectedBook = webTestClient
                .post()
                .uri("/books")
                .headers(headers -> headers.setBearerAuth(isabelleTokens.getAccessToken()))
                .bodyValue(bookToCreate)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Book.class).value(book -> assertThat(book).isNotNull())
                .returnResult().getResponseBody();

        Book bookToUpdate = new Book(
                expectedBook.getId(),
                expectedBook.getIsbn(),
                expectedBook.getTitle(),
                expectedBook.getAuthor(),
                7.95,
                expectedBook.getPublisher(),
                expectedBook.getCreatedDate(),
                expectedBook.getLastModifiedDate(),
                expectedBook.getVersion());

        webTestClient
                .put()
                .uri("/books/" + bookIsbn)
                .bodyValue(bookToUpdate)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void whenDeleteRequestEmployeeRoleThenBookDeleted() {
        String bookIsbn = "1231231230";
        Book bookToCreate = Book.build(bookIsbn, "Title", "Author", 9.90, "Polarsophia");
        webTestClient
                .post()
                .uri("/books")
                .headers(headers -> headers.setBearerAuth(isabelleTokens.getAccessToken()))
                .bodyValue(bookToCreate)
                .exchange()
                .expectStatus().isCreated();

        webTestClient
                .delete()
                .uri("/books/" + bookIsbn)
                .headers(headers -> headers.setBearerAuth(isabelleTokens.getAccessToken()))
                .exchange()
                .expectStatus().isNoContent();

        webTestClient
                .get()
                .uri("/books/" + bookIsbn)
                .headers(headers -> headers.setBearerAuth(isabelleTokens.getAccessToken()))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(String.class).value(errorMessage ->
                        assertThat(errorMessage).isEqualTo("The book with ISBN " + bookIsbn + " was not found")
                );
    }

    @Test
    void whenDeleteRequestCustomerRoleThen403() {
        String bookIsbn = "1231231230";
        Book bookToCreate = Book.build(bookIsbn, "Title", "Author", 9.90, "Polarsophia");
        webTestClient
                .post()
                .uri("/books")
                .headers(headers -> headers.setBearerAuth(isabelleTokens.getAccessToken()))
                .bodyValue(bookToCreate)
                .exchange()
                .expectStatus().isCreated();

        webTestClient
                .delete()
                .uri("/books/" + bookIsbn)
                .headers(headers -> headers.setBearerAuth(bjornTokens.getAccessToken()))
                .exchange()
                .expectStatus().isForbidden();

        webTestClient
                .get()
                .uri("/books/" + bookIsbn)
                .headers(headers -> headers.setBearerAuth(bjornTokens.getAccessToken()))
                .exchange()
                .expectStatus().isOk()
                .expectBody(Book.class).value(actualBook -> {
                    assertThat(actualBook).isNotNull();
                    assertThat(actualBook.getIsbn()).isEqualTo("1231231230");
                });
    }

    @Test
    void whenDeleteRequestUnauthenticatedThen401() {
        String bookIsbn = "1231231230";
        Book bookToCreate = Book.build(bookIsbn, "Title", "Author", 9.90, "Polarsophia");
        webTestClient
                .post()
                .uri("/books")
                .headers(headers -> headers.setBearerAuth(isabelleTokens.getAccessToken()))
                .bodyValue(bookToCreate)
                .exchange()
                .expectStatus().isCreated();

        webTestClient
                .delete()
                .uri("/books/" + bookIsbn)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    private static KeycloakToken authenticateWith(String username, String password, WebClient webClient) {
        return webClient.post()
                .body(BodyInserters.fromFormData("grant_type", "password")
                        .with("client_id", "polar-test")
                        .with("username", username)
                        .with("password", password))
                .retrieve()
                .bodyToMono(KeycloakToken.class)
                .block();
    }

    @Getter
    private static class KeycloakToken {

        private final String accessToken;

        @JsonCreator
        private KeycloakToken(@JsonProperty("access_token") final String accessToken) {
            this.accessToken = accessToken;
        }
    }
}
