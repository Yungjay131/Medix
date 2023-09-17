package app.slyworks.base_feature._di

import android.content.Context
import app.slyworks.base_feature.WorkInitializer
import dagger.Module
import dagger.Provides

@Module
object MFirebaseMSModule {

  @Provides
  fun provideWorkInitializer(context: Context):WorkInitializer
  = WorkInitializer(context)
}
