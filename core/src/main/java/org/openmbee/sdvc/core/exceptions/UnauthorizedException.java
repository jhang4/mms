package org.openmbee.sdvc.core.exceptions;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends SdvcException {

    public UnauthorizedException(Object body) {
        super(HttpStatus.UNAUTHORIZED, body);
    }

}
