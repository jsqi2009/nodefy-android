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
package im.vector.app.kelare.voip.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.doOnPreDraw
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.MaterialSharedAxis
import im.vector.app.VectorApplication.Companion.corePreferences
import org.linphone.core.tools.Log

abstract class GenericFragment<T : ViewDataBinding> : Fragment() {

    private var _binding: T? = null
    protected val binding get() = _binding!!

    protected var useMaterialSharedAxisXForwardAnimation = true


    protected fun isBindingAvailable(): Boolean {
        return _binding != null
    }

    private fun getFragmentRealClassName(): String {
        return this.javaClass.name
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            try {
                val navController = findNavController()
                Log.d("[Generic Fragment] ${getFragmentRealClassName()} handleOnBackPressed")
                if (!navController.popBackStack()) {
                    Log.d("[Generic Fragment] ${getFragmentRealClassName()} couldn't pop")
                    if (!navController.navigateUp()) {
                        Log.d("[Generic Fragment] ${getFragmentRealClassName()} couldn't navigate up")
                        // Disable this callback & start a new back press event
                        isEnabled = false
                        goBack()
                    }
                }
            } catch (ise: IllegalStateException) {
                Log.e("[Generic Fragment] ${getFragmentRealClassName()} Can't go back: $ise")
            }
        }
    }

    abstract fun getLayoutId(): Int

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = DataBindingUtil.inflate(inflater, getLayoutId(), container, false)
        return _binding!!.root
    }

    override fun onStart() {
        super.onStart()

        if (useMaterialSharedAxisXForwardAnimation && corePreferences.enableAnimations) {
            enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
            reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
            returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
            exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)

            postponeEnterTransition()
            binding.root.doOnPreDraw { startPostponedEnterTransition() }
        }

        setupBackPressCallback()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        onBackPressedCallback.remove()
        _binding = null
    }

    protected fun goBack() {
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    private fun setupBackPressCallback() {
        Log.d("[Generic Fragment] ${getFragmentRealClassName()} setupBackPressCallback")

        onBackPressedCallback.isEnabled = false

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback)
    }

}
