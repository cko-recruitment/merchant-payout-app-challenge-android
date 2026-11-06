package manoellribeiro.dev.domain.usecase

class MaskIbanUseCase {

    /** Keeps the first and last four characters, as in FR12*****1231. */
    operator fun invoke(iban: String): String {
        if (iban.length <= VISIBLE_AT_EACH_END * 2) return "*".repeat(iban.length)
        val hidden = iban.length - VISIBLE_AT_EACH_END * 2
        return iban.take(VISIBLE_AT_EACH_END) + "*".repeat(hidden) + iban.takeLast(VISIBLE_AT_EACH_END)
    }

    private companion object {
        const val VISIBLE_AT_EACH_END = 4
    }
}
