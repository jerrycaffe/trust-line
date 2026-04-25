package trustline.appuser;

import trustline.appuser.dto.VerificationType
import java.util.*


object Utility {

    fun generateSixDigitsNumber(): Int {
        val random = Random()
        return 100_000 + random.nextInt(900_000)
    }

    fun welcomeEmailTemplate(user: String, otp: String): String {
        val htmlContent = """
            <div style='max-width:600px;margin:auto;background-color:#ffffff;border-radius:8px;
            box-shadow:0 0 5px rgba(0,0,0,0.05);padding:30px 20px;font-family:Segoe UI,sans-serif;'>
              <div style='text-align:center;padding-bottom:10px;'>
                <h1 style='color:#837AEF;font-size:24px;margin:0;'>Trustlne</h1>
              </div>
              <div style='font-size:16px;color:#333;line-height:1.6;padding:10px 0;'>
                <p>Hello, <strong>%s</strong></p>
                <p>Thank you for signing up on <strong>Trustlne</strong>! To complete your registration,
                 please verify your email with the OTP below:</p>
              </div>
              <div style='text-align:center;background-color:#CD8FFD22;color:#837AEF;
              font-size:28px;font-weight:bold;letter-spacing:8px;margin:20px auto;padding:15px 25px;
              border-radius:6px;width:fit-content;'>
                %s
              </div>
              <div style='font-size:16px;color:#333;line-height:1.6;padding:10px 0;'>
                <p>This code will expire in 10 minutes. If you didn’t request this, you can ignore this message.</p>
                <p>Welcome to Trustline — be ensured your security is totalling guarantee.</p>
              </div>
              <div style='font-size:13px;color:#777;text-align:center;margin-top:30px;'>
                &copy; 2025 Trustline. All rights reserved.
              </div>
            </div>
        """.trimIndent()

        return String.format(htmlContent, user, otp)
    }

    fun forgotPasswordEmailTemplate(user: String, otp: String): String {
        val forgotPasswordHtml = """
            <div style='max-width:600px;margin:auto;background-color:#ffffff;border-radius:8px;
            box-shadow:0 0 5px rgba(0,0,0,0.05);padding:30px 20px;font-family:Segoe UI,sans-serif;'>
              <div style='text-align:center;padding-bottom:10px;'>
                <h1 style='color:#837AEF;font-size:24px;margin:0;'>Trustlne</h1>
              </div>
              <div style='font-size:16px;color:#333;line-height:1.6;padding:10px 0;'>
                <p>Hello, <strong>%s</strong></p>
                <p>We received a request to reset your password on <strong>Trustlne</strong>.</p>
                <p>Please use the OTP below to continue with the password reset process:</p>
              </div>
              <div style='text-align:center;background-color:#CD8FFD22;color:#837AEF;
              font-size:28px;font-weight:bold;letter-spacing:8px;margin:20px auto;padding:15px 25px;
              border-radius:6px;width:fit-content;'>
                %s
              </div>
              <div style='font-size:16px;color:#333;line-height:1.6;padding:10px 0;'>
                <p>This OTP is valid for only 30 minutes.</p>
                <p>If you did not initiate this request, please ignore this message. Your account is still safe.</p>
              </div>
              <div style='font-size:13px;color:#777;text-align:center;margin-top:30px;'>
                &copy; 2025 Trustlne. All rights reserved.
              </div>
            </div>
        """.trimIndent()

        return String.format(forgotPasswordHtml, user, otp)
    }

    fun getEmailTemplate(
        verificationType: VerificationType,
        user: String,
        otp: String
    ): String =
        when (verificationType) {
            VerificationType.REGISTER ->
                welcomeEmailTemplate(user, otp)

            VerificationType.RESET_PASSWORD ->
                forgotPasswordEmailTemplate(user, otp)
        }

    fun inviteEmailTemplate(email: String, tempPassword: String, roleName: String): String {
        return """
            <div style='max-width:600px;margin:auto;background-color:#ffffff;border-radius:8px;
            box-shadow:0 0 5px rgba(0,0,0,0.05);padding:30px 20px;font-family:Segoe UI,sans-serif;'>
              <div style='text-align:center;padding-bottom:10px;'>
                <h1 style='color:#837AEF;font-size:24px;margin:0;'>Trustlne</h1>
              </div>
              <div style='font-size:16px;color:#333;line-height:1.6;padding:10px 0;'>
                <p>Hello, <strong>${email}</strong></p>
                <p>You have been invited to join <strong>Trustlne</strong> as a <strong>${roleName}</strong>.</p>
                <p>Your temporary password is:</p>
              </div>
              <div style='text-align:center;background-color:#CD8FFD22;color:#837AEF;
              font-size:22px;font-weight:bold;letter-spacing:4px;margin:20px auto;padding:15px 25px;
              border-radius:6px;width:fit-content;'>
                ${tempPassword}
              </div>
              <div style='font-size:16px;color:#333;line-height:1.6;padding:10px 0;'>
                <p>Please login and change your password immediately.</p>
              </div>
              <div style='font-size:13px;color:#777;text-align:center;margin-top:30px;'>
                &copy; 2026 Trustlne. All rights reserved.
              </div>
            </div>
        """.trimIndent()
    }

    fun caseAcknowledgementEmailTemplate(email: String, caseId: String): String {
        return """
            <div style='max-width:600px;margin:auto;background-color:#ffffff;border-radius:8px;
            box-shadow:0 0 5px rgba(0,0,0,0.05);padding:30px 20px;font-family:Segoe UI,sans-serif;'>
              <div style='text-align:center;padding-bottom:10px;'>
                <h1 style='color:#837AEF;font-size:24px;margin:0;'>Trustline</h1>
              </div>
              <div style='font-size:16px;color:#333;line-height:1.6;padding:10px 0;'>
                <p>Hello, <strong>${email}</strong></p>
                <p>Your case has been successfully reported and logged with reference <strong>${caseId}</strong>.</p>
                <p>We want to assure you that the <strong>confidentiality of your case is our utmost priority</strong>. 
                All information provided will be handled with the strictest confidence and your 
                <strong>privacy will be fully protected</strong> throughout the process.</p>
                <p>Our team will review your report and take appropriate action. You will be notified of any updates regarding your case.</p>
                <p>Thank you for trusting us with this matter.</p>
              </div>
              <div style='font-size:13px;color:#777;text-align:center;margin-top:30px;'>
                &copy; 2026 Trustline. All rights reserved.
              </div>
            </div>
        """.trimIndent()
    }
}

