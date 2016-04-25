/*
 * Copyright (c) 2016, Christoph Engelbert (aka noctarius) and
 * contributors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.noctarius.lazylog.util;

/**
 * The <tt>Validate</tt> class supports convenience validations to be
 * applied. It is fully Java 8 lambda capable and heavily uses them.
 */
public final class Validate {

    private static final String MESSAGE_PARAM_NOT_NULL = "%s must not be null";

    public static void validate(MessageBuilder messageBuilder, Validation validation, ExceptionBuilder exceptionBuilder) {
        if (!validation.validate()) {
            Exception exception = exceptionBuilder.build(messageBuilder.build());
            if (exception instanceof RuntimeException) {
                throw (RuntimeException) exception;
            }
            throw new RuntimeException(exception);
        }
    }

    public static void notNull(String paramName, Object value) {
        validate(message(MESSAGE_PARAM_NOT_NULL, paramName), () -> value != null, NullPointerException::new);
    }

    private Validate() {
    }

    private static MessageBuilder message(String message, Object param) {
        return () -> String.format(message, param);
    }

    /**
     * The <tt>Validation</tt> interface is used to implement internal and
     * external validations based on Java 8 lambdas.
     */
    public interface Validation {

        /**
         * This method implements the validation logic and returns <tt>true</tt>
         * if the validation passed or <tt>false</tt> if not.
         *
         * @return true if validation passed, otherwise false
         */
        boolean validate();
    }

    /**
     * The <tt>MessageBuilder</tt> interface is used to delay creation of
     * exception messages up to the point where a validation really failed.
     * This prevents heavy string concatinations or other sort of costly
     * operations to be as lazy as possible and to only happen if really
     * necessary.
     */
    public interface MessageBuilder {

        /**
         * Generates the content and builds the exception message.
         *
         * @return the generated exception message
         */
        String build();
    }

    /**
     * The <tt>ExceptionBuilder</tt> interface is used to delay creation of
     * exception up to the point where a validation really failed and a message
     * was created.
     */
    public interface ExceptionBuilder {

        /**
         * Generates the exception using the given exceptional message
         *
         * @return the exceptional message
         */
        Exception build(String message);
    }

}
