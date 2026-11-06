package manoellribeiro.dev.data.remote.model

import kotlinx.serialization.Serializable
import manoellribeiro.dev.domain.model.Currency

/** Strict on purpose: an unknown currency fails the response rather than showing money wrongly. */
@Serializable
internal enum class RemoteCurrency { GBP, EUR }

internal fun RemoteCurrency.toDomain() = when (this) {
    RemoteCurrency.GBP -> Currency.GBP
    RemoteCurrency.EUR -> Currency.EUR
}

internal fun Currency.toRemote() = when (this) {
    Currency.GBP -> RemoteCurrency.GBP
    Currency.EUR -> RemoteCurrency.EUR
}
