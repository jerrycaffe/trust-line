package trustline.appuser.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ResendOtpRequest {
    @NotNull(message = "Previous OTP ID is required")
    private UUID prevOtpId;
}
