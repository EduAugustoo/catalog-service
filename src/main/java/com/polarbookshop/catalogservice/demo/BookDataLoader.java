package com.polarbookshop.catalogservice.demo;

import com.polarbookshop.catalogservice.domain.Book;
import com.polarbookshop.catalogservice.domain.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
@Profile("testdata")
public class BookDataLoader {

    private final BookRepository bookRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void loadBookTestData() {
        this.bookRepository.deleteAll();

        var book1 = Book.build("1234567891", "Northern Lights", "Lyra Silvertongue", 9.90);
        var book2 = Book.build("1234567892", "Polar Journey", "Iorek Polarson", 12.90);

        this.bookRepository.saveAll(List.of(book1, book2));
    }
}
