package trustline.appuser.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class ForgotPasswordRes {
private UUID otpId;

}
