package com.goldenowl.ecommerce.utils

import android.util.Patterns

class TextValidation {
    companion object {

        private val TAG: String? = "TextValidation"

        fun isValidEmail(email: String): Boolean {
            return Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }

        // todo NO special characters
        fun validateName(name: String): String {
            if (name.isEmpty()) {
                return "Required Field!"
            } else {
                return ""
            }
        }

        fun validatePassword(password: String): String {
            if (password.isEmpty()) {
                return "Required Field!"
            } else if (password.length < 6) {
                return "Password can't be less than 6"

//        } else if (!isStringContainNumber(edtPassword.text.toString())) {
//            inputLayoutPassword.error = "Required at least 1 digit"
//            edtPassword.requestFocus()
//            return false
//        } else if (!isStringLowerAndUpperCase(edtPassword.text.toString())) {
//            inputLayoutPassword.error =
//                "Password must contain upper and lower case letters"
//            edtPassword.requestFocus()
//            return false
//        } else if (!isStringContainSpecialCharacter(edtPassword.text.toString())) {
//            inputLayoutPassword.error = "1 special character required"
//            edtPassword.requestFocus()
//            return false
            } else {
                return ""
//            inputLayoutPassword.isErrorEnabled = false
            }
        }

        /**
         * 1) field must not be empty
         * 2) text should matches email address format
         */
        fun validateEmail(email: String): String {
            return if (email.isEmpty()) {
                "Required Field!"

            } else if (!isValidEmail(email)) {
                "Invalid Email!"
            } else {
                ""
            }
        }

        fun validateDoB(dob: String): String {
            if (dob.isEmpty()) {
                return "Required Field!"
            }
            return ""
        }

        fun validateRePassword(password: String, rePassword: String): String {
            return if (password == rePassword) "" else "Password mismatch"
        }
    }
}