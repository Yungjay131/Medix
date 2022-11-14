package com.slyworks.medix.di.components

import android.content.Context
import androidx.fragment.app.DialogFragment
import androidx.work.Worker
import com.slyworks.di.ApplicationScope
import com.slyworks.medix.App
import com.slyworks.medix.di.modules.ApplicationModule
import com.slyworks.medix.helpers.ListenerManager
import dagger.BindsInstance
import dagger.Component
import javax.inject.Named


/**
 *Created by Joshua Sylvanus, 3:51 PM, 07/08/2022.
 */
@ApplicationScope
@Component(modules = [ApplicationModule::class])
abstract class ApplicationComponent {
    abstract fun inject(app: App)

    abstract fun baseActivityComponentBuilder(): BaseActivityComponent.Builder
    abstract fun activityComponentBuilder(): ActivityComponent.Builder
    abstract fun dialogFragmentComponentBuilder():DialogFragmentComponent.Builder
    abstract fun serviceComponentBuilder(): ServiceComponent.Builder
    abstract fun workerComponentBuilder():WorkerComponent.Builder

    @Component.Builder
    interface Builder{
        fun componentContext(@BindsInstance
                             context: Context): Builder
        fun build(): ApplicationComponent
    }
}