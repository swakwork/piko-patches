package crimera.patches.twitter.misc.fab

import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.fingerprint
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.smali.ExternalLabel
import com.android.tools.smali.dexlib2.Opcode
import crimera.patches.twitter.misc.settings.PREF_DESCRIPTOR
import crimera.patches.twitter.misc.settings.enableSettings
import crimera.patches.twitter.misc.settings.settingsPatch
import crimera.patches.twitter.misc.settings.settingsStatusLoadFingerprint

internal val hideFABFingerprint = fingerprint {
    strings("android_compose_fab_menu_enabled")
}

@Suppress("unused")
val hideFABPatch = bytecodePatch(
    name = "Hide FAB",
    description = "Adds an option to hide Floating action button",
) {
    dependsOn(settingsPatch)
    compatibleWith("com.twitter.android")

    val hideFABFingerprintMatch by hideFABFingerprint()
    val settingsStatusMatch by settingsStatusLoadFingerprint()

    execute {

        val method = hideFABFingerprintMatch.mutableMethod
        val instructions = method.instructions
        val constObj = instructions.last { it.opcode == Opcode.CONST_4 }

        method.addInstructionsWithLabels(
            0, """
            invoke-static {}, ${PREF_DESCRIPTOR};->hideFAB()Z
            move-result v0
            if-nez v0, :cond_1212
        """.trimIndent(),
            ExternalLabel("cond_1212", constObj)
        )

        settingsStatusMatch.enableSettings("hideFAB")
    }
}