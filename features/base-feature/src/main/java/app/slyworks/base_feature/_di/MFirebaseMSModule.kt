package app.slyworks.base_feature._di

import android.content.Context
import app.slyworks.auth_lib.LoginManager
import app.slyworks.auth_lib.UsersManager
import app.slyworks.base_feature.NotificationHelper
import app.slyworks.base_feature.WorkInitializer
import app.slyworks.data_lib.DataManager
import app.slyworks.di_base_lib.MFirebaseMSScope
import app.slyworks.network_lib.NetworkRegister
import app.slyworks.utils_lib.PreferenceManager
import dagger.Module
import dagger.Provides

@Module
object MFirebaseMSModule {

  @Provides
  fun provideWorkInitializer(context: Context):WorkInitializer
  = WorkInitializer(context)
}
