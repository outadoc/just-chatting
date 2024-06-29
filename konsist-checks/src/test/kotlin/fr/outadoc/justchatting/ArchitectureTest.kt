package fr.outadoc.justchatting

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.architecture.KoArchitectureCreator.assertArchitecture
import com.lemonappdev.konsist.api.architecture.Layer
import org.junit.jupiter.api.Test

class ArchitectureTest {

    @Test
    fun `Check that clean architecture layers have correct dependencies`() {
        Konsist
            .scopeFromProject()
            .assertArchitecture {
                // Define layers
                val domain = Layer("Domain", "fr.outadoc.justchatting..domain..")
                val presentation = Layer("Presentation", "fr.outadoc.justchatting..presentation..")
                val data = Layer("Data", "fr.outadoc.justchatting..data..")

                // Define architecture assertions
                domain.dependsOnNothing()
                presentation.dependsOn(domain)
                data.dependsOn(domain)
            }
    }
}
