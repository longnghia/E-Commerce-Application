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

        fun validateCardName(name: String): String {
            return validateName(name)
        }

        fun validateCardNumber(cardNumber: String): String {
            if (cardNumber.isBlank()) {
                return "Field required"
            }
            if (cardNumber[0] != '4' && cardNumber[0] != '5') {
                return "Card number must start with 4 or 5"
            }
            if (cardNumber.length < 19) {
                return "Card number invalid"
            }
            return ""
        }

        fun validateCardCvv(cvv: String): String {
            if (cvv.isEmpty())
                return "Field required"
            if (cvv.length < 3)
                return "Invalid CVV"
            return ""
        }

        fun validateCardExpireDate(expireDate: String): String {
            if (expireDate.isEmpty())
                return "Field required"
            val date = expireDate.split("/")
            if(date[0].isEmpty())
                return "Require month"
            val month = date[0].toInt()
            if(date.size<2)
                return "Require month and year"
            if(date[1].isEmpty())
                return "Require year"
            val year = date[1].toInt()
            if (year <= 0 || month <= 0 || month > 12)
                return "Invalid date"
            return ""
        }
    }
}