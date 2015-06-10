package org.feenaboccles.kindlomist.download;

import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;
import org.feenaboccles.kindlomist.valid.Validator;
import org.hibernate.validator.constraints.Length;

import javax.validation.ValidationException;
import javax.validation.constraints.Pattern;

/**
 * Represents a validated user-name for an online service
 */
@Value() @Accessors(fluent=true)
public final class UserName {

    @NonNull @Length(min=3, max=30) @Pattern(regexp="[a-zA-Z0-9_\\-]+", message="Username contains invalid characters")
    String value;

    private UserName (String value) {
        this.value = value;
    }

    public static UserName of (String value) throws ValidationException {
        return new UserName(value).validate();
    }

    public UserName validate() throws ValidationException {
        Validator.INSTANCE.validate(this, "user-name");
        return this;
    }
}
