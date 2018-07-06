/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package mozilla.components.feature.tabs.toolbar

import mozilla.components.concept.toolbar.Toolbar
import mozilla.components.feature.tabs.tabstray.mock
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class TabsToolbarFeatureTest {
    @Test
    fun `feature adds "tabs" button to toolbar`() {
        val toolbar: Toolbar = mock()
        TabsToolbarFeature(RuntimeEnvironment.application, toolbar) {}

        verify(toolbar).addBrowserAction(any())
    }
}

private fun <T> any(): T {
    Mockito.any<T>()
    return uninitialized()
}

@Suppress("UNCHECKED_CAST")
private fun <T> uninitialized(): T = null as T
