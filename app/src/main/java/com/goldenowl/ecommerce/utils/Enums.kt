package com.goldenowl.ecommerce.utils

import com.goldenowl.ecommerce.R

enum class BaseLoadingStatus {
    NONE,
    LOADING,
    DONE,
    SUCCEEDED,
    FAILED,

}

enum class SortType {
    POPULAR, NEWEST, REVIEW, PRICE_INCREASE, PRICE_DECREASE
}

val authErrors = mapOf<String, Int>(
    "ERROR_INVALID_CUSTOM_TOKEN" to R.string.error_login_custom_token,
    "ERROR_CUSTOM_TOKEN_MISMATCH" to R.string.error_login_custom_token_mismatch,
    "ERROR_INVALID_CREDENTIAL" to R.string.error_login_credential_malformed_or_expired,
    "ERROR_INVALID_EMAIL" to R.string.error_login_invalid_email,
    "ERROR_WRONG_PASSWORD" to R.string.error_login_wrong_password,
    "ERROR_USER_MISMATCH" to R.string.error_login_user_mismatch,
    "ERROR_REQUIRES_RECENT_LOGIN" to R.string.error_login_requires_recent_login,
    "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" to R.string.error_login_accounts_exits_with_different_credential,
    "ERROR_EMAIL_ALREADY_IN_USE" to R.string.error_login_email_already_in_use,
    "ERROR_CREDENTIAL_ALREADY_IN_USE" to R.string.error_login_credential_already_in_use,
    "ERROR_USER_DISABLED" to R.string.error_login_user_disabled,
    "ERROR_USER_TOKEN_EXPIRED" to R.string.error_login_user_token_expired,
    "ERROR_USER_NOT_FOUND" to R.string.error_login_user_not_found,
    "ERROR_INVALID_USER_TOKEN" to R.string.error_login_invalid_user_token,
    "ERROR_OPERATION_NOT_ALLOWED" to R.string.error_login_operation_not_allowed,
    "ERROR_WEAK_PASSWORD" to R.string.error_login_password_is_weak
)