package com.goldenowl.ecommerce.utils

import android.content.Context
import android.util.Base64
import android.util.Log
import com.goldenowl.ecommerce.R
import java.io.BufferedReader
import java.io.StringReader
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.*
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.RSAPrivateKeySpec
import java.security.spec.RSAPublicKeySpec
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException

/**
 * Created by Awesometic
 * It's encrypt returns Base64 encoded, and also decrypt for Base64 encoded cipher
 * references: http://stackoverflow.com/questions/12471999/rsa-encryption-decryption-in-android
 */
class RSACipher(private val context: Context) {
    private var publicKey: PublicKey? = null
    private var privateKey: PrivateKey? = null
    private lateinit var encryptedBytes: ByteArray
    private lateinit var decryptedBytes: ByteArray
    private lateinit var cipher: Cipher
    private lateinit var cipher1: Cipher
    private lateinit var encrypted: String
    private lateinit var decrypted: String

    @Throws(
        NoSuchAlgorithmException::class,
        NoSuchPaddingException::class,
        InvalidKeyException::class,
        IllegalBlockSizeException::class,
        BadPaddingException::class
    )
    private fun generateKeyPair() {
        privateKey = stringToPrivateKey(context.getString(R.string.private_key))
        publicKey = privateKey?.let { getPublicKey(it) }
    }

    /**
     * Encrypt plain text to RSA encrypted and Base64 encoded string
     *
     * @param args
     * args[0] should be plain text that will be encrypted
     * If args[1] is be, it should be RSA public key to be used as encrypt public key
     * @return a encrypted string that Base64 encoded
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    @Throws(
        NoSuchAlgorithmException::class,
        NoSuchPaddingException::class,
        InvalidKeyException::class,
        IllegalBlockSizeException::class,
        BadPaddingException::class
    )
    fun encrypt(vararg args: Any): String {
        val plain = args[0] as String
        val rsaPublicKey: PublicKey? = if (args.size == 1) {
            publicKey
        } else {
            args[1] as PublicKey
        }
        cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, rsaPublicKey)
        encryptedBytes = cipher.doFinal(plain.toByteArray(StandardCharsets.UTF_8))
        encrypted = Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
        return encrypted
    }

    @Throws(
        NoSuchAlgorithmException::class,
        NoSuchPaddingException::class,
        InvalidKeyException::class,
        IllegalBlockSizeException::class,
        BadPaddingException::class
    )
    fun decrypt(result: String?): String {
        cipher1 = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding")
        cipher1.init(Cipher.DECRYPT_MODE, privateKey)
        decryptedBytes = cipher1.doFinal(Base64.decode(result, Base64.DEFAULT))
        decrypted = String(decryptedBytes)
        return decrypted
    }

    companion object {

        fun stringToPrivateKey(private_key: String?): PrivateKey? {
            try {
                // Read in the key into a String
                val pkcs8Lines = StringBuilder()
                val rdr = BufferedReader(StringReader(private_key))
                var line: String?
                while (rdr.readLine().also { line = it } != null) {
                    pkcs8Lines.append(line)
                }
                // Remove the "BEGIN" and "END" lines, as well as any whitespace
                var pkcs8Pem = pkcs8Lines.toString()
                pkcs8Pem = pkcs8Pem.replace("-----BEGIN PRIVATE KEY-----", "")
                pkcs8Pem = pkcs8Pem.replace("-----END PRIVATE KEY-----", "")
                pkcs8Pem = pkcs8Pem.replace("\\s+".toRegex(), "")

                // Base64 decode the result
                val pkcs8EncodedBytes = Base64.decode(pkcs8Pem, Base64.DEFAULT)

                // extract the private key
                val keySpec = PKCS8EncodedKeySpec(pkcs8EncodedBytes)
                val kf = KeyFactory.getInstance("RSA")
                return kf.generatePrivate(keySpec)
            } catch (e: Exception) {
                Log.e(TAG, "stringToPrivateKey: ", e)
            }
            return null
        }

        fun getPublicKey(privateKey: PrivateKey): PublicKey? {
            val kf = KeyFactory.getInstance("RSA")
            val priv: RSAPrivateKeySpec = kf.getKeySpec(privateKey, RSAPrivateKeySpec::class.java)
            val keySpec = RSAPublicKeySpec(priv.modulus, BigInteger.valueOf(65537))
            return kf.generatePublic(keySpec)
        }

        const val TAG = "RSACipher"
    }

    init {
        generateKeyPair()
    }
}