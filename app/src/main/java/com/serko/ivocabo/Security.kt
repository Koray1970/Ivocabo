package com.serko.ivocabo

import android.os.Build
import androidx.annotation.RequiresApi
import java.security.MessageDigest
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class Security {

    @RequiresApi(Build.VERSION_CODES.O)
    fun encrypt(strToEncrypt: String): String {

        val encoder: Base64.Encoder = Base64.getEncoder()
        return encoder.encodeToString(strToEncrypt.toByteArray())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun decrypt(dataToDecrypt: String): String {
        val decoder: Base64.Decoder = Base64.getDecoder()
        return String(decoder.decode(dataToDecrypt))
    }



}