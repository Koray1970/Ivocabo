package com.serko.ivocabo.data

class FormActionResult<T>(t: T) {
    var resultFlag: FormActionResultFlag = FormActionResultFlag.Failed
    var result = t
    var error: FormActionError? = null
}

class FormActionError {
    var code: String? = null
    var exception: String? = null
}

enum class FormActionResultFlag(var flag: Int) {
    Failed(0), Success(1)
}