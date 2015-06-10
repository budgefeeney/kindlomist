package org.feenaboccles.kindlomist.download;

import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;
import org.feenaboccles.kindlomist.valid.Validator;
import org.hibernate.validator.constraints.Length;

import javax.validation.ValidationException;
import javax.validation.constraints.Pattern;

/**
 * Represents a validated password.
 */
@Value() @Accessors(fluent=true)
public final class Password {

    @NonNull @Length(min=3, max=30)
    @Pattern(regexp="\\S+", message="Password contains invalid characters")
    String value;

    private Password(String value) {
        this.value = value;
    }

    public static Password of (String value) throws ValidationException {
        return new Password(value).validate();
    }

    public Password validate() throws ValidationException {
        Validator.INSTANCE.validate(this, "password");
        return this;
    }

    public String value() {
        return value;
    }
}
