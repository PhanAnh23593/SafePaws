package com.zippy.auth.exception;
public class ResourceNotFoundException extends AppException {
    public ResourceNotFoundException() { super(ErrorCode.RESOURCE_NOT_FOUND); }
}
