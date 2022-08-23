package com.polarbookshop.catalogservice.web;

import com.polarbookshop.catalogservice.domain.Book;
import com.polarbookshop.catalogservice.domain.BookService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RequestMapping("/books")
@RequiredArgsConstructor
@RestController
public class BookController {

    private static final Logger log = LoggerFactory.getLogger(BookController.class);

    private final BookService bookService;

    @GetMapping
    public Iterable<Book> get() {
        log.info("Fetching the list of books in the catalog");
        return this.bookService.viewBookList();
    }

    @GetMapping("/{isbn}")
    public Book getByIsbn(@PathVariable String isbn) {
        log.info("Fetching details about the book: {}", isbn);
        return this.bookService.viewBookDetails(isbn);
    }

    @PostMapping
    public ResponseEntity<Book> post(@Valid @RequestBody Book book) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.bookService.addBookToCatalog(book));
    }

    @DeleteMapping("/{isbn}")
    public ResponseEntity<Void> delete(@PathVariable String isbn) {
        log.info("Deleting book: {}", isbn);
        this.bookService.removeBookFromCatalog(isbn);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PutMapping("/{isbn}")
    public Book put(@PathVariable String isbn, @Valid @RequestBody Book book) {
        return this.bookService.editBookDetails(isbn, book);
    }
}
