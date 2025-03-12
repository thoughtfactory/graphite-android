package com.syncodec.graphite.di.modelObjectBox

import com.syncodec.graphite.utils.alice2.Alice2
import io.objectbox.annotation.BaseEntity
import io.objectbox.annotation.Id


interface Encryptable<T> {
    fun encrypt(alice2: Alice2): T
}

interface Decryptable<T> {
    fun decrypt(alice2: Alice2): T
}

@BaseEntity
abstract class EncryptedBox : Decryptable<DecryptedBox> {
    @Id
    var id: Long = 0
    abstract val decryptedBox: DecryptedBox?

    abstract override fun decrypt(alice2: Alice2): DecryptedBox
}

@BaseEntity
abstract class DecryptedBox : Encryptable<EncryptedBox> {
    abstract val encryptedBox: EncryptedBox?
    abstract override fun encrypt(alice2: Alice2): EncryptedBox
}
