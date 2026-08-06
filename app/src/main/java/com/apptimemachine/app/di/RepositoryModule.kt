package com.apptimemachine.app.di

import com.apptimemachine.app.data.repository.AppRepository
import com.apptimemachine.app.data.repository.AppRepositoryImpl
import com.apptimemachine.app.data.repository.TimelineRepository
import com.apptimemachine.app.data.repository.TimelineRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAppRepository(impl: AppRepositoryImpl): AppRepository

    @Binds
    @Singleton
    abstract fun bindTimelineRepository(impl: TimelineRepositoryImpl): TimelineRepository
}
