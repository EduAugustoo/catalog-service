package com.polarbookshop.catalogservice.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class BookService {

    private final BookRepository bookRepository;

    public Iterable<Book> viewBookList() {
        return this.bookRepository.findAll();
    }

    public Book viewBookDetails(String isbn) {
        return this.bookRepository.findByIsbn(isbn).orElseThrow(() -> new BookNotFoundException(isbn));
    }

    public Book addBookToCatalog(Book book) {
        if (this.bookRepository.existsByIsbn(book.getIsbn())) {
            throw new BookAlreadyExistsException(book.getIsbn());
        }

        return this.bookRepository.save(book);
    }

    public void removeBookFromCatalog(String isbn) {
        this.bookRepository.deleteByIsbn(isbn);
    }

    public Book editBookDetails(String isbn, Book book) {
        Optional<Book> existingBook = this.bookRepository.findByIsbn(isbn);
        if (existingBook.isEmpty()) {
            return addBookToCatalog(book);
        }

        Book bookToUpdate = new Book(existingBook.get().getIsbn(), book.getTitle(), book.getAuthor(), book.getPrice());
        return this.bookRepository.save(bookToUpdate);
    }
}
