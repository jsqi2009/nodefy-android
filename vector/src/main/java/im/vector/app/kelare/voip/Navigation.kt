/*
 * Copyright (c) 2010-2020 Belledonne Communications SARL.
 *
 * This file is part of linphone-android
 * (see https://www.linphone.org).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package im.vector.app.kelare.voip

import android.net.Uri
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.slidingpanelayout.widget.SlidingPaneLayout
import im.vector.app.R
import im.vector.app.kelare.voip.fragment.ComingCallFragment

internal fun Fragment.findMasterNavController(): NavController {
    return parentFragment?.parentFragment?.findNavController() ?: findNavController()
}

fun popupTo(
    popUpTo: Int = -1,
    popUpInclusive: Boolean = false,
    singleTop: Boolean = true
): NavOptions {
    val builder = NavOptions.Builder()
    builder.setPopUpTo(popUpTo, popUpInclusive).setLaunchSingleTop(singleTop)
    return builder.build()
}

/* Calls related */

internal fun VoiceCallActivity.navigateToActiveCall() {
    if (findNavController(R.id.nav_host_fragment).currentDestination?.id != R.id.callFragment) {
        findNavController(R.id.nav_host_fragment).navigate(
            R.id.action_global_singleCallFragment,
            null,
            null
        )
    }
}



/*internal fun VoiceCallActivity.navigateToOutgoingCall() {
    findNavController(R.id.nav_host_fragment).navigate(
        R.id.action_global_outgoingCallFragment,
        null,
        popupTo(R.id.singleCallFragment, true)
    )
}*/

internal fun VoiceCallActivity.navigateToIncomingCall(earlyMediaVideoEnabled: Boolean) {
    val args = Bundle()
    args.putBoolean("earlyMediaVideo", earlyMediaVideoEnabled)
    findNavController(R.id.nav_host_fragment).navigate(
        R.id.action_global_incomingCallFragment,
        args,
        popupTo(R.id.callFragment, true)
    )
}

/*internal fun OutgoingCallFragment.navigateToActiveCall() {
    findNavController().navigate(
        R.id.action_global_singleCallFragment,
        null,
        popupTo(R.id.outgoingCallFragment, true)
    )
}*/

internal fun ComingCallFragment.navigateToActiveCall() {
    findNavController().navigate(
        R.id.action_global_singleCallFragment,
        null,
        popupTo(R.id.comingCallFragment, true)
    )
}




