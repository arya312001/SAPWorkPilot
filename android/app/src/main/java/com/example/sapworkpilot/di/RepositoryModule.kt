package com.example.sapworkpilot.di

import com.example.sapworkpilot.data.repository.AuditRepositoryImpl
import com.example.sapworkpilot.data.repository.AuthRepositoryImpl
import com.example.sapworkpilot.data.repository.ChatRepositoryImpl
import com.example.sapworkpilot.data.repository.DocumentRepositoryImpl
import com.example.sapworkpilot.data.repository.MeetingRepositoryImpl
import com.example.sapworkpilot.data.repository.RemoteProjectRepository
import com.example.sapworkpilot.domain.repository.AuditRepository
import com.example.sapworkpilot.domain.repository.AuthRepository
import com.example.sapworkpilot.domain.repository.ChatRepository
import com.example.sapworkpilot.domain.repository.DocumentRepository
import com.example.sapworkpilot.domain.repository.MeetingRepository
import com.example.sapworkpilot.domain.repository.ProjectRepository
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