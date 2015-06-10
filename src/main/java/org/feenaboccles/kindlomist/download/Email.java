package org.feenaboccles.kindlomist.download;

import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;
import org.feenaboccles.kindlomist.valid.Validator;
import org.hibernate.validator.constraints.Length;

import javax.validation.ValidationException;

/**
 * Represents a validated user-name for an online service
 */
@Value() @Accessors(fluent=true)
public final class Email {

    @NonNull @Length(min=3, max=30) @org.hibernate.validator.constraints.Email
    String value;

    private Email(String value) {
        this.value = value;
    }

    public static Email of (String value) throws ValidationException {
        return new Email(value).validate();
    }

    public Email validate() throws ValidationException {
        Validator.INSTANCE.validate(this, "user-name");
        return this;
    }
}
