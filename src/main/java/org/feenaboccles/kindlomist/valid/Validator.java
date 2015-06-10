package org.feenaboccles.kindlomist.valid;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidationException;
import javax.validation.ValidatorFactory;


/**
 * A singleton exposing in a non-threadsafe way access to a single static validator.
 * @author bryanfeeney
 *
 */
public enum Validator {
	INSTANCE;
	
	private final javax.validation.Validator validator;
	
	Validator()
	{
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}
	
	/**
	 * Checks if the given object is valid. If not, thrown an {@link IllegalArgumentException}
	 * with a multi-line error message listing all the validation failures.
	 * @param object the object to inspect
	 * @param name its name, used in error messages
	 * @throws IllegalArgumentException if the object is invalid
	 */
	public <T> void validate (T object, String name) throws ValidationException
	{	StringBuilder failures = new StringBuilder(0); // start optimistically.
		
		Set<ConstraintViolation<T>> violations = validator.validate(object);
		for (ConstraintViolation<T> violation : violations) {
			String propertyPath = violation.getPropertyPath().toString();
			String message      = violation.getMessage();
			failures.append("\tInvalid value for: '")
					.append(propertyPath).append("': ")
					.append(message) .append('\n');
		}
		
		if (failures.length() > 0)
			throw new ValidationException ("Invalid " + name + ":\n" + failures.toString());
	}
}
