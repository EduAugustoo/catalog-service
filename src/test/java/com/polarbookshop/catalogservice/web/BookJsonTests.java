package com.polarbookshop.catalogservice.web;

import com.polarbookshop.catalogservice.domain.Book;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class BookJsonTests {

    @Autowired
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private JacksonTester<Book> json;

    @Test
    void testSerialize() throws IOException {
        Book book = Book.build("1234567890", "Title", "Author", 9.90, "Polarsophia");
        var jsonContent = json.write(book);
        assertThat(jsonContent).extractingJsonPathStringValue("@.isbn").isEqualTo("1234567890");
        assertThat(jsonContent).extractingJsonPathStringValue("@.title").isEqualTo("Title");
        assertThat(jsonContent).extractingJsonPathStringValue("@.author").isEqualTo("Author");
        assertThat(jsonContent).extractingJsonPathNumberValue("@.price").isEqualTo(9.90);
    }

    @Test
    void testDeserialize() throws IOException {
        var content = "{\n" +
                "\"isbn\": \"1234567890\",\n" +
                "\"title\": \"Title\",\n" +
                "\"author\": \"Author\",\n" +
                "\"price\": 9.90,\n" +
                "\"publisher\": \"Polarsophia\"\n" +
                "}";

        assertThat(json.parse(content))
                .usingRecursiveComparison()
                .isEqualTo(Book.build("1234567890", "Title", "Author", 9.90, "Polarsophia"));
    }
}
