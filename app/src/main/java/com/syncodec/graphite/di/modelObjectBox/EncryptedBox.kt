package com.syncodec.graphite.di.modelObjectBox

import com.syncodec.graphite.di.modelObjectBox.encryptable.EncryptedBoolean
import com.syncodec.graphite.utils.alice2.Alice2
import io.objectbox.annotation.BaseEntity


interface Encryptable<T> {
    fun encrypt(alice2: Alice2): T?
}

interface Decryptable<T> {
    fun decrypt(alice2: Alice2, default: T? = null): T?
}

abstract class DecryptedBox() : Encryptable<EncryptedBox> {
    abstract val id: Long
}

@BaseEntity
abstract class EncryptedBox() : Decryptable<DecryptedBox> {
    abstract var id: Long
    abstract var isLocked: EncryptedBoolean?
}
