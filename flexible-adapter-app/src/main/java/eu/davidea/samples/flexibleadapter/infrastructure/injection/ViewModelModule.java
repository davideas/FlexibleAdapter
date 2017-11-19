package eu.davidea.samples.flexibleadapter.infrastructure.injection;

import android.arch.lifecycle.ViewModelProvider;

import dagger.Binds;
import dagger.Module;

@Module
public abstract class ViewModelModule {

    @Binds
    abstract ViewModelProvider.Factory bindViewModelFactory(ViewModelFactory factory);

//    @Binds
//    @IntoMap
    //@ViewModelKey(UserViewModel.class)
    //abstract ViewModel bindUserViewModel(UserViewModel viewModel);

}