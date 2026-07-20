package com.example.nityaandroid.di


import android.content.Context
import androidx.room.Room
import com.example.nityaandroid.data.local.HabitDatabase
import com.example.nityaandroid.data.local.dao.HabitDao
import com.example.nityaandroid.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideHabitDatabase(
        @ApplicationContext context: Context
    ): HabitDatabase {
        return Room.databaseBuilder(
            context,
            HabitDatabase::class.java,
            Constants.DATABASE_NAME
        )
            .fallbackToDestructiveMigration(true) // Use this simplified syntax
            .build()
    }

    @Provides
    @Singleton
    fun provideHabitDao(database: HabitDatabase): HabitDao {
        return database.habitDao
    }
}