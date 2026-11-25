package com.iustinmoraru.prtracker.domain

import com.iustinmoraru.prtracker.domain.validation.AuthValidator
import com.iustinmoraru.prtracker.domain.validation.FieldError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AuthValidatorTest {

    @Test
    fun `valid email passes`() {
        assertNull(AuthValidator.validateEmail("iustin@student.usv.ro"))
        assertNull(AuthValidator.validateEmail("  a.b+c@gmail.com  "))
    }

    @Test
    fun `empty or malformed email is rejected`() {
        assertEquals(FieldError.REQUIRED, AuthValidator.validateEmail(" "))
        assertEquals(FieldError.INVALID_EMAIL, AuthValidator.validateEmail("iustin@"))
        assertEquals(FieldError.INVALID_EMAIL, AuthValidator.validateEmail("iustin.gmail.com"))
    }

    @Test
    fun `sign-up password must be long and contain letters and digits`() {
        assertEquals(FieldError.PASSWORD_TOO_SHORT, AuthValidator.validatePassword("abc1", isSignUp = true))
        assertEquals(FieldError.PASSWORD_NEEDS_LETTER_AND_DIGIT, AuthValidator.validatePassword("abcdefgh", isSignUp = true))
        assertNull(AuthValidator.validatePassword("forta2024", isSignUp = true))
    }

    @Test
    fun `login password only needs to be present`() {
        assertEquals(FieldError.REQUIRED, AuthValidator.validatePassword("", isSignUp = false))
        assertNull(AuthValidator.validatePassword("abc", isSignUp = false))
    }

    @Test
    fun `name length is checked`() {
        assertEquals(FieldError.NAME_TOO_SHORT, AuthValidator.validateName("A"))
        assertEquals(FieldError.NAME_TOO_LONG, AuthValidator.validateName("A".repeat(41)))
        assertNull(AuthValidator.validateName("Iustin Moraru"))
    }
}
