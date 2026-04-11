package fr.outadoc.justchatting.feature.chat.presentation.ui

internal actual val enableColorTransitions: Boolean
    // Can't enable color transitions with Proguard as of 2025-08-10
    //
    // Exception in thread "AWT-EventQueue-0" java.lang.VerifyError: Inconsistent stackmap frames at branch target 10555
    // Exception Details:
    //   Location:
    //     com/materialkolor/ktx/ColorSchemeKt.animateColorScheme(Landroidx/compose/material3/ColorScheme;Lkotlin/jvm/functions/Function3;Ljava/lang/String;Landroidx/compose/runtime/Composer;II)Landroidx/compose/material3/ColorScheme; @10555: getstatic
    //   Reason:
    //     Type top (current frame, locals[64]) is not assignable to long (stack map, locals[64])
    get() = false
