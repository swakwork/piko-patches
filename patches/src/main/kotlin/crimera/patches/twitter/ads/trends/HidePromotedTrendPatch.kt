package crimera.patches.twitter.ads.trends

import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.fingerprint
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.smali.ExternalLabel
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import crimera.patches.twitter.misc.settings.PREF_DESCRIPTOR
import crimera.patches.twitter.misc.settings.enableSettings
import crimera.patches.twitter.misc.settings.settingsPatch
import crimera.patches.twitter.misc.settings.settingsStatusLoadFingerprint

internal val hidePromotedTrendFingerprint = fingerprint {
    returns("Ljava/lang/Object;")
    custom { it, _ ->
        it.definingClass == "Lcom/twitter/model/json/timeline/urt/JsonTimelineTrend;"
    }
}

@Suppress("unused")
val hidePromotedTrendPatch = bytecodePatch(
    name = "Hide Promoted Trends",
) {
    dependsOn(settingsPatch)
    compatibleWith("com.twitter.android")

    val result by hidePromotedTrendFingerprint()
    val settingsStatusMatch by settingsStatusLoadFingerprint()

    execute {
        val method = result.mutableMethod
        val instructions = method.instructions

        val return_obj = instructions.last { it.opcode == Opcode.RETURN_OBJECT }
        val return_loc = return_obj.location.index
        val return_reg = method.getInstruction<OneRegisterInstruction>(return_loc).registerA

        val last_new_inst = instructions.last { it.opcode == Opcode.NEW_INSTANCE }.location.index
        val loc = last_new_inst + 3
        val reg = method.getInstruction<TwoRegisterInstruction>(loc).registerA

        val HOOK_DESCRIPTOR =
            "invoke-static {v$reg}, ${PREF_DESCRIPTOR};->hidePromotedTrend(Ljava/lang/Object;)Z"

        method.addInstructionsWithLabels(
            return_loc, """
            $HOOK_DESCRIPTOR
            move-result v$reg
            if-eqz v$reg, :cond_1212
            const v$return_reg, 0x0
        """.trimIndent(),
            ExternalLabel("cond_1212", return_obj)
        )

        settingsStatusMatch.enableSettings("hidePromotedTrends")
    }
}