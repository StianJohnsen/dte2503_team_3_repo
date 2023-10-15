package com.example.dashcarr.extensions

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * Collects values from a [Flow] with lifecycle awareness.
 *
 * This extension function is used to collect values emitted by a [Flow] while taking into account the
 * lifecycle of a [LifecycleOwner]. It automatically handles the starting and stopping of collection
 * based on the [minActiveState] of the [Lifecycle]. The collected values are processed using the provided
 * [action] lambda.
 *
 * @param T The type of values emitted by the [Flow].
 * @param lifecycleOwner The [LifecycleOwner] whose lifecycle controls the collection.
 * @param minActiveState The minimum [Lifecycle.State] in which collection should occur (default is [Lifecycle.State.STARTED]).
 * @param action A lambda function to process each emitted value.
 * @return A [Job] representing the collection process, which can be used to cancel or manage the collection.
 */
inline fun <reified T> Flow<T>.collectWithLifecycle(
    lifecycleOwner: LifecycleOwner,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    noinline action: suspend (T) -> Unit,
): Job = lifecycleOwner.lifecycleScope.launch {
    flowWithLifecycle(lifecycleOwner.lifecycle, minActiveState).collect {
        action.invoke(it)
    }
}