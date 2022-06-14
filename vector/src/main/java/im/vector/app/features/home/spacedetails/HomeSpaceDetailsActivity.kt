/*
 * Copyright (c) 2022 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package im.vector.app.features.home.spacedetails

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.appcompat.widget.SearchView
import dagger.hilt.android.AndroidEntryPoint
import im.vector.app.R
import im.vector.app.core.extensions.replaceFragment
import im.vector.app.core.platform.VectorBaseActivity
import im.vector.app.databinding.ActivityFilteredRoomsBinding
import im.vector.app.databinding.ActivityHomeSpaceDetailsBinding
import im.vector.app.features.analytics.plan.MobileScreen
import im.vector.app.features.home.HomeSharedActionViewModel
import im.vector.app.features.home.RoomListDisplayMode
import im.vector.app.features.home.event.BackFromSpaceDetailsEvent
import im.vector.app.features.home.room.filtered.FilteredRoomsActivity
import im.vector.app.features.home.room.list.RoomListFragment
import im.vector.app.features.home.room.list.RoomListParams
import im.vector.app.features.location.LocationData
import im.vector.app.features.location.LocationSharingActivity
import im.vector.app.features.location.LocationSharingArgs
import im.vector.app.features.location.LocationSharingMode
import kotlinx.parcelize.Parcelize

@Parcelize
data class HomeSpaceDetailsArgs(
        val roomId: String,
        val displayName: String,
        val name: String?
) : Parcelable

@AndroidEntryPoint
class HomeSpaceDetailsActivity  : VectorBaseActivity<ActivityHomeSpaceDetailsBinding>(), View.OnClickListener  {

    override fun getBinding() = ActivityHomeSpaceDetailsBinding.inflate(layoutInflater)

    private val roomListFragment: RoomListFragment?
        get() {
            return supportFragmentManager.findFragmentByTag(FRAGMENT_TAG) as? RoomListFragment
        }
    private lateinit var sharedActionViewModel: HomeSharedActionViewModel

    override fun getCoordinatorLayout() = views.coordinatorLayout

    private var homeSpaceDetailsArgs: HomeSpaceDetailsArgs? = null

    override fun initUiAndData() {
        homeSpaceDetailsArgs = intent?.extras?.getParcelable(EXTRA_HOME_SPACE_DETAILS_ARGS)
        if (homeSpaceDetailsArgs == null) {
            finish()
            return
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusBarColor(this)

        analyticsScreenName = MobileScreen.ScreenName.Room
        sharedActionViewModel = viewModelProvider.get(HomeSharedActionViewModel::class.java)
        setupToolbar(views.filteredRoomsToolbar)
                .allowBack()
        if (isFirstCreation()) {
            val params = RoomListParams(RoomListDisplayMode.PEOPLE)
            replaceFragment(views.spaceRoomsFragmentContainer, RoomListFragment::class.java, params, FRAGMENT_TAG)
        }
        views.filteredRoomsSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                roomListFragment?.filterRoomsWith(newText)
                return true
            }
        })
        // Open the keyboard immediately
        //views.filteredRoomsSearchView.requestFocus()

        views.spaceNameView.text = homeSpaceDetailsArgs!!.displayName
        views.backImage.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.backImage    -> {
                onBackPressed()
            }
            else -> {}
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        mBus.post(BackFromSpaceDetailsEvent())
        finish()
    }

    companion object {
        private const val FRAGMENT_TAG = "SpaceRoomListFragment"
        private const val EXTRA_HOME_SPACE_DETAILS_ARGS = "EXTRA_HOME_SPACE_DETAILS_ARGS"

        fun getIntent(context: Context, homeSpaceDetailsArgs: HomeSpaceDetailsArgs): Intent {
            return Intent(context, HomeSpaceDetailsActivity::class.java).apply {
                putExtra(EXTRA_HOME_SPACE_DETAILS_ARGS, homeSpaceDetailsArgs)
            }
        }

        fun newIntent(context: Context): Intent {
            return Intent(context, HomeSpaceDetailsActivity::class.java)
        }
    }


}
