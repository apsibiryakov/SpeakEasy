package ru.speakeasy

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap

@Module
@InstallIn(SingletonComponent::class)
abstract class ActionModule {

    @Binds
    @IntoMap
    @ClassKey(SpeakCardAction::class)
    abstract fun bindSpeakActionHandler(handler: SpeakActionHandler): ActionHandler<*>

    @Binds
    @IntoMap
    @ClassKey(LaunchAppCardAction::class)
    abstract fun bindLaunchAppActionHandler(handler: LaunchAppActionHandler): ActionHandler<*>
}