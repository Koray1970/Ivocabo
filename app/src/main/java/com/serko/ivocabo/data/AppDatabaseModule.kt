package com.serko.ivocabo.data

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class AppDatabaseModule {
    @Provides
    fun provideApplicationDB(@ApplicationContext context: Context)=
        Room.databaseBuilder(context,AppDatabase::class.java,"ivocabodb.db").allowMainThreadQueries().fallbackToDestructiveMigration().build()
    @Provides
    fun provideUserDao(appDatabase: AppDatabase)=appDatabase.userDao()
}