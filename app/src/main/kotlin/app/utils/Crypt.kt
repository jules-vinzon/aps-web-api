package app.utils

import app.App
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.bouncycastle.openssl.PEMWriter
import org.bouncycastle.util.io.pem.PemReader
import org.json.JSONObject
import java.io.StringReader
import java.io.StringWriter
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import org.bouncycastle.util.encoders.Base64
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec


class Crypt {
    fun decrypt(breadcrumb: Breadcrumb, privateKeyPem: String, encryptedString: String?): JSONObject {
        breadcrumb.log("START TO DECRYPT")
        breadcrumb.log("ENCRYPTED REQUEST BODY $encryptedString")
        val response = JSONObject()
        try {
            val privateKey = privateKeyPem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("\n", "")

            breadcrumb.log("START READ PEM KEY")
            val privateKeyBytes = java.util.Base64.getDecoder().decode(privateKey)
            val keyFactory = KeyFactory.getInstance("RSA")
            val privateKeySpec = PKCS8EncodedKeySpec(privateKeyBytes)
            val privateRsaKey = keyFactory.generatePrivate(privateKeySpec)
            breadcrumb.log("DONE READ PEM KEY")

            breadcrumb.log("START DECRYPTING STRING")
            val cipher = Cipher.getInstance("RSA/ECB/OAEPPadding")
            cipher.init(Cipher.DECRYPT_MODE, privateRsaKey)

            val decryptedBytes = cipher.doFinal(java.util.Base64.getDecoder().decode(encryptedString))
            val decrypted = String(decryptedBytes, Charsets.UTF_8)
            breadcrumb.log("DONE DECRYPTING STRING")

            response.put("decrypted", decrypted)
            response.put("valid", true)
        } catch (e: Exception) {
            response.put("valid", false)
            e.printStackTrace()
        }
        breadcrumb.log("DONE TO DECRYPT")
        return response
    }

    fun encrypt(publicKeyPem: String?, plainText: String): String? {
        val pemReader = PemReader(StringReader(publicKeyPem))
        val content = pemReader.readPemObject().content
        val keySpec = X509EncodedKeySpec(content)
        val kf: KeyFactory = KeyFactory.getInstance("RSA")
        val publicKeySecret: PublicKey = kf.generatePublic(keySpec)
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, publicKeySecret)
        val encryptedBytes = cipher.doFinal(plainText.toByteArray())
        return String(Base64.encode(encryptedBytes))
    }


    fun generateToken(reqId: String, username: String): String {
        val secretKey = App.dotenv["SECRET_KEY"].toString()
        val now = Date()
        val expiryDate = Date(now.time + 86400000) // 1 day

        val token = Jwts.builder()
            .setSubject(username)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(SignatureAlgorithm.HS512, secretKey)
            .compact()
        return token
    }

    fun validateJwtToken(token: String): Boolean {
        val secretKey = App.dotenv["SECRET_KEY"].toString()
        return try {
            val claims = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .body
            claims.expiration.after(Date())
        } catch (e: Exception) {
            false // Token is invalid
        }
    }

    fun generateKeypair(): JSONObject {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        val keyPair = keyPairGenerator.generateKeyPair()

        val keyspecPrivate = PKCS8EncodedKeySpec(keyPair.private.encoded)
        val sb = StringBuilder()
        sb.append("-----BEGIN PRIVATE KEY-----\n")
        sb.append(String(Base64.encode(keyspecPrivate.encoded)) + "\n")
        sb.append("-----END PRIVATE KEY-----\n")

        val privPemS = sb.toString()

        val keyspecPublic = PKCS8EncodedKeySpec(keyPair.public.encoded)
        val sbPub = StringBuilder()
        sbPub.append("-----BEGIN PUBLIC KEY-----\n")
        sbPub.append(String(Base64.encode(keyspecPublic.encoded)) + "\n")
        sbPub.append("-----END PUBLIC KEY-----\n")

        val publicWrite = StringWriter()
        val publicPemWriter = PEMWriter(publicWrite)
        publicPemWriter.writeObject(keyPair.public)
        publicPemWriter.close()
        publicPemWriter.close()
        publicWrite.close()


        val keyObj = JSONObject()
        keyObj.put("private",privPemS )
        keyObj.put("public",publicWrite.toString())
        return keyObj

    }

    fun hashPassword(message: String): String {
        val secretKey = App.dotenv["SECRET_KEY"].toString()
        val hmacSha256 = Mac.getInstance("HmacSHA256")
        val secretKeySpec = SecretKeySpec(secretKey.toByteArray(), "HmacSHA256")
        hmacSha256.init(secretKeySpec)

        val hmacBytes = hmacSha256.doFinal(message.toByteArray())
        return java.util.Base64.getEncoder().encodeToString(hmacBytes)
    }

}