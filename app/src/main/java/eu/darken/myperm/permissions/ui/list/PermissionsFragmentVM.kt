package eu.darken.myperm.permissions.ui.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.myperm.common.coroutine.DispatcherProvider
import eu.darken.myperm.common.debug.logging.log
import eu.darken.myperm.common.livedata.SingleLiveEvent
import eu.darken.myperm.common.uix.ViewModel3
import eu.darken.myperm.main.ui.main.MainFragmentDirections
import eu.darken.myperm.permissions.core.PermissionRepo
import eu.darken.myperm.permissions.core.types.DeclaredPermission
import eu.darken.myperm.permissions.core.types.UnknownPermission
import eu.darken.myperm.permissions.ui.list.permissions.DeclaredPermissionVH
import eu.darken.myperm.permissions.ui.list.permissions.UnknownPermissionVH
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

@HiltViewModel
class PermissionsFragmentVM @Inject constructor(
    @Suppress("UNUSED_PARAMETER") handle: SavedStateHandle,
    dispatcherProvider: DispatcherProvider,
    permissionRepo: PermissionRepo,
) : ViewModel3(dispatcherProvider = dispatcherProvider) {

    private val searchTerm = MutableStateFlow<String?>(null)
    private val filterOptions = MutableStateFlow(FilterOptions())
    private val sortOptions = MutableStateFlow(SortOptions())

    val events = SingleLiveEvent<PermissionsEvents>()

    val listData: LiveData<List<PermissionsAdapter.Item>> = combine(
        permissionRepo.permissions,
        searchTerm,
        filterOptions,
        sortOptions
    ) { permissions, searchTerm, filterOptions, sortOptions ->
        val filtered = permissions
            .filter { perm -> filterOptions.keys.all { it.matches(perm) } }
            .filter {
                if (searchTerm == null) return@filter true
                if (it.id.toString().contains(searchTerm)) return@filter true
                if (it.label?.contains(searchTerm) == true) return@filter true

                return@filter false
            }
            .sortedWith(sortOptions.mainSort.comparator)

        filtered
            .sortedByDescending { it.grantedApps.size }
            .map { permission ->
                when (permission) {
                    is DeclaredPermission -> DeclaredPermissionVH.Item(
                        perm = permission,
                        onClickAction = {
                            log(TAG) { "Navigating to $permission" }
                            MainFragmentDirections.actionMainFragmentToPermissionDetailsFragment(
                                permissionId = permission.id
                            ).navigate()
                        }
                    )
                    is UnknownPermission -> UnknownPermissionVH.Item(
                        perm = permission,
                        onClickAction = {

                        }
                    )
                }
            }
    }
        .asLiveData2()


    fun onSearchInputChanged(term: String?) {
        log { "onSearchInputChanged(term=$term)" }
        searchTerm.value = term
    }

    fun updateFilterOptions(action: (FilterOptions) -> FilterOptions) {
        val old = filterOptions.value
        val new = action(old)
        log { "updateFilterOptions($old) -> $new" }
        filterOptions.value = new
    }

    fun updateSortOptions(action: (SortOptions) -> SortOptions) {
        val old = sortOptions.value
        val new = action(old)
        log { "updateFilterOptions($old) -> $new" }
        sortOptions.value = new
    }

    fun showFilterDialog() {
        log { "showFilterDialog" }
        events.postValue(PermissionsEvents.ShowFilterDialog(filterOptions.value))
    }

    fun showSortDialog() {
        log { "showSortDialog" }
        events.postValue(PermissionsEvents.ShowSortDialog(sortOptions.value))
    }
}