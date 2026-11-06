package manoellribeiro.dev.domain.usecase

class ValidateIbanUseCase {

    /** A format check only: it doesn't verify the IBAN's check digits. */
    operator fun invoke(iban: String): Boolean =
        iban.length in MIN_LENGTH..MAX_LENGTH && iban.take(2).all { it in 'A'..'Z' }

    private companion object {
        const val MIN_LENGTH = 15

        // The longest IBAN the standard allows.
        const val MAX_LENGTH = 34
    }
}
