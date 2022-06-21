package com.polarbookshop.catalogservice;

import com.polarbookshop.catalogservice.domain.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CatalogServiceApplicationTests {

    @Autowired
    private WebTestClient webTestClient;

    @BeforeEach
    void cleanUp() {
        webTestClient
                .delete()
                .uri("/books/1231231230")
                .exchange();
    }

    @Test
    void whenGetRequestWithIdThenBookReturned() {
        String bookIsbn = "1231231230";
        Book bookToCreate = Book.build(bookIsbn, "Title", "Author", 9.90);
        Book expectedBook = webTestClient
                .post()
                .uri("/books")
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
    void whenPostRequestThenBookCreated() {
        Book expectedBook = Book.build("1231231230", "Title", "Author", 9.90);

        webTestClient
                .post()
                .uri("/books")
                .bodyValue(expectedBook)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Book.class).value(actualBook -> {
                    assertThat(actualBook).isNotNull();
                    assertThat(actualBook.getIsbn()).isEqualTo("1231231230");
                });
    }

    @Test
    void whenPutRequestThenBookUpdated() {
        String bookIsbn = "1231231230";
        Book bookToCreate = Book.build(bookIsbn, "Title", "Author", 9.90);
        Book expectedBook = webTestClient
                .post()
                .uri("/books")
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
                expectedBook.getCreatedDate(),
                expectedBook.getLastModifiedDate(),
                expectedBook.getVersion());

        webTestClient
                .put()
                .uri("/books/" + bookIsbn)
                .bodyValue(bookToUpdate)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Book.class).value(actualBook -> {
                    assertThat(actualBook).isNotNull();
                    assertThat(actualBook.getPrice()).isEqualTo(bookToUpdate.getPrice());
                });
    }

    @Test
    void whenDeleteRequestThenBookDeleted() {
        String bookIsbn = "1231231230";
        Book bookToCreate = Book.build(bookIsbn, "Title", "Author", 9.90);
        webTestClient
                .post()
                .uri("/books")
                .bodyValue(bookToCreate)
                .exchange()
                .expectStatus().isCreated();

        webTestClient
                .delete()
                .uri("/books/" + bookIsbn)
                .exchange()
                .expectStatus().isNoContent();

        webTestClient
                .get()
                .uri("/books/" + bookIsbn)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(String.class).value(errorMessage ->
                        assertThat(errorMessage).isEqualTo("The book with ISBN " + bookIsbn + " was not found")
                );
    }
}
