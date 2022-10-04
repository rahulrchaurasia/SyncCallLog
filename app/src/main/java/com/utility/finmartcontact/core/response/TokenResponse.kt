package com.utility.finmartcontact.core.response

import com.utility.finmartcontact.APIResponse

data class TokenResponse(
    val MasterData: List<TokenEntity>,

): APIResponse()

data class TokenEntity(
    val Message: String,
    val SavedStatus: Int
)