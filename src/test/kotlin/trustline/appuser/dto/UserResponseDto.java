package trustline.appuser.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import trustline.appuser.model.User;

import java.util.UUID;

@Setter
@Getter
@Builder
public class UserResponseDto {

    private UUID id;
    private  String email;
    private String phoneNumber;
    private Status status;
    private UUID otpId;
    private boolean emailVerified;

    public static UserResponseDto fromUser(User user, UUID otpId){
        return UserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .status(user.getStatus())
                .emailVerified(user.isAccountVerified())
                .otpId(otpId)
                .build();
    }
    public static UserResponseDto fromUser(User user){
        return UserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .status(user.getStatus())
                .emailVerified(user.isAccountVerified())
                .build();
    }
}
