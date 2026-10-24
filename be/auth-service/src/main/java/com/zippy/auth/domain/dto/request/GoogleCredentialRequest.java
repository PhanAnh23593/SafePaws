package com.zippy.auth.domain.dto.request;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
@Schema(description = "Technical Google Identity exchange; not a field of any authentication screen")
public record GoogleCredentialRequest(
    @NotBlank @Size(max = 16384)
    @Schema(description = "ID token returned by Google Identity Services", accessMode = Schema.AccessMode.WRITE_ONLY)
    String credential) {
    @Override public String toString() { return "GoogleCredentialRequest[redacted]"; }
}
