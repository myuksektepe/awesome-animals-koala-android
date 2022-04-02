package obidahi.books.animals.util.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import obidahi.books.animals.data.network.KtorClient
import obidahi.books.animals.data.repository.RemoteRepository
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object AppModule {

    @Singleton
    @Provides
    fun provideRemoteRepository(): RemoteRepository {
        return RemoteRepository
    }

    @Singleton
    @Provides
    fun provideKtorClient(): KtorClient {
        return KtorClient
    }
}