package com.utility.finmartcontact

interface IResponseSubcriber {

    abstract fun onSuccess(response: APIResponse, message: String)

    abstract fun onFailure(error: String)
}