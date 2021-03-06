package com.example.test_diplom.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test_diplom.data.model.homeFragment.ItemHome
import com.example.test_diplom.data.model.homeFragment.popular.ListFilm
import com.example.test_diplom.repository.HomeRepository
import com.example.test_diplom.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepository
) : ViewModel() {

    private val _all = MutableStateFlow<AllEvent>(AllEvent.Empty)
    val all: StateFlow<AllEvent> = _all

    init {
        getAll()
    }

    fun getAll() {
        viewModelScope.launch {
            _all.value = AllEvent.Loading
            try {
                val res: MutableList<ItemHome> = mutableListOf()
                val destination = "home"
                coroutineScope {
                    val call1 = async(Dispatchers.IO) { homeRepository.getListPopular(null) }
                    checkRightState(call1, res, "Popular",destination)

                    val call2 = async(Dispatchers.IO) { homeRepository.getListTopRated(null) }
                    checkRightState(call2, res, "TopRated",destination)

                    val call3 = async(Dispatchers.IO) { homeRepository.getListUpcoming(null) }
                    checkRightState(call3, res, "Upcoming",destination)

                    _all.value = AllEvent.Success(res)

                }
            } catch (e: Exception) {
                _all.value = AllEvent.Failure(e.message.toString())
            }
        }
    }

    private suspend fun checkRightState(
        call: Deferred<Resource<ListFilm>>,
        res: MutableList<ItemHome>,
        title: String,
        destination: String
    ) {
        when (val response = call.await()) {
            is Resource.Success -> {
                res.add(ItemHome(title, response.data!!,destination))
            }
            else -> {
                _all.value = AllEvent.Failure(response.message.toString())
                call.cancel()
            }
        }
    }

}

sealed class AllEvent {
    class Success(val result: List<ItemHome>) : AllEvent()
    class Failure(val errorText: String) : AllEvent()
    object Loading : AllEvent()
    object Empty : AllEvent()
}