package com.example.dashcarr.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ViewModelExample: ViewModel() {

    private var _numCounter = MutableLiveData<Int>(0)
    val numCounter: LiveData<Int>
        get() = _numCounter


    fun incCounter(){
        _numCounter.value = _numCounter.value?.plus(1)
    }

    fun decCounter(){
        _numCounter.value = _numCounter.value?.minus(1)
    }
}