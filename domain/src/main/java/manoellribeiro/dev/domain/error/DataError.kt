package manoellribeiro.dev.domain.error

/** Every failure a repository can report. Sealed so the UI handles each case exhaustively. */
sealed class DataError(cause: Throwable? = null) : Exception(cause) {
    /** The request failed in transit. For a payout, the outcome is unknown: it may have gone through. */
    class Network(cause: Throwable) : DataError(cause)

    /** The server failed (5xx). */
    class ServiceUnavailable : DataError()

    /** The merchant's balance can't cover the payout. */
    class InsufficientFunds : DataError()

    /** Anything else: an unexpected status, a malformed response or a storage failure. */
    class Unexpected(cause: Throwable? = null) : DataError(cause)
}
