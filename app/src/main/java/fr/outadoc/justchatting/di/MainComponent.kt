package fr.outadoc.justchatting.di

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import fr.outadoc.justchatting.MainApplication
import javax.inject.Singleton

@Singleton
@Component(modules = [AndroidInjectionModule::class, MainModule::class, ActivityBuilderModule::class, DatabaseModule::class])
interface MainComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(application: Application): Builder

        fun build(): MainComponent
    }

    fun inject(mainApplication: MainApplication)
}
