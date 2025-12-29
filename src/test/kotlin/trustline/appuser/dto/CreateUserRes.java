package trustline.appuser.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import trustline.appuser.model.User;

import java.util.UUID;

@Builder
@Data
@AllArgsConstructor
public class CreateUserRes {
    private User user;
    private UUID otpId;
}
