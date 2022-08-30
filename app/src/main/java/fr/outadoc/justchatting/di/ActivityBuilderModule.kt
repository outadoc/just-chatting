package fr.outadoc.justchatting.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import fr.outadoc.justchatting.ui.chat.ChatActivity
import fr.outadoc.justchatting.ui.login.LoginActivity
import fr.outadoc.justchatting.ui.main.MainActivity
import fr.outadoc.justchatting.ui.settings.SettingsActivity

@Suppress("unused")
@Module(includes = [ViewModelModule::class])
abstract class ActivityBuilderModule {

    @ContributesAndroidInjector(modules = [FragmentBuilderModule::class])
    abstract fun contributeMainActivity(): MainActivity

    @ContributesAndroidInjector
    abstract fun contributeLoginActivity(): LoginActivity

    @ContributesAndroidInjector(modules = [FragmentBuilderModule::class])
    abstract fun contributeChatActivity(): ChatActivity

    @ContributesAndroidInjector(modules = [FragmentBuilderModule::class])
    abstract fun contributeSettingsActivity(): SettingsActivity
}
