package com.telekom.citykey.view.registration

import androidx.core.util.PatternsCompat
import com.telekom.citykey.InstantTaskExecutorExtension
import com.telekom.citykey.R
import com.telekom.citykey.RxImmediateSchedulerExtension
import com.telekom.citykey.custom.views.inputfields.FieldValidation
import com.telekom.citykey.domain.repository.UserRepository
import com.telekom.citykey.view.user.registration.RegFields
import com.telekom.citykey.view.user.registration.RegistrationViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(RxImmediateSchedulerExtension::class)
@ExtendWith(InstantTaskExecutorExtension::class)
class RegistrationViewModelTest {

    private val userRepository: UserRepository = mockk(relaxed = true)
    private lateinit var registrationViewModel: RegistrationViewModel
    private val hints = arrayOf(
        "minimum 8 characters",
        "at least one symbol",
        "at least one digit"
    )
    private val formattedHints: String =
        "Please use: minimum 8 characters, at least one symbol, at least one digit"

    @BeforeEach
    fun setUp() {
        registrationViewModel =
            RegistrationViewModel(userRepository)
        registrationViewModel.onPasswordHintsResourcesReceived(hints, formattedHints)
    }

    @Test
    fun `Test validate password if length less than 7 `() {
        registrationViewModel.onPasswordTextChanged("pass")
        assertEquals(
            registrationViewModel.inputValidation.value,
            RegFields.PASSWORD to FieldValidation(
                FieldValidation.ERROR, null,
                R.string.r_001_registration_error_password_too_weak
            )
        )
    }

    @Test
    fun `Test validate password if length greater than 7 `() {
        registrationViewModel.onPasswordTextChanged("password")
        assertEquals(
            registrationViewModel.inputValidation.value,
            RegFields.PASSWORD to FieldValidation(
                FieldValidation.ERROR, null,
                R.string.r_001_registration_error_password_too_weak
            )
        )
    }

    @Test
    fun `Test validate password if length greater than 7 contains Special Symbols `() {
        registrationViewModel.onPasswordTextChanged("password@")
        assertEquals(
            registrationViewModel.inputValidation.value,
            RegFields.PASSWORD to FieldValidation(
                FieldValidation.ERROR, null,
                R.string.r_001_registration_error_password_too_weak
            )
        )
    }

    @Test
    fun `Test validate password if password and email are same `() {
        registrationViewModel.onEmailUpdated("password@0", "password@0")
        registrationViewModel.onPasswordTextChanged("password@0")
        assertEquals(
            registrationViewModel.inputValidation.value,
            RegFields.PASSWORD to FieldValidation(
                FieldValidation.ERROR, null,
                R.string.r_001_registration_error_password_email_same
            )
        )
    }

    @Test
    fun `Test validate password if length greater than 7 contains Special Symbols and digits`() {
        registrationViewModel.onPasswordTextChanged("password@0")
        assertEquals(
            registrationViewModel.inputValidation.value,
            RegFields.PASSWORD to FieldValidation(FieldValidation.SUCCESS, "")
        )
    }

    @Test
    fun `Test on post code ready if empty`() {
        registrationViewModel.onPostCodeReady("")
        assertEquals(
            registrationViewModel.inputValidation.value,
            RegFields.POSTAL_CODE to FieldValidation(
                FieldValidation.ERROR,
                null,
                R.string.r_001_registration_error_empty_field
            )
        )
    }

    @Test
    fun `Test on post code ready if not empty & length less than 5`() {
        registrationViewModel.onPostCodeReady("1111")
        assertEquals(
            registrationViewModel.inputValidation.value,
            RegFields.POSTAL_CODE to FieldValidation(
                FieldValidation.ERROR,
                null,
                R.string.r_001_registration_error_incorrect_postcode
            )
        )
    }

    @Test
    fun `Test on email ready if empty`() {
        registrationViewModel.onEmailReady("")
        assertEquals(
            registrationViewModel.inputValidation.value,
            RegFields.EMAIL to FieldValidation(
                FieldValidation.ERROR,
                null,
                R.string.r_001_registration_error_empty_field
            )
        )
    }

    @Disabled
    @Test
    fun `Test on email ready if non-empty and invalid email`() {
        mockkObject(PatternsCompat.EMAIL_ADDRESS)
        every { PatternsCompat.EMAIL_ADDRESS.matcher(any()).matches() } returns false

        registrationViewModel.onEmailReady("abc@")
        assertEquals(
            registrationViewModel.inputValidation.value,
            RegFields.EMAIL to FieldValidation(
                FieldValidation.ERROR,
                null,
                R.string.r_001_registration_error_incorrect_email
            )
        )
    }

    @Test
    fun `Test validate second password if second password empty`() {
        registrationViewModel.onSecondPasswordTextChanged("password@0" to "")
        assertEquals(
            registrationViewModel.inputValidation.value,
            RegFields.SEC_PASSWORD to
                    FieldValidation(
                        FieldValidation.ERROR, null,
                        R.string.r_001_registration_error_empty_field
                    )
        )
    }

    @Test
    fun `Test validate second password if non-empty and matches first and second password`() {
        registrationViewModel.onSecondPasswordTextChanged("password@0" to "password@0")
        assertEquals(
            registrationViewModel.inputValidation.value,
            RegFields.SEC_PASSWORD to FieldValidation(FieldValidation.OK, null)
        )
    }
}
