/* Copyright 2017 Andrew Dawson
 *
 * This file is a part of Tusky.
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * Tusky is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Tusky; if not,
 * see <http://www.gnu.org/licenses>. */

package com.keylesspalace.tusky.fragment

import android.os.Bundle
import android.util.Log

import com.keylesspalace.tusky.ViewMediaActivity
import com.keylesspalace.tusky.entity.Attachment

abstract class ViewMediaFragment : BaseFragment() {
    private var toolbarVisibiltyDisposable: Function0<Boolean>? = null

    abstract fun setupMediaView(url: String)
    abstract fun onToolbarVisibilityChange(visible: Boolean)

    companion object {
        @JvmStatic protected val ARG_START_POSTPONED_TRANSITION = "startPostponedTransition"
        @JvmStatic protected val ARG_ATTACHMENT = "attach"
        @JvmStatic protected val ARG_AVATAR_URL = "avatarUrl"
        private const val TAG = "ViewMediaFragment"

        @JvmStatic
        fun newInstance(attachment: Attachment, shouldStartPostponedTransition: Boolean): ViewMediaFragment {
            val arguments = Bundle(2)
            arguments.putParcelable(ARG_ATTACHMENT, attachment)
            arguments.putBoolean(ARG_START_POSTPONED_TRANSITION, shouldStartPostponedTransition)

            var fragment: ViewMediaFragment? = null
            when (attachment.type) {
                Attachment.Type.IMAGE -> fragment = ViewImageFragment()
                Attachment.Type.VIDEO,
                Attachment.Type.GIFV -> fragment = ViewVideoFragment()
                else -> Log.e(TAG, "Unknown media type")
            }
            fragment!!.arguments = arguments
            return fragment
        }

        @JvmStatic
        fun newAvatarInstance(avatarUrl: String): ViewMediaFragment {
            val arguments = Bundle(2)
            val fragment = ViewImageFragment()
            arguments.putString(ARG_AVATAR_URL, avatarUrl)
            arguments.putBoolean(ARG_START_POSTPONED_TRANSITION, true)

            fragment.arguments = arguments
            return fragment
        }
    }

    protected fun setupToolbarVisibilityListener() {
        toolbarVisibiltyDisposable = (activity as ViewMediaActivity).addToolbarVisibilityListener(object: ViewMediaActivity.ToolbarVisibilityListener {
            override fun onToolbarVisiblityChanged(isVisible: Boolean) {
                onToolbarVisibilityChange(isVisible)
            }
        })
    }

    override fun onDestroyView() {
        toolbarVisibiltyDisposable?.invoke()
        super.onDestroyView()
    }
}
