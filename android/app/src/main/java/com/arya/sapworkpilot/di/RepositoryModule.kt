package com.arya.sapworkpilot.di

import com.arya.sapworkpilot.data.repository.AuditRepositoryImpl
import com.arya.sapworkpilot.data.repository.AuthRepositoryImpl
import com.arya.sapworkpilot.data.repository.ChatRepositoryImpl
import com.arya.sapworkpilot.data.repository.DocumentRepositoryImpl
import com.arya.sapworkpilot.data.repository.MeetingRepositoryImpl
import com.arya.sapworkpilot.data.repository.RemoteProjectRepository
import com.arya.sapworkpilot.domain.repository.AuditRepository
import com.arya.sapworkpilot.domain.repository.AuthRepository
import com.arya.sapworkpilot.domain.repository.ChatRepository
import com.arya.sapworkpilot.domain.repository.DocumentRepository
import com.arya.sapworkpilot.domain.repository.MeetingRepository
import com.arya.sapworkpilot.domain.repository.ProjectRepository
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
    abstract fun bindProjectRepository(impl: RemoteProjectRepository): ProjectRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindChatRepository(impl: ChatRepositoryImpl): ChatRepository

    @Binds
    @Singleton
    abstract fun bindDocumentRepository(impl: DocumentRepositoryImpl): DocumentRepository

    @Binds
    @Singleton
    abstract fun bindMeetingRepository(impl: MeetingRepositoryImpl): MeetingRepository

    @Binds
    @Singleton
    abstract fun bindAuditRepository(impl: AuditRepositoryImpl): AuditRepository
}