package com.zippy.auth.exception;

/** Session revocation must commit even though the replaying request receives 401. */
public final class RefreshTokenReuseException extends AppException {
    public RefreshTokenReuseException() { super(ErrorCode.AUTH_REVOKED_REFRESH_TOKEN); }
}
