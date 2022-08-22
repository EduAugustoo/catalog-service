package com.polarbookshop.catalogservice.domain;

import com.polarbookshop.catalogservice.config.DataConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJdbcTest
@Import(DataConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("integration")
public class BookRepositoryJdbcTests {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private JdbcAggregateTemplate jdbcAggregateTemplate;

    @Test
    public void findBookByIsbnWhenExisting() {
        String bookIsbn = "1234561235";
        Book book = Book.build(bookIsbn, "Title", "Author", 12.90, "Polarsophia");
        jdbcAggregateTemplate.insert(book);

        Optional<Book> actualBook = bookRepository.findByIsbn(bookIsbn);

        assertThat(actualBook).isPresent();
        assertThat(actualBook.get().getIsbn()).isEqualTo(book.getIsbn());
    }

    @Test
    public void whenCreateBookNotAuthenticatedThenNoAuditMetadata() {
        Book bookToCreate = Book.build("1232343456", "Title", "Author", 12.90, "Polarsophia");
        Book createdBook = this.bookRepository.save(bookToCreate);

        assertThat(createdBook.getCreatedBy()).isNull();
        assertThat(createdBook.getLastModifiedBy()).isNull();
    }

    @Test
    @WithMockUser("john")
    public void whenCreateBookAuthenticatedThenAuditMetadata() {
        Book bookToCreate = Book.build("1232343456", "Title", "Author", 12.90, "Polarsophia");
        Book createdBook = this.bookRepository.save(bookToCreate);

        assertThat(createdBook.getCreatedBy()).isEqualTo("john");
        assertThat(createdBook.getLastModifiedBy()).isEqualTo("john");
    }

}
