package com.polarbookshop.catalogservice.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;

@Data
@AllArgsConstructor
public class Book {

    @NotBlank(message = "The book ISBN must be defined")
    @Pattern(regexp = "^([0-9]{10}|[0-9]{13})$", message = "The ISBN format is not valid")
    private String isbn;

    @NotBlank(message = "The book title must be defined")
    private String title;

    @NotBlank(message = "The book author must be defined")
    private String author;

    @NotNull(message = "The book price must be defined")
    @Positive(message = "The book price must be greater than zero")
    private Double price;
}
