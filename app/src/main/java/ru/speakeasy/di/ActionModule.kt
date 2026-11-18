package ru.speakeasy.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap
import ru.speakeasy.domain.action.ActionHandler
import ru.speakeasy.domain.action.LaunchAppActionHandler
import ru.speakeasy.domain.action.LaunchAppCardAction
import ru.speakeasy.domain.action.SpeakActionHandler
import ru.speakeasy.domain.action.SpeakCardAction

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