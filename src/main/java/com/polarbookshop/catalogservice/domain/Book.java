package com.polarbookshop.catalogservice.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Book {

    private String isbn;

    private String title;

    private String author;

    private Double price;
}
