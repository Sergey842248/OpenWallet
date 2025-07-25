package nz.eloque.foss_wallet.model

import androidx.room.Embedded
import androidx.room.Relation
import nz.eloque.foss_wallet.model.field.PassField

private const val CHANGE_MESSAGE_FORMAT = "%@"

data class PassWithLocalization(
    @Embedded
    val pass: Pass,
    @Relation(
        parentColumn = "id",
        entityColumn = "passId"
    )
    val localizations: List<PassLocalization>
) {

    fun applyLocalization(locale: String): Pass {
        val mapping = localeMapping(locale).ifEmpty { localeMapping("en") }
        return pass.copy(
            description = mapping[pass.description]?.text ?: pass.description,
            headerFields = pass.headerFields.applyLocalization(mapping),
            primaryFields = pass.primaryFields.applyLocalization(mapping),
            secondaryFields = pass.secondaryFields.applyLocalization(mapping),
            auxiliaryFields = pass.auxiliaryFields.applyLocalization(mapping),
            backFields = pass.backFields.applyLocalization(mapping),
        )
    }

    private fun List<PassField>.applyLocalization(mapping: Map<String, PassLocalization>): List<PassField> {
        return this.map { field ->
            val localizedLabel = mapping[field.label]?.text ?: field.label
            val localizedChangeMessage = (mapping[field.changeMessage]?.text ?: field.changeMessage) ?.replace(CHANGE_MESSAGE_FORMAT, field.content.prettyPrint())
            field.copy(label = localizedLabel, changeMessage = localizedChangeMessage)
        }
    }

    private fun localeMapping(locale: String): Map<String, PassLocalization> {
        return localizations.filter { it.lang == locale }.associateBy { it.label }
    }
}