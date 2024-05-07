package com.geeksville.mesh.di
import androidx.lifecycle.Lifecycle
import com.geeksville.mesh.database.dao.NodeInfoDao
import com.geeksville.mesh.model.MyNodeDB
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {
    @Provides
    fun provideMyNodeDB(nodeInfoDao: NodeInfoDao, processLifecycle: Lifecycle): MyNodeDB =
        MyNodeDB(processLifecycle, nodeInfoDao)
}
