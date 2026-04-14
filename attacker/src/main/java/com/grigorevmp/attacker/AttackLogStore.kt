package com.grigorevmp.attacker

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AttackLogStore {
    private const val MAX_ENTRIES = 200

    private val flow = MutableStateFlow<List<String>>(emptyList())

    val logs: StateFlow<List<String>> = flow.asStateFlow()

    @Synchronized
    fun append(entry: String) {
        flow.value = (listOf(entry) + flow.value).take(MAX_ENTRIES)
    }

    fun clear() {
        flow.value = emptyList()
    }
}
