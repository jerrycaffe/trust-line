package trustline.appuser.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class OtpRequest {
    @NotNull(message = "verification id is required")
    private String verificationId;
    @NotNull(message = "user id is required")
    private UUID userId;
}
