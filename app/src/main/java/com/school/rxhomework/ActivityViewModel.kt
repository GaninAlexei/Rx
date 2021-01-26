package com.school.rxhomework

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.schedulers.Schedulers

class ActivityViewModel : ViewModel() {

    private val _state = MutableLiveData<State>(State.Loading)
    val state: LiveData<State>
        get() = _state

    private val getStateSubject = PublishSubject.create<Unit>()

    val getStateObserver: Observer<Unit> = getStateSubject

    init{
        refreshData()
    }

    fun refreshData(){
        getStateSubject
                .subscribeOn(Schedulers.io())
                .switchMap { Repository.getPosts().toObservable() }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { posts ->
                            _state.value = State.Loaded(posts)
                        },
                        {
                            _state.value = State.Loaded(emptyList())
                        }
                )
    }

    fun processAction(action: Action) {
        when (action) {
            Action.RefreshData -> refreshData()
        }
    }
}
