package trustline.appuser.controller

import org.springframework.http.MediaType
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import trustline.appuser.dto.UpdateProfileRequest
import trustline.appuser.model.ProfileResponseDto
import trustline.appuser.service.UserService

@RestController
@RequestMapping("api/v1/profile")
class ProfileController(
    private val userService: UserService
) {

    @GetMapping
    fun getProfile(): ProfileResponseDto {
        return userService.getProfile()
    }

    @PutMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun updateProfile(
        @Validated @RequestPart("profile") request: UpdateProfileRequest,
        @RequestPart("profileImage", required = false) profileImage: MultipartFile?
    ): ProfileResponseDto {
        return userService.updateProfile(request, profileImage)
    }
}
