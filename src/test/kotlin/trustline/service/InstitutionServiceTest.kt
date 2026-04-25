package trustline.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.dao.DataIntegrityViolationException
import trustline.config.exception.DuplicateException
import trustline.institution.dto.CreateInstitutionDto
import trustline.institution.model.InstitutionModel
import trustline.institution.repository.InstitutionRepository
import trustline.institution.service.InstitutionServiceImpl
import java.util.*
import kotlin.test.assertNotNull

class InstitutionServiceTest {
    private val institutionRepository = mockk<InstitutionRepository>()
    private val jwtConfigService = mockk<trustline.config.security.JWTConfigService>()
    private val institutionService = InstitutionServiceImpl(institutionRepository, jwtConfigService)


    val institutionModel = InstitutionModel(UUID.randomUUID(), "TEST", "080000000000", "address")
    val createInstitutionReq = CreateInstitutionDto("TEST", "0800000000000", "adress")

    @Test
    fun `Create Institution Should Throw DuplicateException When previous institution exists by name`() {
        every { institutionRepository.save(any<InstitutionModel>()) } throws DataIntegrityViolationException("Institution with the name exist")
        assertThrows<DuplicateException> { institutionService.createInstitution(createInstitutionReq) }
    }


    @Test
    fun `Create Institution Should be successful When all details are correct`() {
        every { institutionRepository.save(any()) } returns institutionModel
        val response = institutionService.createInstitution(createInstitutionReq)
        assertNotNull(response)
        verify(atMost = 1) { institutionRepository.save(any()) }
    }
}