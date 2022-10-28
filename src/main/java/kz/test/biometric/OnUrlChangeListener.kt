package kz.test.biometric

interface OnUrlChangeListener {

    fun onResultSuccess(result: String)

    fun onResultFailure(reason: String)
}