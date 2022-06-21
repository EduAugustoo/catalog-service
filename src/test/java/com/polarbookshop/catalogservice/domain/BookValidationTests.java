package com.polarbookshop.catalogservice.domain;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class BookValidationTests {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void whenAllFieldsCorrectThenValidationSucceeds() {
        Book book = Book.build("1234567890", "Title", "Author", 9.90, "Polarsophia");
        Set<ConstraintViolation<Book>> violations = validator.validate(book);
        assertThat(violations).isEmpty();
    }

    @Test
    public void whenIsbnNotDefinedThenValidationFails() {
        Book book = Book.build("", "Title", "Author", 9.90, "Polarsophia");
        Set<ConstraintViolation<Book>> violations = validator.validate(book);
        assertThat(violations).hasSize(2);

        var constraintViolationMessages =
                violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.toList());

        assertThat(constraintViolationMessages)
                .contains("The book ISBN must be defined")
                .contains("The ISBN format is not valid");
    }

    @Test
    public void whenIsbnDefinedButIncorretThenValidationFails() {
        Book book = Book.build("a234567890", "Title", "Author", 9.90, "Polarsophia");
        Set<ConstraintViolation<Book>> violations = validator.validate(book);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("The ISBN format is not valid");
    }

    @Test
    public void whenTitleNotDefinedThenValidationFails() {
        Book book = Book.build("1234567890", "", "Author", 9.90, "Polarsophia");
        Set<ConstraintViolation<Book>> violations = validator.validate(book);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("The book title must be defined");
    }

    @Test
    public void whenAuthorNotDefinedThenValidationFails() {
        Book book = Book.build("1234567890", "Title", "", 9.90, "Polarsophia");
        Set<ConstraintViolation<Book>> violations = validator.validate(book);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("The book author must be defined");
    }

    @Test
    public void whenPriceNotDefinedThenValidationFails() {
        Book book = Book.build("1234567890", "Title", "Author", null, "Polarsophia");
        Set<ConstraintViolation<Book>> violations = validator.validate(book);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("The book price must be defined");
    }

    @Test
    public void whenPriceDefinedButZeroThenValidationFails() {
        Book book = Book.build("1234567890", "Title", "Author", 0.0, "Polarsophia");
        Set<ConstraintViolation<Book>> violations = validator.validate(book);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("The book price must be greater than zero");
    }

    @Test
    public void whenPriceDefinedButNegativeThenValidationFails() {
        Book book = Book.build("1234567890", "Title", "Author", -9.90, "Polarsophia");
        Set<ConstraintViolation<Book>> violations = validator.validate(book);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("The book price must be greater than zero");
    }
}
