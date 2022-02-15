package awesome.animals.koala.util.di

import awesome.animals.koala.data.network.KtorClient
import awesome.animals.koala.data.repository.RemoteRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
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